package net.fukure.android.pecaplayer.listener;

import java.util.ArrayList;

import org.json.JSONObject;

public interface OnBBSListener {
	void onBbsSubject(String[] ids, String[] subjects);
	void onBbsRes(ArrayList<JSONObject> list);
	void onBbsRes1000();
	void onBbsLoadError();
	void onBbsPreLoad();
	void onBbsLoaded();
}
