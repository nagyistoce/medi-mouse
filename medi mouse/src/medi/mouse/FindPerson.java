package medi.mouse;

import java.util.ArrayList;
import java.util.HashMap;

import medi.mouse.EditStatus.menu_node;
import medi.mouse.EditStatus.myDate;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FindPerson extends medi_mouse_activity {
	private static final float TRANS_START = -25;
	private static final long TRANS_DUR = 1000;
	menu_node root,current;

	ArrayList<myDate> inputDates;
	private TextView tv;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setup variables
		setContentView(R.layout.find_person);   
        String username,stafflink,full_name,status,mydate,password;
        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
        
        
        username = spref.getString("user_name", "");
    	stafflink = spref.getString("stafflink", "");
    	
    	full_name = spref.getString("full_name", "");
    	status = spref.getString("status", "");
    	mydate = spref.getString("date", "");
    	password = spref.getString("user_password", "");
    	//-------------------------------------------------------------------------
    	//setup ui stuff
    	
    	
    	TranslateAnimation anim = new TranslateAnimation(0, 0, -50, 0);
    	TranslateAnimation animText = new TranslateAnimation(0, 0, -25, 0);
    	
        anim.setDuration(1000);
        anim.setFillAfter(true);
        animText.setDuration(700);
        animText.setFillAfter(true);
        
    	TextView name_view = (TextView) findViewById(R.id.name_view);
    	TextView status_view = (TextView) findViewById(R.id.status_view);
    	TextView date_view = (TextView) findViewById(R.id.date_view);
        ImageView picture = (ImageView) findViewById(R.id.picture_view);
        
    	name_view.setText(full_name);
    	status_view.setText(status);
    	date_view.setText(mydate);
    	
    	client = medi_post.connect(username, password);
        me = new medi_person(this,1);
    	
        tv = (TextView) findViewById(R.id.entry);
        tv.setVisibility(View.VISIBLE);
        tv.requestLayout();
        
        tv.setEnabled(true);
        
        
        name_view.startAnimation(animText);
        status_view.startAnimation(animText);
        date_view.startAnimation(animText);
        tv.startAnimation(anim);
        
        
        //http://www.meditech.com/employees/RATweb/RATweb.mps
        //VIEW.location.replace('RATWeb.mps?TYPE=Lookup&User='+value+'&Key='+lookup.value);
    	//HEAD.document.forms[0].User.value='';
        tv.setOnKeyListener(new OnKeyListener(){

			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				
				
				System.out.println(arg1);
				//enter key: 66
				if(arg1==66) {
					TextView tv = FindPerson.this.tv;
					String user = "";
					CharSequence ch = tv.getText();
					for(int x = 0; x< ch.length();x++) {
						user += tv.getText().charAt(x);
					}
					
					FindPerson.this.find(user);
					tv.setVisibility(View.GONE);
					
				}
				return false;
			}
        	
        });
        
        
        
		
	}
	protected void find(String user) {
		System.out.println(user);
		me.data = new HashMap<String, String>();
		me.data.put("TYPE", "Lookup");
		me.data.put("Key", "User");
		me.data.put("User", user);
		medi_post postme;
		postme = new medi_post(me.data);
		postme.execute(me);
	}
}
