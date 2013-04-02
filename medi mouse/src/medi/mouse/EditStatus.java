package medi.mouse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EditStatus extends medi_mouse_activity {
	static int TRANS_START=800;
	static int TRANS_DUR=300;
	
	static final int GET_MENU = 100;
	static final int GET_INFO = 101;
	static final int SET_STATUS = 102;
	
	private String username,full_name,status,mydate,password,menu;
	protected boolean one_shot=false;
	boolean menu_built = false;
	private int action;
	public void onCreate(Bundle savedInstanceState) {
			
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.view_user);   
	        
	        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
	        
	        username = spref.getString("user_name", "");
	    	password = spref.getString("user_password","");
	    	full_name = spref.getString("full_name", "");
	    	status = spref.getString("status", "");
	    	mydate = spref.getString("date", "");
	    	
	    	menu = spref.getString("trax_menu","");
	    	
	        me = new medi_person(this,1);
	        
	        me.username=username;
			me.password=password;
			
	        if(menu.length()==0){
	    		action = GET_MENU;
	    		coretrax_post ctpost = new coretrax_post(this);
	    		ctpost.execute("get_menu",username,password,null,null);
	    	} else {
	    		build_menu(menu);
	    	}
			
			
	    	
	    	
	    	
	    		        
	}
	private void info_UI_stuff(){
		//-------------------------------------------------------------------------
    	//setup ui stuff
    	TextView name_view = (TextView) findViewById(R.id.name_view);
    	TextView status_view = (TextView) findViewById(R.id.status_view);
    	TextView date_view = (TextView) findViewById(R.id.date_view);
        ImageView picture = (ImageView) findViewById(R.id.picture_view);
        
    	name_view.setText(full_name);
    	status_view.setText(status);
    	date_view.setText(mydate);
	}
	

	
	

	@Override
	public void onPostExecute(Object result) {
		
		
		String res = result.toString();
		switch(action){
		case GET_MENU:
			SharedPreferences spref=
			PreferenceManager.getDefaultSharedPreferences(me.context);

			SharedPreferences.Editor editor = spref.edit();
			
			editor.putString("trax_menu", res);
			editor.commit();
			build_menu(res);
			break;
		case GET_INFO:
		case SET_STATUS:
			save_status(res);
			
			break;
		}

		
		
	}
	public void build_menu(String menu_data){
		

	}
	private void save_status(String res) {
		SharedPreferences spref=
				PreferenceManager.getDefaultSharedPreferences(me.context);

		SharedPreferences.Editor editor = spref.edit();
		editor.putString("full_name", me.full_name);
		editor.putString("status", me.status);
		editor.putString("date", me.date);
		
		editor.commit();
		
	}
	class menu_node{
		JSONObject submenu;
		
	}

		
}
