package net.fukure.android.pecaplayer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import net.fukure.android.pecaplayer.bbs.BbsModel;
import net.fukure.android.pecaplayer.bbs.BbsResList;
import net.fukure.android.pecaplayer.bbs.ResData;
import net.fukure.android.pecaplayer.http.HttpResponse;
import net.fukure.android.pecaplayer.http.HttpUtil;
import net.fukure.android.pecaplayer.listener.OnBBSListener;
import net.fukure.android.pecaplayer.listener.OnPecaPlayerListener;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class MainActivity extends Activity implements OnPecaPlayerListener {

	final static String LOG_TAG = "PecaPlayer";
	
	private BbsResList bbsResList = null;
	private BbsHandler bbsHandler = null;
	private int retryCount = 0;
	private boolean front = false;

	Player player = null;
	Dialog dialog = null;
	BbsModel bbs = null;
	Gui gui = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		gui = new Gui(this);
	
		bbsHandler = new BbsHandler();
		bbsResList = new BbsResList(this);
	
		bbs = new BbsModel(bbsHandler);
		bbs.loadState(this);
		bbs.start();
		
		dialog = new Dialog(this);
	
		try {
			ArrayList<JSONObject> list = new ArrayList<JSONObject>();
			
			JSONObject json = new JSONObject();
			json.put("num", "0");
			json.put("name", "");
			json.put("message", "ypアプリからmmsh://～のストリームurlで起動してください");
			list.add(json);
			
			json = new JSONObject();
			json.put("num", "0");
			json.put("name", "");
			json.put("message", "Peercast for Android と連帯すると、わいわい/したらば掲示板のレスを表示できます");
			list.add(json);
			
			bbsHandler.onBbsRes(list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		retryCount = 0;
		
		readIntent();
		
		Log.d(LOG_TAG, "onStart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		front = true;
		
		bbs.start();
		
		gui.startFpsThread();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		front = false;

		bbs.stop();
		
		gui.stopFpsThread();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		retryCount = 5;
		
		stop();

		Log.d(LOG_TAG, "onStop");
		
		finish();
	}
	
	@Override
	protected void onDestroy() {

		bbs.saveState(this);
		
		gui.playerView.release();

		Log.d(LOG_TAG, "onDestroy");
		
		super.onDestroy();
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public void onImage(IplImage image) {
		if(front){
			gui.playerView.drawRequest(image);
		}
	}

	void readIntent(){
		Intent intent = getIntent();
		if (intent !=null && Intent.ACTION_VIEW.equals(intent.getAction())) {
			String url = intent.getDataString();
			if(url!=null){
				getBbsUrlFromPeercastRelayHtml(url);
				play(url);
				setIntent(null);
			}			
		}
	}

	void play(String url){
		if(player!=null){
			player.release();
		}
		try {
			player = new Player(this, url);
			player.start();
			Log.d(LOG_TAG, "play "+url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void stop(){
		if(player!=null){
			try {
				player.release();
				Log.d(LOG_TAG, "stop");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPlayEnd(String url) {
	
		retryCount++;
		if(retryCount>2) return;
		
		Log.d(LOG_TAG, "retry "+retryCount);
		
		play(url);
	}

	void bbsThread(String contactUrl){
		Log.d(LOG_TAG, "contact url:"+contactUrl);
		bbs.stop();
		bbs = new BbsModel(bbsHandler, contactUrl, true);
		bbs.start();
	}

	void getBbsUrlFromPeercastRelayHtml(final String url){
		Log.d(LOG_TAG, "get bbs url:"+url);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] parts = url.split("/");
				if(!parts[0].equals("mmsh:") || !parts[3].equals("stream")) return;
				String host = parts[2];
				String id = parts[4].substring(0, 32);
						
				HttpResponse response = HttpUtil.get("http://"+host+"/html/ja/relays.html");
				String html = response.body;
				//Log.d(LOG_TAG, html);
				
				String regex = id+".+(http[^>|\"]+).+"+id;
				Pattern p = Pattern.compile(regex, Pattern.DOTALL);
			
				Matcher m = p.matcher(html);
				if (m.find()&&m.groupCount()>0){
					String contactUrl = m.group(1);
					bbsThread(contactUrl);
				}
			}
		}).start();
	}

	class BbsHandler implements OnBBSListener {
		@Override
		public void onBbsPreLoad() {
			gui.setVisibility(gui.loadingBar, true);
		}
	
		@Override
		public void onBbsLoaded() {
			gui.setVisibility(gui.loadingBar, false);
		}
	
		@Override
		public void onBbsRes(final ArrayList<JSONObject> list) {
			gui.handler.post(new Runnable(){
				@Override
			    public void run() {
					for (int i = 0; i < list.size(); i++) {
						ResData messageData = new ResData(list.get(i));
						bbsResList.addMessage(messageData);
					}
					bbsResList.refresh();
			    }
			});
		}
	
		@Override
		public void onBbsRes1000() {
			try {
				bbs.getSubject();
			} catch (Exception e) {
				onBbsLoadError();
				Log.e(LOG_TAG, "res 1000 error");
			}
		}
	
		@Override
		public void onBbsLoadError() {
			gui.handler.post(new Runnable(){
				@Override
			    public void run() {
					//dialog.showBbsErrorDialog();
					gui.toast(R.string.bbs_error_msg);
			    }
			});
		}
		
		@Override
		public void onBbsSubject(final String[] ids, final String[] subjects) {
			gui.handler.post(new Runnable(){
				@Override
			    public void run() {
					dialog.showBbsNextThreadDialog(ids, subjects);
			    }
			});
		}
		
	}

}
