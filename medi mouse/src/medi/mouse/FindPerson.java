package medi.mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import medi.mouse.EditStatus.menu_node;
import medi.mouse.EditStatus.myDate;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FindPerson extends medi_mouse_activity {
	private static final float TRANS_START = -25;
	private static final long TRANS_DUR = 1000;
	menu_node root,current;

	ArrayList<myDate> inputDates;
	String stafflink,myStafflink;
	private TextView tv;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setup variables
		setContentView(R.layout.find_person);   
        String username,full_name,status,mydate,password;
        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
        
        
        username = spref.getString("user_name", "");
    	myStafflink = spref.getString("stafflink", "");
    	password = spref.getString("user_password", "");
    	//-------------------------------------------------------------------------
    	//setup ui stuff
    	
    	
    	TranslateAnimation anim = new TranslateAnimation(0, 0, -50, 0);
    	TranslateAnimation animText = new TranslateAnimation(0, 0, -25, 0);
    	
        anim.setDuration(1000);
        anim.setFillAfter(true);
        animText.setDuration(700);
        animText.setFillAfter(true);
        
    	
    	client = medi_post.connect(username, password);
        me = new medi_person(this,1);
    	
        tv = (TextView) findViewById(R.id.entry);
        tv.setVisibility(View.VISIBLE);
        tv.requestLayout();
        
        tv.setEnabled(true);
        
        
        tv.startAnimation(anim);

        //http://www.meditech.com/employees/RATweb/RATweb.mps
        //VIEW.location.replace('RATWeb.mps?TYPE=Lookup&User='+value+'&Key='+lookup.value);
    	//HEAD.document.forms[0].User.value='';
        
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println(keyCode);
		if (event.isLongPress()) {
			return false;
		}else if (keyCode == KeyEvent.KEYCODE_BACK ||
	    		keyCode == KeyEvent.KEYCODE_ENTER) {
	    	
	    	tv = (TextView) findViewById(R.id.entry);
	    	if (tv.getText().length()>0){
	    		String user = "";
				CharSequence ch = tv.getText();
				for(int x = 0; x< ch.length();x++) {
					user += tv.getText().charAt(x);
				}
				
				FindPerson.this.find(user);
				//tv.setVisibility(View.INVISIBLE);
				//tv.setVisibility(View.GONE);
				return false;
	    	} else {
	    		return super.onKeyDown(keyCode, event);
	    	}
	        
	    } else {
	    	return super.onKeyDown(keyCode, event);
	    }
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
	@Override
	public void onPostExecute(medi_person result) {
		// TODO Auto-generated method stub
		me = result;
		
		tv = (TextView) findViewById(R.id.entry);
		tv.setText("");
		Iterator<String> keys = me.found_people.keySet().iterator();
		
		TranslateAnimation anim = new TranslateAnimation(TRANS_START, 0, 0, 0);
        anim.setDuration(TRANS_DUR);
        anim.setFillAfter(true);
        
        ListView lv = (ListView) findViewById(R.id.list_view);
        
        lv.requestLayout();
        
        int size = me.found_people.size();
        final String[] options;
        boolean matches;
        if (matches=size>0){
        	options = new String[size];
            
	        for (int x=0; keys.hasNext();x++){
				String key = keys.next();
				System.out.println(key+": "+me.found_people.get(key));
				options[x]=key;
			}
	        matches = true;
        } else {
        	options = new String[1];
        	options[0]="no matches found";
        }
	        
        
        if(options!=null){
        	lv.setAdapter(new ArrayAdapter<String>(FindPerson.this, 
        			R.layout.list_item, options));
        }
        
        lv.setTextFilterEnabled(true);
        lv.startAnimation(anim);
        if(matches){
	        lv.setOnItemClickListener(new OnItemClickListener(){
	
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					//start view user activity
					SharedPreferences spref=
							PreferenceManager.getDefaultSharedPreferences(me.context);
					SharedPreferences.Editor editor = spref.edit();
					editor.putString("lookup_stafflink", me.found_people.get(options[arg2]));
					editor.commit();
					startActivity(new Intent(FindPerson.this, ViewPerson.class));
				}
	        });
        }
	}
}
