package medi.mouse;

import medi.mouse.medi_mouse_activity.JSInterface;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class MainApp extends medi_mouse_activity implements OnSharedPreferenceChangeListener, OnTouchListener {
	static final String TAG = "MainApp";
	
	private View mInfoView;
	private float[] start_pos = new float[2];

	private View mNotesView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"SDK: "+SDK);
		setContentView(R.layout.main_view);
		
		View view = findViewById(R.id.MainFrame);
		view.setOnTouchListener(this);
		
    	view.setBackgroundColor(toColor(background_color));
    	spref.registerOnSharedPreferenceChangeListener(this);
    	setupInfoView();
    	setupRefreshButton();
    	if(SDK < android.os.Build.VERSION_CODES.HONEYCOMB) {
    		//add In/Out button
    		addInOutButton();
		}
    	WhatsNew();
	}
	private void addInOutButton() {
		View info = createBoxView();
		LinearLayout MainView = (LinearLayout) findViewById(R.id.MainLinearLayout);
		TableLayout TableView = (TableLayout) info.findViewById(R.id.TableLayout);
		TextView refresh = createTextView();
		refresh.setText("In/Out");
		refresh.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainApp.this,EditStatus.class);
        		startActivity(intent);
			}
		});
		TableView.addView(refresh);
		TableView.forceLayout();
		MainView.addView(info);
		
		MainView.forceLayout();
		MainView.invalidate();
		//MainView.buildLayer();
		
	}
	private View createNotesView() {
		mNotesView = createBoxView();
		refreshNotesView();
		return mNotesView;
	}

	private View setupInfoView(){
		mInfoView = createBoxView();
		LinearLayout MainView = (LinearLayout) findViewById(R.id.MainLinearLayout);
		refreshInfoView(mInfoView);
		MainView.addView(mInfoView);
		if(notes.length()>0){
			createNotesView();
			MainView.addView(mNotesView);
		}
		MainView.forceLayout();
		return mInfoView;
		
	}
	private void refreshInfoView(View info){
		TableLayout TableView = (TableLayout) info.findViewById(R.id.TableLayout);
		TableView.removeAllViews();
		TextView nameView = createTextView();
		nameView.setText(full_name);
		TextView statusView = createTextView();
		statusView.setText(status);
		
		TableView.addView(nameView);
		TableView.addView(statusView);
		info.invalidate();
		
	}
	private void refreshNotesView(){
		TableLayout TableView = (TableLayout) mNotesView.findViewById(R.id.TableLayout);
		TableView.removeAllViews();
		WebView wv = createWebView();
		wv.loadData(notes, "text/html", null);
		wv.loadUrl("file:///android_asset/notes.html");
        wv.addJavascriptInterface(new JSInterface(this), "Android");
        WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		TableView.addView(wv);
		mNotesView.invalidate();
	}
	private void refresh(){
		//FloatingLayout
		
		spinMouse();
		
		coretrax_args arg = new coretrax_args("get_status",username,password);
		coretrax_post trax_post = new coretrax_post(this);
		trax_post.execute(arg);
		
		//arg = new coretrax_args("get_notes",username,password);
		//trax_post = new coretrax_post(this);
		//trax_post.execute(arg);
	}
	private void setupRefreshButton(){
		View info = createBoxView();
		LinearLayout MainView = (LinearLayout) findViewById(R.id.MainLinearLayout);
		TableLayout TableView = (TableLayout) info.findViewById(R.id.TableLayout);
		TextView refresh = createTextView();
		refresh.setText("Refresh");
		refresh.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				MainApp.this.refresh();
			}
		});
		TableView.addView(refresh);
		TableView.forceLayout();
		MainView.addView(info);
		MainView.forceLayout();
		MainView.invalidate();
		//MainView.buildLayer();
		
	}
	

	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPostExecute(Object result) {
		Log.d(TAG,"onPostExecute");
		stopMouse();
		
		coretrax_resp resp = (coretrax_resp)result;
		JSONObject data = resp.getData();
		if(data.has("error")){
			
		}else{
			SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
	    	SharedPreferences.Editor editor = spref.edit();
			if(data.has("status")){
				try {
					status = data.getString("status");
					editor.putString("status", status);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(data.has("full_name")){
				try {
					full_name = data.getString("full_name");
					editor.putString("full_name", full_name);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(data.has("notes")){
				try {
					notes = data.getString("notes");
					Log.d(TAG,"notes: "+notes);
					editor.putString("notes", notes);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			editor.commit();
			refreshInfoView(mInfoView);
			refreshNotesView();
		}
	}

}
