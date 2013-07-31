package medi.mouse;


import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends FragmentActivity 
		implements
		ScheduleFragment.ActivityCallback,
		BigEventFragment.ActivityCallback,
		InOutFragment.ActivityCallback,
		PeopleFragment.ActivityCallback,
		NotesFragment.ActivityCallback,
		HelpAboutFragment.ActivityCallback{
	private static final String TAG = "MainActivity";
	private SharedPreferences spref;
	private String username,password;
	private ScheduleFragment mScheduleFragment;
	private InOutFragment mInOutFragment;
	private PeopleFragment mPeopleFragment;
	private ProgressDialog mDialog_loading;
	private NotesFragment mNotesFragment;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		username = spref.getString("username", "");
    	password = spref.getString("password","");
    	if(mScheduleFragment==null){
    		mScheduleFragment = new ScheduleFragment();
    	}
    	if(mInOutFragment==null){
    		mInOutFragment = new InOutFragment();
    	}
    	if(mPeopleFragment==null){
    		mPeopleFragment = new PeopleFragment();
    	}
    	if(mNotesFragment==null){
    		mNotesFragment = new NotesFragment();
    	}
    	
		if(savedInstanceState==null){
			Log.d(TAG,"new activity");
			if(username.equals("")||password.equals("")){
				loadLoginActivity();
			}else{
				
				loadMainActivity();
				loadScheduleFragment();
			}
		} else {
			loadMainActivity();
		}
			
	}
	@Override
	protected void onResume(){
		Log.d(TAG,"onResume");
		super.onResume();
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		
	}
	public void showLoading(String message){
		mDialog_loading = new ProgressDialog(this);
		mDialog_loading.setMessage(message);
		mDialog_loading.setCancelable(false);
		mDialog_loading.show();
	}
	public void dismissLoading(){
		if(mDialog_loading!=null){
			mDialog_loading.dismiss();
			mDialog_loading = null;
		}
	}
	
	private void loadLoginActivity(){
		Intent intent = new Intent(this, LoginActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	startActivity(intent);
	}
	private void loadMainActivity(){
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.main_view, null);
		FrameLayout fl = (FrameLayout) findViewById(R.id.main);
		fl.addView(view);
		
		Button inout = (Button) view.findViewById(R.id.in_out);
		inout.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				loadInOutFragment(1);
			}
			
		});
		Button schedule = (Button)view.findViewById(R.id.schedule);
		schedule.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				loadScheduleFragment();
			}
			
		});
		
		Button people = (Button)view.findViewById(R.id.people);
		people.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				loadPeopleFragment();
			}
			
		});
		
		Button notes = (Button)view.findViewById(R.id.notes);
		notes.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				loadNotesFragment();
			}
			
		});
		
		Button logout = (Button)view.findViewById(R.id.logout);
		logout.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getActionMasked()==MotionEvent.ACTION_UP){
					setUsername("");
					setPassword("");
					loadLoginActivity();
				}
				return true;
			}
			
		});
		
		Button help_about = (Button)view.findViewById(R.id.help_about);
		help_about.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getActionMasked()==MotionEvent.ACTION_UP){
					loadHelpAboutFragment();
				}
				return true;
			}
			
		});
	}

	private void loadNotesFragment(){
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_content, mNotesFragment);
		fragmentTransaction.commit();
	}
	private void loadScheduleFragment(){
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_content, mScheduleFragment);
		fragmentTransaction.commit();
	}
	private void loadInOutFragment(int try_num){
		String menu = spref.getString("menu","");
		if(menu.equals("")){
			//no menu, get one
			getMenu(try_num+1);
		}else{
			//TODO:
			//sanity checking menu
			//menu version number?
			Bundle args = new Bundle();
			args.putString("menu", menu);
			mInOutFragment = new InOutFragment();
			mInOutFragment.setArguments( args );
			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.replace(R.id.main_content, mInOutFragment);
			fragmentTransaction.commit();
		}
	}
	private void loadHelpAboutFragment(){
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_content, new HelpAboutFragment());
		fragmentTransaction.commit();
	}
	private void loadPeopleFragment(){
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_content, mPeopleFragment);
		fragmentTransaction.commit();
	}
	private void loadPeopleFragment(String info){
		
		Bundle args = new Bundle();
		args.putString("person", info);
		mPeopleFragment = new PeopleFragment();
		mPeopleFragment.setArguments( args );
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_content, mPeopleFragment);
		fragmentTransaction.commit();
	
	}
	@Override
	public String getUsername() {
		return spref.getString("username","");
	}

	@Override
	public String getPassword() {
		return spref.getString("password","");
	}

	public void setUsername(String username) {
		Editor editor = spref.edit();
        editor.putString("username",username);
    	editor.commit();
		
		
	}

	public void setPassword(String password) {
		Editor editor = spref.edit();
		editor.putString("password",password);
    	editor.commit();
		
	}
	
	@Override
	public void getSchedule(final ScheduleFragment fragment) {
		showLoading("Getting schedule...");
		BackendInterface post = new BackendInterface(new BackendCallback(){
			@Override
			public void onPostExecute(JSONObject value) {
				Log.d(TAG,"onPostExecute: "+value.toString());
				dismissLoading();
				try {
					if(value.has("error")){
						String error_msg = value.getString("error");
						if (value.has("detail")){
							error_msg += ": "+value.getString("detail");
						}
						Log.d(TAG,error_msg);
						Toast.makeText(MainActivity.this,
								error_msg,
								Toast.LENGTH_SHORT).show();
					} else {
						Editor editor = spref.edit();
						editor.putString("events",value.toString());
				    	editor.commit();
				    	fragment.loadSchedule(value);
					} 
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}});
		HashMap<String,String> hash_data = new HashMap<String,String>();
		hash_data.put("type","list_events");
		hash_data.put("username",getUsername());
		hash_data.put("password",getPassword());
		
		JSONObject data = new JSONObject(hash_data);
		Log.d(TAG,"Sending message: "+data.toString());
		post.execute(data);
		
	}
	
	
	@Override
	public SharedPreferences getSharedPreferences() {
		if(spref==null){
			spref = PreferenceManager.getDefaultSharedPreferences(this); 
		}
		return spref;
	}
	@Override
	public void loadPerson(String name) {
		showLoading("looking up person...");
		BackendInterface post = new BackendInterface(new BackendCallback(){
			@Override
			public void onPostExecute(JSONObject value) {
				Log.d(TAG,"onPostExecute: "+value.toString());
				dismissLoading();
				try {
					if(value.has("error")){
						String error_msg = value.getString("error");
						if (value.has("detail")){
							error_msg += ": "+value.getString("detail");
						}
						Log.d(TAG,error_msg);
						Toast.makeText(MainActivity.this,
								error_msg,
								Toast.LENGTH_SHORT).show();
					} else {
						loadPeopleFragment(value.toString());
					} 
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}});
		HashMap<String,String> hash_data = new HashMap<String,String>();
		hash_data.put("type","lookup_person");
		hash_data.put("lookup_key",name);
		hash_data.put("username",getUsername());
		hash_data.put("password",getPassword());
		
		JSONObject data = new JSONObject(hash_data);
		post.execute(data);
	}
	
	public void getMenu(final int try_num) {
		
		showLoading("loading menu...");
		BackendInterface post = new BackendInterface(new BackendCallback(){
			@Override
			public void onPostExecute(JSONObject value) {
				dismissLoading();
				if(value.has("error")){
					String error_msg;
					error_msg = value.optString("error","");
					if (value.has("detail")){
						error_msg += ": "+value.optString("detail");
					}
					Log.d(TAG,error_msg);
					Toast.makeText(MainActivity.this,
							error_msg,
							Toast.LENGTH_SHORT).show();
				} else {
					Editor editor = spref.edit();
					editor.putString("menu",value.toString());
			    	editor.commit();
			    	loadInOutFragment(try_num);
				}
				
			}
		});
		HashMap<String,String> hash_data = new HashMap<String,String>();
		hash_data.put("type","get_menu");
		JSONObject data = new JSONObject(hash_data);
		post.execute(data);
		
		
	}
	@Override
	public void saveStatus(ArrayList<String> path, HashMap<String, String> extra) {
		
		
		showLoading("setting status...");
		BackendInterface post = new BackendInterface(new BackendCallback(){
			@Override
			public void onPostExecute(JSONObject value) {
				dismissLoading();
				if(value.has("error")){
					String error_msg;
					error_msg = value.optString("error","");
					if (value.has("detail")){
						error_msg += ": "+value.optString("detail");
					}
					Log.d(TAG,error_msg);
					Toast.makeText(MainActivity.this,
							error_msg,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MainActivity.this,
							"status saved",
							Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		HashMap<String,Object> hash_data = new HashMap<String,Object>();
		hash_data.put("type","set_status");
		hash_data.put("username",getUsername());
		hash_data.put("password",getPassword());
		hash_data.put("path",new JSONArray(path));
		if(extra!=null && extra.size()>0){
			hash_data.put("extra",new JSONObject(extra));
		}
		JSONObject data = new JSONObject(hash_data);
		//Log.d(TAG,"saving status: "+data.toString());
		post.execute(data);
		 
		 
		
	}
	@Override
	public JSONObject getQuickSaves() {
		String scustom_status = spref.getString("custom_status","{}");
		JSONObject jcustom_status = new JSONObject();
		try {
			jcustom_status = new JSONObject(scustom_status);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jcustom_status;
	}
	@Override
	public void setQuickSave(ArrayList<String> path,
			HashMap<String, String> extra, 
			String label) {
		String scustom_status = spref.getString("custom_status","{}");
		try {
			JSONObject jcustom_status = new JSONObject(scustom_status);
			JSONObject new_jcustom_status = new JSONObject();
			new_jcustom_status.put("path",new JSONArray(path));
			if(extra!=null && extra.size()>0){
				JSONObject ext = new JSONObject(extra);
				Log.d(TAG,"setQuickSave:extra "+ext.toString());
				new_jcustom_status.put("extra",ext);
			}
			jcustom_status.put(label,new_jcustom_status);
			Editor editor = spref.edit();
			editor.putString("custom_status",jcustom_status.toString());
	    	editor.commit();
	    	Toast.makeText(MainActivity.this,
					"quick save status saved",
					Toast.LENGTH_SHORT).show();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	@Override
	public void delQuickSave(String label){
		String scustom_status = spref.getString("custom_status","{}");
		JSONObject jcustom_status;
		try {
			jcustom_status = new JSONObject(scustom_status);
		
			if(jcustom_status.has(label)){
				jcustom_status.remove(label);
				Editor editor = spref.edit();
				editor.putString("custom_status",jcustom_status.toString());
		    	editor.commit();
		    	Toast.makeText(MainActivity.this,
						"quick save status removed",
						Toast.LENGTH_SHORT).show();
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void getStatus(final InOutFragment fragment) {

		showLoading("getting status...");
		BackendInterface post = new BackendInterface(new BackendCallback(){

			@Override
			public void onPostExecute(JSONObject value) {
				dismissLoading();
				if(value.has("error")){
					String error_msg;
					error_msg = value.optString("error","");
					if (value.has("detail")){
						error_msg += ": "+value.optString("detail");
					}
					Log.d(TAG,error_msg);
					Toast.makeText(MainActivity.this,
							error_msg,
							Toast.LENGTH_SHORT).show();
				} else {
					fragment.DisplayStatus(value);
				}
			}
			
		});
		HashMap<String,Object> hash_data = new HashMap<String,Object>();
		hash_data.put("type","get_status");
		hash_data.put("username",getUsername());
		hash_data.put("password",getPassword());
		
		
		
		post.execute(new JSONObject(hash_data));
	}
	
	public String getVersion(){
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	@Override
	public void getNotes(NotesFragment fragment) {
		// TODO Auto-generated method stub
		
	}
}
