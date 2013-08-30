package net.fukure.android.pecaplayer.bbs;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fukure.android.pecaplayer.http.HttpResponse;
import net.fukure.android.pecaplayer.http.HttpUtil;
import net.fukure.android.pecaplayer.listener.OnBBSListener;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class BbsModel {
	
	final static String LOG_TAG = "PecaPlayerBbs";
	
	private JSONObject setting;
	private OnBBSListener listener;
	private Thread bbsThread;
	private long getDelay = 0;
	private int errorCount = 0;
	private boolean autoReload = false;
	
	public String getUrl(){
		try {
			return setting.getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private void setSetting(String jsonstr) {
		try {
			this.setting = new JSONObject(jsonstr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getSetting(){
		try {
			return setting.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}
	}
	
	public void loadState(Activity activity) {
		try {
	        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
	        long time = sp.getLong("BbsSaveTime", 0);
	    	String jsonstr = sp.getString("BbsSetting", "{}");
	    	setSetting(jsonstr);
	        if(System.currentTimeMillis()-time>5000){
				setting.put("autoReload", false);
				setting.put("trash", true);
	        }else{
	        	if(setting.getString("url")!=null){
	        		if(!setting.getString("url").equals("")){
	        			if(!setting.has("trash")){
	        				getDelay = 7000;
	    	    			loadAllRes();
	        			}
	        		}
	        	}
	        }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void saveState(Activity activity) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        Editor edit = sp.edit();
        edit.putString("BbsSetting", getSetting());
        edit.putLong("BbsSaveTime", System.currentTimeMillis());
        edit.commit();
	}
	
	public BbsModel(OnBBSListener listener){
		this.listener = listener;
		this.setting = new JSONObject();
	}
	
	public BbsModel(OnBBSListener listener, String url, boolean autoReload) {

		this.listener = listener;
		this.setting = new JSONObject();
		
		try {
			init(url, autoReload);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void init(String url, boolean autoReload) throws Exception{

		this.setting.put("length", 0);
		this.setting.put("lastModified", null);
		this.setting.put("resnum", 1);
		this.setting.put("charset", "UTF-8");
		this.setting.put("url", url);
		this.setting.put("autoReload", autoReload);
		
		url = url.replaceAll("/bbs/lite/subject.cgi", "");
		url = url.replaceAll("/bbs/lite/read.cgi", "/bbs/read.cgi");
		
		String id, type, charset, subjecturl, daturl;
		
		//http://jbbs.livedoor.jp/bbs/read.cgi/game/32255/1264801878/l50
		//http://jbbs.livedoor.jp/study/10787/subject.txt
		String jbbsRegex = "http://jbbs.livedoor.jp/bbs/read.cgi/(\\w+)/(\\d+)/(\\d+)/";
		Pattern jbbsp = Pattern.compile(jbbsRegex);
		Matcher jbbsm = jbbsp.matcher(url);
		if (jbbsm.find()){
			String category = jbbsm.group(1);
			String board = jbbsm.group(2);
			id = jbbsm.group(3);
			type = "jbbs";
			charset = "EUC-JP";
			daturl = "http://jbbs.livedoor.jp/bbs/rawmode.cgi/"+category+"/"+board+"/"+id+"/";
			subjecturl = "http://jbbs.livedoor.jp/"+category+"/"+board+"/subject.txt";
			setting.put("id", id);
			setting.put("type", type);
			setting.put("charset", charset);
			setting.put("daturl", daturl);
			setting.put("subjecturl", subjecturl);
			setting.put("res", new JSONArray());
			Log.d(LOG_TAG, daturl);
			return;
		}

		//http://jbbs.livedoor.jp/game/52573/
		String jbbsBoardRegex = "http://jbbs.livedoor.jp/(\\w+)/(\\d+)/";
		Pattern jbbsboardp = Pattern.compile(jbbsBoardRegex);
		Matcher jbbsboardm = jbbsboardp.matcher(url);
		if (jbbsboardm.find()){
			String category = jbbsboardm.group(1);
			String board = jbbsboardm.group(2);
			id = "0000000000";
			type = "jbbs";
			charset = "EUC-JP";
			daturl = "http://jbbs.livedoor.jp/bbs/rawmode.cgi/"+category+"/"+board+"/"+id+"/";
			subjecturl = "http://jbbs.livedoor.jp/"+category+"/"+board+"/subject.txt";
			setting.put("id", id);
			setting.put("type", type);
			setting.put("charset", charset);
			setting.put("daturl", daturl);
			setting.put("subjecturl", subjecturl);
			setting.put("res", new JSONArray());
			getSubject();
			Log.d(LOG_TAG, daturl);
			return;
		}

		//http://yy33.kakiko.com/test/read.cgi/peercast/1264865694/l50
		//http://yy33.kakiko.com/peercast/dat/1264865694.dat;
		String yyRegex = "http://(yy.+(kakiko.com|kg))/test/read.cgi/(\\w+)/(\\d+)/";
		Pattern yyp = Pattern.compile(yyRegex);
		Matcher yym = yyp.matcher(url);
		if (yym.find()){
			String host = yym.group(1);
			String board = yym.group(3);
			id = yym.group(4);
			type = "yy";
			charset = "Shift_JIS";
			daturl = "http://"+host+"/"+board+"/dat/"+id+".dat";
			subjecturl = "http://"+host+"/"+board+"/subject.txt";
			setting.put("id", id);
			setting.put("type", type);
			setting.put("charset", charset);
			setting.put("daturl", daturl);
			setting.put("subjecturl", subjecturl);
			setting.put("res", new JSONArray());
			Log.d(LOG_TAG, daturl);
			return;
		}

		//http://yy82.60.kg/hatsunetsu/
		String yyBoardRegex = "http://(yy.+(kakiko.com|kg))/(\\w+)/";
		Pattern yyboardp = Pattern.compile(yyBoardRegex);
		Matcher yyboardm = yyboardp.matcher(url);
		if (yyboardm.find()){
			String host = yyboardm.group(1);
			String board = yyboardm.group(3);
			id = "0000000000";
			type = "yy";
			charset = "Shift_JIS";
			daturl = "http://"+host+"/"+board+"/dat/"+id+".dat";
			subjecturl = "http://"+host+"/"+board+"/subject.txt";
			setting.put("id", id);
			setting.put("type", type);
			setting.put("charset", charset);
			setting.put("daturl", daturl);
			setting.put("subjecturl", subjecturl);
			setting.put("res", new JSONArray());
			getSubject();
			Log.d(LOG_TAG, daturl);
			return;
		}
		
		/*
		If-Modified-Since: Tue, 16 Jul 2013 00:35:12 GMT
		If-None-Match: "e040cf-4b741-8099680"
		Last-Modified: Tue, 16 Jul 2013 00:35:12 GMT
		Etag: "e040f7-3bfe0-265fd000"
		Content-Length: 245728
		*/

		setting.put("autoReload", false);
		setting.put("url", "");
	}
	
	public void start(){
		if(bbsThread!=null){
			bbsThread.interrupt();
		}
		
		autoReload = true;
		
		try {
			if(!setting.getBoolean("autoReload")){
				return;
			}
			if(setting.getString("url")==null || setting.getString("url").equals("")){
				return;
			}
			if(setting.getString("url")==null || setting.getString("id").equals("0000000000")){
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		errorCount = 0;
		bbsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(getDelay);
					while(autoReload){
						if(errorCount>=3){
							throw new Exception();
						}
						try{
							get();
							errorCount = 0;
							listener.onBbsLoaded();
							Thread.sleep(10000);
							listener.onBbsPreLoad();
							Thread.sleep(1000);
						}catch(Exception e){
							errorCount++;
							Thread.sleep(10000);
						}
					}
				} catch (InterruptedException e) {
				} catch (Exception e) {
					listener.onBbsLoadError();
					e.printStackTrace();
				}
			}
		});
		bbsThread.start();
	}
	
	public void stop(){
		if(bbsThread!=null){
			bbsThread.interrupt();
		}
		autoReload = false;
	}
	
	private ArrayList<JSONObject> parse(String str) throws Exception {
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		String[] lines = (str+"").split("\r\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			if(setting.getString("type").equals("yy")){
				line = setting.getInt("resnum") +  "<>" + line;
			}
			String[] parts = line.split("<>");
			if(parts.length<3) continue;
			setting.put("resnum", setting.getInt("resnum")+1);
			
			String num = parts[0];
			String name = parts[1];
			//String mail = parts[2];
			//String date = parts[3];
			String body = parts[4].replaceAll("<br>", "\n");
			body = body.replaceAll("<.+?>", "");
			body = body.replaceAll("&lt;", "<");
			body = body.replaceAll("&gt;", ">");
			body = body.trim();
			if(body.length()>105){
				body = body.substring(0, 100) + "(ˆÈ‰º—ª";
			}

			JSONObject json = new JSONObject();
			try{
				json.put("num", num);
				json.put("name", name);
				json.put("message", body);
				list.add(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		return list;
	}
	
	public void getSubject() throws Exception {
		ArrayList<String> idlist = new ArrayList<String>();
		ArrayList<String> subjectlist = new ArrayList<String>();

		String subjecturl = setting.getString("subjecturl");
		String charset = setting.getString("charset");
		String type = setting.getString("type");
		
		Header[] requestHeader = new Header[0];
		HttpResponse response = HttpUtil.get(subjecturl, requestHeader, charset);
		String str = response.body;
		String[] lines = str.split("\r\n");
		
		String splitStr = ",";
		String idext = ".cgi";
		if(type.equals("yy")){
			splitStr = "<>";
			idext = ".dat";
		}
		
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();
			String[] parts = line.split(splitStr,2);
			String id = parts[0].replaceAll(idext, "");
			String title = parts[1];
			idlist.add(id);
			subjectlist.add(title);
			if(idlist.size()>=30) break;
		}
		
		if(response.status>=400 || response.status==0){
			stop();
			listener.onBbsLoadError();
		}else{
			String[] ids = idlist.toArray(new String[idlist.size()]);
			String[] subjects = subjectlist.toArray(new String[subjectlist.size()]);
			listener.onBbsSubject(ids, subjects);
		}
	}
	
	public void get() throws Exception {
		if(setting.getString("type")==null) return;
		if(setting.getInt("resnum")>=1000) return;
		if(setting.getString("id")==null) return;
		if(setting.getString("id").equals("0000000000")) return;
		
		String geturl = setting.getString("daturl");
		Header[] requestHeader = new Header[0];
		if(setting.getString("type").equals("yy")){
			if(setting.getInt("length")>0){
				if(setting.getString("lastModified")!=null){
					requestHeader = new Header[2];
					requestHeader[0] = new BasicHeader("Range", "bytes="+setting.getInt("length")+"-");
					requestHeader[1] = new BasicHeader("If-Modified-Since", setting.getString("lastModified"));
					//requestHeader[0] = new BasicHeader("If-None-Match", etag);
				}
			}
		}
		if(setting.getString("type").equals("jbbs")){
			if(setting.getInt("resnum")>1){
				geturl = setting.getString("daturl") + setting.getInt("resnum") + "-";
			}
		}
		
		Log.d(LOG_TAG, "get "+geturl);
		
		HttpResponse response = HttpUtil.get(geturl, requestHeader, setting.getString("charset"));
		Header[] responseHeaders = response.headers;
		for (int i = 0; i < responseHeaders.length; i++) {
			Header header = responseHeaders[i];
			if(header.getName().equals("Content-Length")){
				int length = Integer.parseInt(header.getValue());
				setting.put("length", setting.getInt("length")+length);
			}
			if(header.getName().equals("Last-Modified")){
				setting.put("lastModified", header.getValue());
			}
		}
		
		if(response.status>=400 || response.status==0){
			stop();
			listener.onBbsLoadError();
		}else{
			ArrayList<JSONObject> list = parse(response.body);
			if(list.size()>0){
				listener.onBbsRes(list);
				
				JSONArray array = (JSONArray)setting.get("res");
				for (int i = 0; i < list.size(); i++) {
					array.put(list.get(i));
				}
			}
			
			if(setting.getInt("resnum")>=1000){
				stop();
				listener.onBbsRes1000();
			}
		}
		
	}

	public void next(String idstr) {
		try {
        	stop();
			String id = setting.getString("id");
			String daturl = setting.getString("daturl").replaceAll(id, idstr);
			setting.put("daturl", daturl);
			setting.put("id", idstr);
			setting.put("length", 0);
			setting.put("resnum", 1);
			setting.put("lastModified", null);
			setting.put("res", new JSONArray());
        	start();
		} catch (JSONException e) {
			e.printStackTrace();
			listener.onBbsLoadError();
		}
	}

	public void asyncGet() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					get();
				} catch (Exception e) {
					listener.onBbsLoadError();
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void loadAllRes() {
		try {
			ArrayList<JSONObject> list = new ArrayList<JSONObject>();
			JSONArray jArray = (JSONArray)setting.get("res");
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject json = (JSONObject)jArray.get(i);
				list.add(json);
			}
			listener.onBbsRes(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
