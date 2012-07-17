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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

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
    	
    	
    	
    	client = medi_post.connect(username, password);
        me = new medi_person(this,1);
    	
        tv = (TextView) findViewById(R.id.entry);
        tv.setVisibility(View.VISIBLE);
        tv.requestLayout();
        
        tv.setEnabled(true);
        
        TranslateAnimation anim;
        anim = new TranslateAnimation(EditStatus.TRANS_START, 0, 0, 0);
	    anim.setDuration(EditStatus.TRANS_DUR);
	    anim.setFillAfter(true);
	    TableLayout lp = (TableLayout) me.context.findViewById(R.id.input_view);
        anim.setDuration(TRANS_DUR);
        anim.setFillAfter(true);
        //lp.setTextFilterEnabled(true);
        lp.startAnimation(anim);
        
        
        tv.setOnEditorActionListener(new OnEditorActionListener(){

			public boolean onEditorAction(TextView arg0, int keyCode, KeyEvent arg2) {
				
				dolookup();
			    return false;
			}
        	
        });
        tv.setOnKeyListener(new OnKeyListener(){

			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				System.out.println("onkeylistener:" + arg1);
				return false;
			}
        	
        });
        tv.setOnFocusChangeListener(new OnFocusChangeListener(){

			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				System.out.println("focus changed");
				
			}
        	
        });
        
        //http://www.meditech.com/employees/RATweb/RATweb.mps
        //VIEW.location.replace('RATWeb.mps?TYPE=Lookup&User='+value+'&Key='+lookup.value);
    	//HEAD.document.forms[0].User.value='';
        
	}
	
	public boolean dolookup(){
			
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
			
    	}
	
		return false;
	}
	protected void find(String user) {
		System.out.println(user);
		me.data = new HashMap<String, String>();
		me.data.put("TYPE", "Lookup");
		me.data.put("Key", "User");
		me.data.put("User", user);
		medi_post postme;
		postme = new medi_post(me.data,me.is_lss);
		postme.execute(me);
	}
	@Override
	public void onPostExecute(medi_person result) {
		// TODO Auto-generated method stub
		me = result;
		
		tv = (TextView) findViewById(R.id.entry);
		tv.setText("");
		Iterator<String> keys;
		int size;
		ListView lv = (ListView) findViewById(R.id.list_view);
		TranslateAnimation anim;
		if (me.found_people==null){
			size=0;
		} else {
			
			anim = new TranslateAnimation(EditStatus.TRANS_START, 0, 0, 0);
		    anim.setDuration(EditStatus.TRANS_DUR);
		    anim.setFillAfter(true);
		    TableLayout lp = (TableLayout) me.context.findViewById(R.id.table_view);
	        anim.setDuration(TRANS_DUR);
	        anim.setFillAfter(true);
	        //lp.setTextFilterEnabled(true);
	        lp.startAnimation(anim);
	        
		    lv = (ListView) findViewById(R.id.list_view);
		    lv.requestLayout();

	        lv.setTextFilterEnabled(true);
	        lv.startAnimation(anim);
		    size = me.found_people.size();
		}
        
        final String[] options;
        boolean matches;
        if (matches=size>0){
        	options = new String[size];
        	keys = me.found_people.keySet().iterator();
	        for (int x=0; keys.hasNext();x++){
				String key = keys.next();
				System.out.println(key+": "+me.found_people.get(key));
				options[x]=key;
			}
	        matches = true;
	        
        } else if(me.found_stafflink!=null){
        	SharedPreferences spref=
					PreferenceManager.getDefaultSharedPreferences(me.context);
			SharedPreferences.Editor editor = spref.edit();
			editor.putString("lookup_stafflink", me.found_stafflink);
			editor.commit();
			startActivity(new Intent(FindPerson.this, ViewPerson.class));
			options = null;
        } else {
        	options = new String[1];
        	options[0]="no matches found";
        }
	        
        
        if(options!=null){
        	lv.setAdapter(new ArrayAdapter<String>(FindPerson.this, 
        			R.layout.list_item, options));
        }
        
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
