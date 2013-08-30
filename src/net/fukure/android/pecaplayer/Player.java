package net.fukure.android.pecaplayer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import net.fukure.android.pecaplayer.listener.OnPecaPlayerListener;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.Frame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class Player extends Thread {
	
	final static String LOG_TAG = "PecaPlayerPlayer";
	
	private OnPecaPlayerListener listener = null;
	private AudioTrack myAudioTrack = null;
	private PlayAudioThread audioThread = null;

	private String url = null;
	private boolean end = false;
	private double fps = 10;
	private long frameCount = 0;
	private int writeBufSize = 44100;
	private int sampleRate = 44100;
	
	private ByteBuffer byteBufferCache = null;
	private ByteBuffer shortDirectBuffer = null;

	static {
        System.loadLibrary("pecaplayer");
    }
	
	private native void floatbuffer2shortbuffer(int len, FloatBuffer fb, ShortBuffer bb);
	
	public Player(OnPecaPlayerListener listener, String url) {
		this.listener = listener;
		this.url = url;
	}
	
	@Override
	public void run() {
		try {
			play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void release(){
		end = true;
		stopAudioLine();
	}
	
	void play() throws Exception {
		
		FrameGrabber grabber = new FFmpegFrameGrabber(url);
		//grabber.setFormat("asf");
		//FrameGrabber grabber = new FFmpegFrameGrabber("/mnt/sdcard/test.mp4");

		if(grabber.getFrameRate()>15){
			grabber.setFrameRate(15);
		}
		grabber.start();
		
		sampleRate = grabber.getSampleRate();
		Log.i(LOG_TAG, "sampleRate:"+sampleRate);
		
		startAudioLine();
		
		try {
			Frame frame = grabber.grabFrame();
			while (frame!=null && !end) {
				if(frame.image!=null){
					long t = System.currentTimeMillis();
					onVideoFrame(frame.image);
					t = System.currentTimeMillis() - t;
					//Log.i(LOG_TAG, "video:"+t);
				}
				
				if(frame.samples!=null){
					long t = System.currentTimeMillis();
					onAudioFrame(frame.samples);
					t = System.currentTimeMillis() - t;
					//Log.i(LOG_TAG, "audio:"+t);
				}
				
				frameCount++;
				
				frame = grabber.grabFrame();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopAudioLine();
		
        grabber.stop();
        grabber.release();
        
        listener.onPlayEnd(url);
	}
	
	void onVideoFrame(IplImage iplImage) throws Exception{
		
		listener.onImage(iplImage);
		
	}

	void onAudioFrame(Buffer[] buffer) throws Exception {

		writeCache(buffer);
		
	}
	
	void writeCache(Buffer[] buffer){
		if(byteBufferCache==null){
			byteBufferCache = ByteBuffer.allocate(1024*1024);
		}
		
		byte[] b = floatBuffer2shortBufferNative((FloatBuffer)buffer[0]);
		if(b==null) return;

		synchronized (byteBufferCache) {
			if(byteBufferCache.position()+b.length<=byteBufferCache.capacity()){
				byteBufferCache.put(b);
			}
		}
	}
	
	byte[] readCache(){
		
		byte[] buff = null;
		synchronized (byteBufferCache) {
			if(byteBufferCache.position()>=writeBufSize){
				buff = new byte[writeBufSize];
				int p = byteBufferCache.position();
				byteBufferCache.position(0);
				byteBufferCache.get(buff);
				byteBufferCache.compact();
				byteBufferCache.position(p-buff.length);
			}
		}
		
		return buff;
	}

	private byte[] floatBuffer2shortBufferNative(FloatBuffer floatBuffer){

		if(shortDirectBuffer==null){
			shortDirectBuffer = ByteBuffer.allocateDirect(8192*16);
			shortDirectBuffer.order(ByteOrder.nativeOrder());
		}
	
		int size = floatBuffer.limit();
		shortDirectBuffer.position(0);
		shortDirectBuffer.limit(size*2);
		floatbuffer2shortbuffer(size, floatBuffer, shortDirectBuffer.asShortBuffer());

		byte[] buffer = new byte[size*2];
		if(shortDirectBuffer.limit()>=buffer.length){
			shortDirectBuffer.position(0);
			shortDirectBuffer.get(buffer);
		}
		return buffer;
		
	}

	class PlayAudioThread extends Thread {
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

			try {
				while(!end){
					byte[] b = readCache();
					if(b!=null){
						myAudioTrack.write(b, 0, b.length);
					}else{
						Thread.sleep(1);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	void startAudioLine() throws Exception{
		
		if(sampleRate==0) return;
		
		int bufSize = AudioTrack.getMinBufferSize(sampleRate,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
		
		writeBufSize = bufSize*4;
		
		myAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
							sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT, writeBufSize,
							AudioTrack.MODE_STREAM);

		myAudioTrack.play();

		audioThread = new PlayAudioThread();
		audioThread.start();
	}
	
	void stopAudioLine(){
		
		if(audioThread!=null){
			audioThread.interrupt();
		}
		if(myAudioTrack!=null){
			if(myAudioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING){
				myAudioTrack.stop();
			}
			myAudioTrack.release();
		}
		
	}

}
