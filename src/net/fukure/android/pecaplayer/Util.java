package net.fukure.android.pecaplayer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Util {
    public static String hexString(byte[] by, int len){
    	StringBuilder sb = new StringBuilder();
    	int i=0;
    	for (int b : by) {
    		int b2 = b & 0xff;
    		if (b2 < 16) sb.append("0");
    		sb.append(Integer.toHexString(b2));
    		sb.append(" ");
    		i++;
    		if(i>=len) break;
    	}
    	return sb.toString().trim();
    }
    public static int readInt4bytesLE(InputStream is) throws IOException{
    	byte[] b = new byte[4];
    	for (int i = 0; i < 4; i++) {
			int r = is.read();
			if(r<0) throw new IOException();
			b[i] = (byte) r;
		}
    	return intFrom4bytesLE(b);
    }
    public static int readInt4bytes(InputStream is) throws IOException{
    	byte[] b = new byte[4];
    	for (int i = 0; i < 4; i++) {
			int r = is.read();
			if(r<0) throw new IOException();
			b[i] = (byte) r;
		}
    	return intFrom4bytes(b);
    }
    public static int readInt3bytes(InputStream is) throws IOException{
		byte[] b = new byte[3];
		for (int i = 0; i < 3; i++) {
			int r = is.read();
			if(r<0) throw new IOException();
			b[i] = (byte) r;
		}
		return intFrom3bytes(b);
	}
	public static byte[] readBytes(InputStream is, int size) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while(true){
			if(baos.size()>=size) break;
			int readSize = size - baos.size();
			byte[] buffer = new byte[readSize];
			int r = is.read(buffer);
			baos.write(buffer,0,r);
		}
		return baos.toByteArray();
    }
    public static byte readByte(InputStream is) throws IOException{
    	int r = is.read();
    	if(r<0) throw new IOException();
    	return (byte) r;
    }
    public static int intFrom4bytesLE(byte[] b){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(b);
		bb.position(0);
		return bb.getInt();
	}
    public static int intFrom4bytes(byte[] b){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.put(b);
		bb.position(0);
		return bb.getInt();
	}
    public static int intFrom3bytes(byte[] b){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.put((byte)0);
		bb.put((byte)b[0]);
		bb.put((byte)b[1]);
		bb.put((byte)b[2]);
		bb.position(0);
		return bb.getInt();
	}
	public static byte[] intTo3bytes(int i){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(i);
		bb.position(1);
		byte[] buff = new byte[3];
		bb.get(buff, 0, 3);
		return buff;
	}
    public static byte[] intTo4bytes(int i){
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(i);
		bb.position(0);
		return bb.array();
	}
    public static String readline(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int r = 0;
	    do {
	    	r = is.read();
	    	baos.write(r);
	    	if(baos.size()>=64*1024){
	    		throw new IOException("64k overflow");
	    	}
	    } while (!(r==10));//LF
	    return baos.toString().trim();
	}
}
