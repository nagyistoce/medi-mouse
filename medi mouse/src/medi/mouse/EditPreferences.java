package medi.mouse;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class EditPreferences extends medi_mouse_activity {
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_auth);
		//PreferenceManager.setDefaultValues(this,R.xml.prefs, false);
		

    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	boolean enable_core = spref.getBoolean("enable_core", true);
    	
    	TextView username_view= (TextView) findViewById(R.id.username_view);
    	TextView password_view= (TextView) findViewById(R.id.password_view);
        CheckBox enable_core_view= (CheckBox) findViewById(R.id.enable_core_view);
            	username_view.setText(username);
        password_view.setText(password);
        enable_core_view.setChecked(enable_core);
        
		//addPreferencesFromResource(R.xml.prefs);
       
	}
	@Override
	public void onBackPressed(){
		commitChanges();
		Intent intent = new Intent(this, MedibugsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    
	}
	@Override
	public void onPause(){
		commitChanges();
    	super.onPause();
	}
	
	private void commitChanges(){
		TextView username_view= (TextView) findViewById(R.id.username_view);
    	TextView password_view= (TextView) findViewById(R.id.password_view);
        CheckBox enable_core_view= (CheckBox) findViewById(R.id.enable_core_view);
        String username="";
        for(int x = 0;x<username_view.getText().length();x++){
        	username+=username_view.getText().charAt(x);
        }
        String password="";
        for(int x = 0;x<password_view.getText().length();x++){
        	password+=password_view.getText().charAt(x);
        }		
        
        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = spref.edit();
        
        
    	editor.putString("user_name", username);
    	editor.putString("user_password",password);
    	editor.putBoolean("enable_core", enable_core_view.isChecked());
    	editor.commit();
    	Log.d("EditPreferences","committing changes");
	}
	@Override
	public void onDetachedFromWindow(){
		commitChanges();
	}
	@Override
	public void onPostExecute(Object result) {
		// TODO Auto-generated method stub
		
	}

}


