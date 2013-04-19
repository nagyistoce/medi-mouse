package medi.mouse;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditPreferences extends medi_mouse_activity {
	
	private EditText usernameView;
	private TextView passwordView;
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main_view);
		//PreferenceManager.setDefaultValues(this,R.xml.prefs, false);
    	
    	buildUI();
		
		//addPreferencesFromResource(R.xml.prefs);
       
	}
	
	private void buildUI(){
		LinearLayout ll = (LinearLayout) findViewById(R.id.MainLinearLayout);
		ll.removeAllViews();
		
		LayoutInflater inflater = LayoutInflater.from(this);
        View main = inflater.inflate(R.layout.main, null);
		
		ll.addView(main);
		LinearLayout MainView = (LinearLayout) main.findViewById(R.id.MainLinearLayout);
		
		MainView.removeAllViews();
		View info = createBoxView();
		TableLayout TableView = (TableLayout) info.findViewById(R.id.TableLayout);
		usernameView = createEditText();
		usernameView.setText(username);
		passwordView = createEditText();
		passwordView.setText(password);
		passwordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
		
		View usernameLabel = createTextView("username");
		View passwordLabel = createTextView("password");
		
		TableView.addView(usernameLabel);
		TableView.addView(usernameView);
		TableView.addView(passwordLabel);
		TableView.addView(passwordView);
		MainView.addView(info);
		
		//------------------------------------------------------------------------
		//text color
		View colors = createBoxView();
		TableView = (TableLayout) colors.findViewById(R.id.TableLayout);
		TextView textViewLabel = createTextView("Text Color");
		TableView.addView(textViewLabel);
		
		WebView text_color_view = new WebView(this);
		text_color_view.addJavascriptInterface(new ColorInterface(this,"text-color",text_color), "Android");
		text_color_view.loadUrl("file:///android_asset/color_picker.html");  
		WebSettings webSettings = text_color_view.getSettings();
		webSettings.setJavaScriptEnabled(true);
		TableView.addView(text_color_view);
		
		MainView.addView(colors);
		//------------------------------------------------------------------------
		//box color		
		colors = createBoxView();
		TableView = (TableLayout) colors.findViewById(R.id.TableLayout);
		textViewLabel = createTextView("Box Color");
		TableView.addView(textViewLabel);
		
		text_color_view = new WebView(this);
		text_color_view.addJavascriptInterface(new ColorInterface(this,"box-color",box_color), "Android");
		text_color_view.loadUrl("file:///android_asset/color_picker.html");  
		webSettings = text_color_view.getSettings();
		webSettings.setJavaScriptEnabled(true);
		TableView.addView(text_color_view);
		
		MainView.addView(colors);
		//------------------------------------------------------------------------
		//background color		
		colors = createBoxView();
		TableView = (TableLayout) colors.findViewById(R.id.TableLayout);
		textViewLabel = createTextView("Background Color");
		TableView.addView(textViewLabel);
		
		text_color_view = new WebView(this);
		text_color_view.addJavascriptInterface(new ColorInterface(this,"background-color",background_color), "Android");
		text_color_view.loadUrl("file:///android_asset/color_picker.html");  
		webSettings = text_color_view.getSettings();
		webSettings.setJavaScriptEnabled(true);
		TableView.addView(text_color_view);
		
		MainView.addView(colors);
	}
	@Override
	public void onBackPressed(){
		commitChanges();
		super.onBackPressed();
    
	}
	@Override
	public void onPause(){
		commitChanges();
    	super.onPause();
	}
	
	private void commitChanges(){
		username = usernameView.getText().toString();
		password = passwordView.getText().toString();
        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = spref.edit();
        
        
    	editor.putString("user_name", username);
    	editor.putString("user_password",password);
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
	class ColorInterface {
		static final String TAG = "jsconsole";
	    Context mContext;
	    String mPref;
	    String mVal;
	    /** Instantiate the interface and set the context 
	     * @param prefname, String defaultValue */
	    ColorInterface(Context c, String prefname, String defaultValue) {
	        mContext = c;
	        mPref = prefname;
	        
	        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(mContext);
	        mVal = spref.getString(mPref,defaultValue);
	    }

	    @JavascriptInterface
	    public String getDefaultColor() {
	    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(mContext);
	        mVal = spref.getString(mPref,mVal);
	        return mVal;
	    }
	    
	    @JavascriptInterface
	    public void saveDefaultColor(String value) {
	    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(mContext);
	        Editor editor = spref.edit();
	        editor.putString(mPref,value);
	    	editor.commit();
	    }
	    @JavascriptInterface
	    public String getBackgroundColor(){
	    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(mContext);

	    	return spref.getString("box-color","F2F200");
	        
	    }
	    @JavascriptInterface
	    public void redrawUI(){
	    	EditPreferences.this.buildUI();
	        
	    }
	}

}



