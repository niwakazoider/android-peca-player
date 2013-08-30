package net.fukure.android.pecaplayer.listener;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public interface OnPecaPlayerListener {
	public void onImage(IplImage image);
	public void onPlayEnd(String url);
}
