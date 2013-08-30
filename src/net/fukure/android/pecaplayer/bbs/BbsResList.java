package net.fukure.android.pecaplayer.bbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fukure.android.pecaplayer.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BbsResList {
	
    private LayoutInflater inflater;
	private CommentListAdapter adapter = null;
    private ArrayList<Map<String, Object>> resData = new ArrayList<Map<String, Object>>();
	private ListView listView;;
    //TextAppearanceSpan cyanTextAppearanceSpan;
    
	public BbsResList(Activity activity){
		adapter = new CommentListAdapter(activity, resData, R.layout.res_template);

		listView = (ListView) activity.findViewById(R.id.resListView);
	    listView.setAdapter(adapter);
	    //listView.setAnimationCacheEnabled(false);
	    //listView.setScrollingCacheEnabled(false);
	    //listView.setTextFilterEnabled(true);
	 
	}
	
	public Map<String, Object> getItem(int i){
		return resData.get(i);
	}
	
	public void addMessage(ResData data){
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("num", data.num);
		map.put("name", data.name);
		map.put("message", data.message);
		map.put("addTime", System.currentTimeMillis());
		
		resData.add(0, map);
		if(resData.size()>100){
			resData.remove(100);
		}
	}
	
	public void refresh(){
		adapter.notifyDataSetChanged();
	}

	class CommentListAdapter extends SimpleAdapter {
	    CommentListAdapter(Activity activity, List<? extends Map<String, ?>> data, int resource) {
	        super(activity, data, resource, null, null);
	        BbsResList.this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }
	    
	    @Override
	    public int getCount() {
	    	return resData.size();
	    }
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	
	        final View v = (convertView == null)? inflater.inflate(R.layout.res_template, null) : convertView;
	 
	        Map<String, Object> settingData = resData.get(position);
	        TextView messageTextView = (TextView)v.findViewById(R.id.messageTextView);
	        TextView numTextView = (TextView)v.findViewById(R.id.numTextView);
	        
	        String message = settingData.get("message").toString();
	        String num = settingData.get("num").toString();

	        numTextView.setText(num);
	        messageTextView.setText(message);
	        
	        long time = (Long) settingData.get("addTime");
	        if(System.currentTimeMillis()-time<500){
		 
		        AlphaAnimation animation_alpha = new AlphaAnimation(0, 1);
		        animation_alpha.setDuration(1000);
		        
		        /*
		        HeightAnimation animation_scale = new HeightAnimation(v, 0, v.getHeight());
		        animation_scale.setDuration(500);
		        animation_scale.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
						v.setVisibility(View.VISIBLE);
					}
					@Override
					public void onAnimationRepeat(Animation animation) {}
					@Override
					public void onAnimationEnd(Animation animation) {}
				});
				*/
		        
		        AnimationSet animation_set = new AnimationSet( false );
		        animation_set.addAnimation( animation_alpha );
		        //animation_set.addAnimation( animation_scale );

		        //v.setVisibility(View.GONE);
		        v.startAnimation(animation_set);
	        }
	        
	        return v;
	    }
    
	}
 
}