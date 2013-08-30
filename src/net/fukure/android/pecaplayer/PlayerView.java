package net.fukure.android.pecaplayer;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2RGBA;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class PlayerView extends SurfaceView implements SurfaceHolder.Callback {

	final static String LOG_TAG = "PecaPlayerView";
	
	private Bitmap bitmap;
	private IplImage rgbaImage;
	private IplImage resizedImage;
	private int width = 0;
	private int height = 0;
	private int xoffset = 0;
	private int yoffset = 0;
	private boolean rendering = false;
	private FpsCounter fpsCounter = null;

	public PlayerView(Context context) {
		super(context);
	 
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		fpsCounter = new FpsCounter();

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		this.width = getMeasuredWidth();
		this.height = getMeasuredHeight();
	}

	public void surfaceCreated(SurfaceHolder holder) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	public void release(){
		if(bitmap!=null){
			bitmap.recycle();
		}
	}
	
	private void draw(){
		Canvas canvas = getHolder().lockCanvas();
		if(bitmap!=null){
			Paint paint = new Paint();
			canvas.drawColor(Color.BLACK);
			canvas.drawBitmap(bitmap, xoffset, yoffset, paint);
		}
		getHolder().unlockCanvasAndPost(canvas);
	}

	public void drawRequest(IplImage image) {
		if(!rendering && width>0 && height>0){
			new RenderingThread(image).start();
		}
	}
	
	class RenderingThread extends Thread{
		int w;
		int h;
		float scale;
		IplImage image;
		public RenderingThread(IplImage image) {
			this.w = image.cvSize().width();
			this.h = image.cvSize().height();
			if(width/w>height/h){
				scale = (float)width/w;
			}else{
				scale = (float)height/h;
			}
			
			this.image = image;//.clone();
			rendering = true;
		}
		@Override
		public void run() {
			try{
				int sw = (int) (w * scale);
				int sh = (int) (h * scale);
				xoffset = (width-sw)/2;
				yoffset = (height-sh)/2;
				if(bitmap==null){
					bitmap = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
				}
				if(rgbaImage==null){
					rgbaImage = IplImage.create(sw, sh, IPL_DEPTH_8U, 4);
				}
				if(resizedImage==null){
					resizedImage = IplImage.create(sw, sh, IPL_DEPTH_8U, 3);
				}
				
				for (int i = 0; i < 16; i++) {
					int currentFps = fpsCounter.getFPS();
					//System.out.println(currentFps+"/"+fps);
					if(currentFps>10){
						Thread.sleep(0,999999);
						continue;
					}
					
					if(currentFps<1){
						rendering = false;
						return;
					}
					
					break;
				}
				
				cvResize(image, resizedImage);
				cvCvtColor(resizedImage, rgbaImage, CV_BGR2RGBA);
				bitmap.copyPixelsFromBuffer(rgbaImage.getByteBuffer());
				
				draw();
				
				fpsCounter.addFrameTime();
			}catch(Exception e){
				Log.e(LOG_TAG, "onImage error "+e.getMessage());
			}

			rendering = false;
		}
	}
	
	int getFps(){
		return fpsCounter.getFPS();
	}
	
	class FpsCounter {
		
		private ArrayList<Long> frameTime = new ArrayList<Long>();

		void addFrameTime(){
			synchronized (this) {
				frameTime.add(System.currentTimeMillis());
				if(frameTime.size()>60){
					frameTime.remove(0);
				}
			}
		}
		
		int getFPS(){
			synchronized (this) {
				int count = 0;
				for (int i = 0; i < frameTime.size(); i++) {
					if(System.currentTimeMillis()-frameTime.get(i)<1000){
						count++;
					}
				}
				return count;
			}
		}
	}
}
