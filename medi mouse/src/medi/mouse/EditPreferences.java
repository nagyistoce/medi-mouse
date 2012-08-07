package medi.mouse;


import android.app.Activity;
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

public class EditPreferences extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_auth);
		//PreferenceManager.setDefaultValues(this,R.xml.prefs, false);
		

    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	boolean doreload = spref.getBoolean("reload_onresume", false);
    	boolean is_lss = spref.getBoolean("is_lss", false);
    	
    	final String lss_core_username = spref.getString("lss_core_username", "");
    	final String lss_core_password = spref.getString("lss_core_password", "");
    	
    	TextView username_view= (TextView) findViewById(R.id.username_view);
    	TextView password_view= (TextView) findViewById(R.id.password_view);
        CheckBox doreload_view= (CheckBox) findViewById(R.id.reload_view);
        final CheckBox is_lss_view= (CheckBox) findViewById(R.id.is_lss_view);
        
        final LinearLayout lss_only_view = (LinearLayout) findViewById(R.id.lss_only);
        Log.d("EditPreferences","lss ui 1"+is_lss_view.isChecked());
        if (is_lss){
        	
        	lss_only_view.setVisibility(View.VISIBLE);
			TableLayout tl = (TableLayout) findViewById(R.id.lss_core_view);
			tl.removeAllViews();
			
			TextView label = new TextView(EditPreferences.this);
			label.setText("LSS core credentials");
			
			tl.addView(label, 0);
			EditText et = new EditText(EditPreferences.this);
			et.setText(lss_core_username);
			
			tl.addView(et, 1);
			

			et = new EditText(EditPreferences.this);
			et.setText(lss_core_password);
			//et.setTransformationMethod(new PasswordTransformationMethod());
			//et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			tl.addView(et, 2);
        }else{
        	lss_only_view.setVisibility(View.GONE);
        }
        is_lss_view.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				if (is_lss_view.isChecked()){
					lss_only_view.setVisibility(View.VISIBLE);
					TableLayout tl = (TableLayout) findViewById(R.id.lss_core_view);
					tl.removeAllViews();
					
					TextView label = new TextView(EditPreferences.this);
					label.setText("LSS core credentials");
					
					tl.addView(label, 0);
					EditText et = new EditText(EditPreferences.this);
					et.setText(lss_core_username);
					
					tl.addView(et, 1);
					

					et = new EditText(EditPreferences.this);
					et.setText(lss_core_password);
					//et.setTransformationMethod(new PasswordTransformationMethod());
					//et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					tl.addView(et, 2);
				} else {
					lss_only_view.setVisibility(View.GONE);
				}
				
					
				
			}
        	
        });
        
    	username_view.setText(username);
        password_view.setText(password);
        doreload_view.setChecked(doreload);
        is_lss_view.setChecked(is_lss);
		//addPreferencesFromResource(R.xml.prefs);
       
	}
	@Override
	public void onBackPressed(){
    	TextView username_view= (TextView) findViewById(R.id.username_view);
    	TextView password_view= (TextView) findViewById(R.id.password_view);
        CheckBox doreload_view= (CheckBox) findViewById(R.id.reload_view);
        CheckBox is_lss_view= (CheckBox) findViewById(R.id.is_lss_view);
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
        
        TableLayout tl = (TableLayout) findViewById(R.id.lss_core_view);
        if(tl.getChildCount()==3){
        	EditText et = (EditText) tl.getChildAt(1);
        	String lss_core_username = et.getText().toString();
        	String lss_core_password = ((EditText) tl.getChildAt(2)).getText().toString();
        	
        	editor.putString("lss_core_username", lss_core_username);
        	editor.putString("lss_core_password", lss_core_password);
        }
		
    	editor.putString("user_name", username);
    	editor.putString("user_password",password);
    	editor.putBoolean("reload_onresume", doreload_view.isChecked());
    	editor.putBoolean("is_lss", is_lss_view.isChecked());
    	editor.commit();
    	
    	
	
		super.onBackPressed();
		
	}

}


