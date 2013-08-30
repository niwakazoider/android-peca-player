package net.fukure.android.pecaplayer.bbs;

import org.json.JSONException;
import org.json.JSONObject;

public class ResData {
	public String name = "";
	public String message = "";
	public String num = "";
	
	public ResData(String jsonstr){
		JSONObject json;
		try {
			json = new JSONObject(jsonstr);
		} catch (JSONException e) {
			json = new JSONObject();
		}
		parse(json);
	}
	
	public ResData(JSONObject json) {
		parse(json);
	}
	
	private void parse(JSONObject json) {
		try {
			if(json.has("message")){
				message = json.getString("message");
			}
			if(json.has("name")){
				name = json.getString("name");
			}
			if(json.has("num")){
				num = json.getString("num");
			}
		} catch (JSONException e) {
		}
	}
}