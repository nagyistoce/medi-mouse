package medi.mouse;


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.TextView;

public class EditPreferences extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_auth);
		//PreferenceManager.setDefaultValues(this,R.xml.prefs, false);
		

    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	boolean doreload = spref.getBoolean("reload_onresume", true);
    	
    	TextView username_view= (TextView) findViewById(R.id.username_view);
    	TextView password_view= (TextView) findViewById(R.id.password_view);
        CheckBox doreload_view= (CheckBox) findViewById(R.id.reload_view);
    	
    	username_view.setText(username);
        password_view.setText(password);
        doreload_view.setChecked(doreload);
		//addPreferencesFromResource(R.xml.prefs);
       
	}
	@Override
	public void onBackPressed(){
    	TextView username_view= (TextView) findViewById(R.id.username_view);
    	TextView password_view= (TextView) findViewById(R.id.password_view);
        CheckBox doreload_view= (CheckBox) findViewById(R.id.reload_view);
        String username="";
        for(int x = 0;x<username_view.getText().length();x++){
        	username+=username_view.getText().charAt(x);
        }
        String password="";
        for(int x = 0;x<password_view.getText().length();x++){
        	password+=password_view.getText().charAt(x);
        }		
        
		
		
		System.out.println("saving preferences");
		SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
		
		
    	Editor editor = spref.edit();
    	editor.putString("user_name", username);
    	editor.putString("user_password",password);
    	editor.putBoolean("reload_onresume", doreload_view.isChecked());
    	editor.commit();
    	
    	
	
		super.onBackPressed();
		
	}

}


