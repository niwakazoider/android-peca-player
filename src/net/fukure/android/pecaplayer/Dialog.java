package net.fukure.android.pecaplayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

public class Dialog {
	
	private MainActivity activity;
	//private Gui gui;
	
	public Dialog(MainActivity activity) {
		this.activity = activity;
		//this.gui = activity.gui;
	}

	void showBbsErrorDialog(){
		
		LayoutInflater inf = LayoutInflater.from(activity);
        View bbsView = inf.inflate(R.layout.bbs_notice_dialog, activity.gui.getViewGroup(R.id.layout_bbs_notice_root));
        
        new AlertDialog.Builder(activity)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle("エラー")
        .setView(bbsView)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .show();
	}

	void showBbsNextThreadDialog(final String[] ids, final String[] subjects){
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("スレ選択");
        builder.setItems(subjects, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
            	activity.bbs.next(ids[i]);
            }
        });
 
        AlertDialog dialog = builder.create();
        dialog.show();
	}

	/*
	void showLicenseDialog(){
		
		LayoutInflater inf = LayoutInflater.from(activity);
        View licenseView = inf.inflate(R.layout.about, gui.getViewGroup(R.id.layout_about_root));

        gui.linkMyTextView = gui.getTextView(licenseView, R.id.myCodeTextView6);
        gui.linkJavaCVTextView = gui.getTextView(licenseView, R.id.javacvCodeTextView4);
        Linkify.addLinks(gui.linkMyTextView, Linkify.ALL);
        Linkify.addLinks(gui.linkJavaCVTextView, Linkify.ALL);
        
        gui.licenseDialog = new AlertDialog.Builder(activity)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(activity.resourceIDToString(R.string.about_license))
        .setView(licenseView)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .show();
	}
	void showBBSInputDialog(String url){
		
		if(gui.bbsInputDialog!=null){
			gui.bbsUrlEditText.setText(url);
			gui.bbsInputDialog.show();
			return;
		}
		
		LayoutInflater inf = LayoutInflater.from(activity);
        View bbsView = inf.inflate(R.layout.bbs_dialog, gui.getViewGroup(R.id.layout_bbs_root));

        gui.bbsUrlEditText = gui.getEditText(bbsView, R.id.bbsUrlEditText);
        gui.bbsAutoReloadCheckBox = gui.getCheckBox(bbsView,R.id.bbsAutoReloadCheckBox);
        gui.bbsUrlEditText.setText(url);
		
        gui.bbsInputDialog = new AlertDialog.Builder(activity)
	        .setIcon(android.R.drawable.ic_dialog_info)
	        .setTitle("スレッドの設定")
	        .setView(bbsView)
	        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	gui.bbsInputDialog.hide();
	            	new Thread(new Runnable() {

						@Override
						public void run() {
			            	String url = gui.bbsUrlEditText.getText().toString();
			            	boolean auto = gui.bbsAutoReloadCheckBox.isChecked() ? true : false;
			            	
		            		activity.bbs.stop();
		            		activity.bbs = new BbsModel(activity.bbsHandler, url, auto);
		            		activity.bbs.start();
						}
					}).start();
	            }
	        })
	        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            }
	        })
	        .show();
	}
	*/
}
