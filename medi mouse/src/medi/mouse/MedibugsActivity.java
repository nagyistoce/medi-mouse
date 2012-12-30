package medi.mouse;


import java.util.ArrayList;
import org.acra.ErrorReporter;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MedibugsActivity extends medi_mouse_activity implements OnSharedPreferenceChangeListener{

	TextView name_view;
	TextView status_view;
	TextView date_view;
	ImageView picture;
	String status_message="";
	private boolean reloading;
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.view_user);
		
	    //	medi_post.setConnectionManager();
        
        
        //------------------------------------------------------------------------------------------
        //establish connection to server
        
        
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	boolean enable_core = spref.getBoolean("enable_core", true);
    	
    	spref.registerOnSharedPreferenceChangeListener(this);
    	
    	
    	me = new medi_person(this);
    	
    	//create list of linear layouts
        
        LinearLayout ll = (LinearLayout) findViewById(R.id.options_menu_view);
        ll.removeAllViews();
        View v = this.getLayoutInflater().inflate(R.layout.menu, null);
        TextView tv = (TextView) v.findViewById(R.id.name);
        
        tv.setText("Refresh");
        
        tv.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				if(!MedibugsActivity.this.reloading){
					
					MedibugsActivity.this.reload();
				}
			}});
        
        ll.addView(v, 0);
        ll.forceLayout();
        
        //screen refresh
        
        reload();
        if(enable_core){
        	core_post cpost = new core_post(false);
        	cpost.execute(me);
        }
    	
    	//----------------------------------------------------------------------------------------
    	    	

    	
    }
        
	@Override
    public void onDestroy(){
    	//disconnect
    	//medi_post.disconnect(client);
    	super.onDestroy();
    }
 
    /**
     * full reload.  Creates new connection with the server and requests current state.
     */
    public void reload(){
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(MedibugsActivity.this);
    	
		String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	
    	coretrax_post trax_post;
    	
    	me.username=username;
		me.password=password;
		
		trax_post = new coretrax_post();
		this.reloading = true;
		LinearLayout ll = (LinearLayout) findViewById(R.id.options_menu_view);
        TextView tv = (TextView) ll.getChildAt(0).findViewById(R.id.name);
        tv.setTextColor(Color.GRAY);
        
        if(!shared.debug){
        	//no need to test this, its working well.
        	trax_post.execute(me);
        }
		reload(1);
	}
    @Override
    public void onResume(){
    	super.onResume();
    	reload(1);
    }
    /**
     * reloads screen fields with saved data
     * @param t - ignored
     */
    public void reload(int t){
    	//just refresh screen
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
        me.full_name = spref.getString("full_name", "");
        me.status = spref.getString("status", "");
        me.date = spref.getString("date", "");
    	
    	name_view = (TextView) findViewById(R.id.name_view);
        status_view = (TextView) findViewById(R.id.status_view);
        date_view = (TextView) findViewById(R.id.date_view);
        picture = (ImageView) findViewById(R.id.picture_view);
        
    	name_view.setText(me.full_name);
    	status_view.setText(me.status);
    	date_view.setText(me.date);
    	
    	status_view.refreshDrawableState();

    	//ImageView picture = (ImageView) context.findViewById(R.id.picture_view);
    	/*
		WebView web_view = (WebView) findViewById(R.id.webview);
		web_view.setVisibility(View.VISIBLE);
		web_view.loadDataWithBaseURL(medi_post.BASE_URL, me.webview, 
				"text/html", "", medi_post.SITE);
		*/
		
    }
    
    /**
	 * create options menu
	 * 
	 * 
	 */
    	@Override
	public void onPostExecute(Object result) {
		me = (medi_person)result;
		if(reloading){
			reloading=false;
			LinearLayout ll = (LinearLayout) findViewById(R.id.options_menu_view);
			TextView tv = (TextView) ll.getChildAt(0).findViewById(R.id.name);
	        tv.setTextColor(Color.BLACK);
		}
		reload(1);
		
	}
    
}

class unauthorized extends Exception {
	private static final long serialVersionUID = 1L;
	
}
