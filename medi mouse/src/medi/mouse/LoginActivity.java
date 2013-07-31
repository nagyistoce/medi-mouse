package medi.mouse;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

public class LoginActivity extends FragmentActivity implements LoginFragment.ActivityCallback{
	private static final String TAG = "LoginActivty";
	private static SharedPreferences spref;
	private ProgressDialog mDialog_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG,"onCreate");
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(savedInstanceState==null){
			

			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			LoginFragment fragment = new LoginFragment();
			fragmentTransaction.replace(R.id.main, fragment);
			fragmentTransaction.commit();
			 
		
		} 
	}
	
	private void showLoading(){
		mDialog_loading = new ProgressDialog(this);
		mDialog_loading.setMessage("Loading Schedule...");
		mDialog_loading.setCancelable(false);
		mDialog_loading.show();
	}
	private void dismissLoading(){
		if(mDialog_loading!=null){
			mDialog_loading.dismiss();
			mDialog_loading = null;
		}
	}

	@Override
	public String getUsername() {
		return spref.getString("username","");
	}

	@Override
	public String getPassword() {
		return spref.getString("password","");
	}

	@Override
	public void setUsername(String username) {
		Editor editor = spref.edit();
        editor.putString("username",username);
    	editor.commit();
		
		
	}

	@Override
	public void setPassword(String password) {
		Editor editor = spref.edit();
		editor.putString("password",password);
    	editor.commit();
		
	}

	@Override
	public void login() {
		showLoading();
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
						Toast.makeText(LoginActivity.this,
								error_msg,
								Toast.LENGTH_SHORT).show();
					} else {
						Editor editor = spref.edit();
						editor.putString("events",value.toString());
				    	editor.commit();
				    	finish();
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
}
