package net.fukure.android.pecaplayer;

import net.fukure.android.pecaplayer.MainActivity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Gui {
	private TextView fpsView;
	private TextView memoryView;
	private MainActivity activity;
	private Thread fpsThread;
	PlayerView playerView;
	ProgressBar loadingBar;
	Handler handler;
	
	Gui(MainActivity activity){
		this.activity = activity;
		this.handler = new Handler();
		
		init();
	}
	void init(){
		memoryView = (TextView)activity.findViewById(R.id.memTextView);
		fpsView = (TextView)activity.findViewById(R.id.fpsTextView);
		
		playerView = new PlayerView(activity);
		LinearLayout mainLayout = (LinearLayout)activity.findViewById(R.id.mainView);
		mainLayout.addView(playerView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

		loadingBar = getProgressBar(R.id.loadingProgressBar);
		loadingBar.setVisibility(View.GONE);
	}
	
	void startFpsThread(){
		stopFpsThread();
		fpsThread = new FpsUpdateThread();
		fpsThread.start();
	}
	void stopFpsThread(){
		if(fpsThread!=null){
			fpsThread.interrupt();
		}
	}

	class FpsUpdateThread extends Thread{
		@Override
		public void run() {
			try {
				while(true){
					handler.post(new Runnable() {
						@Override
						public void run() {
							if(activity.player!=null && playerView!=null){
								fpsView.setText("fps "+playerView.getFps());
							}else{
								fpsView.setText("fps 0");
							}
							
							Runtime runtime = Runtime.getRuntime();
							
							String memusage = "";
							memusage += "memory " + (int)((runtime.totalMemory() - runtime.freeMemory())/1024);
							memusage += "/" + (int)(runtime.totalMemory()/1024);
							//memusage += "free=" + (int)(runtime.freeMemory()/1024);
							//memusage += "max=" + (int)(runtime.maxMemory()/1024);
							
							memoryView.setText(memusage);
						}
					});
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void setVisibility(final View view, final boolean visible){
		handler.post(new Runnable(){
			@Override
		    public void run() {
				if(visible){
					view.setVisibility(View.VISIBLE);
				}else{
					view.setVisibility(View.GONE);
				}
		    }
		});
	}
	
	void toast(final String msg){
		handler.post(new Runnable(){
			@Override
		    public void run() {
				Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
		    }
		});
	}
	
	void toast(final int id){
		toast(activity.getResources().getString(id));
	}
	
	ProgressBar getProgressBar(int id){
		return (ProgressBar) activity.findViewById(id);
	}
	
	ProgressBar getProgressBar(View view, int id){
		return (ProgressBar) view.findViewById(id);
	}

	ViewGroup getViewGroup(int id){
		return (ViewGroup) activity.findViewById(id);
	}
	
	ViewGroup getViewGroup(View view, int id){
		return (ViewGroup) view.findViewById(id);
	}
}
