package medi.mouse;

import org.acra.ErrorReporter;
import org.apache.http.client.HttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class medi_mouse_activity extends Activity {
	protected static final String TAG = "medi_mouse_activity";
	static final int SDK = android.os.Build.VERSION.SDK_INT;
	SharedPreferences spref;
	protected String text_color, background_color, box_color;
	protected String username, password, full_name, status;
	protected String menu;
	
	public abstract void onPostExecute(Object result);
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		spref=PreferenceManager.getDefaultSharedPreferences(this);
		username = spref.getString("user_name", "");
    	password = spref.getString("user_password","");
    	full_name = spref.getString("full_name", "");
    	status = spref.getString("status", "");
    	text_color = spref.getString("text-color","000000");
    	background_color = spref.getString("background-color","000000");
    	box_color = spref.getString("box-color","F2F200");
	
    	
    	
		
		setContentView(R.layout.main_view);
		View view = findViewById(R.id.MainFrame);
		
		view.setBackgroundColor(toColor(background_color));
    	
		spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.action_bar, menu);
        return true;
        
        //return super.onCreateOptionsMenu(menu);
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
        //  preparation code here
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed(){
    	super.onBackPressed();
    	/*
    	if (this instanceof MainApp){
    		super.onBackPressed();
    	} else {
			Intent intent = new Intent(this, MainApp.class);
		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    
		    startActivity(intent);
    	}
    	*/
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.d("ActionBar",item.getItemId()+":");
    	Intent intent;
        switch (item.getItemId()) {
        	case android.R.id.home:
        		intent = new Intent(this, MainApp.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                startActivity(intent);
                return true;
        	case R.id.signinout:
        		intent = new Intent(this,EditStatus.class);
        		startActivity(intent);
        		return true;
        		
            case R.id.settings:
            	intent = new Intent(this, EditPreferences.class);
            	startActivity(intent);
                return true;
            case R.id.maps:
            	intent = new Intent(this, FacilityViewerActivity.class);
            	intent.putExtra("core_post", false);
            	startActivity(intent);
                return true;
            case R.id.bugreport:
            	bugreport();
            	
               	//alertDialogView.requestLayout();
            	return true;
        }
        return false;
    }
    
    protected static int toColor(String color){
		int t = color.indexOf("#");
		if(t != -1){
			color = color.substring(t+1);
		}
		//Integer.decode crashes with 8 byte strings
		int ret;
		if(color.length()==6){
			ret = Long.decode("0xFF"+color).intValue();
		} else if(color.length()==8) {
			ret = Long.decode("0x"+color).intValue();
		} else {
			ret = 0;
		}
		return ret;
	}
    
    protected TextView createTextView(){
		TextView ret = new TextView(this);
		ret.setTextColor(toColor(text_color));
		ret.setTextSize(15);
		return ret;
	}
    protected TextView createTextView(String label){
    	TextView ret = createTextView();
		ret.setText(label);
		return ret;
	}
    
	protected View createBoxView(){
		PaintDrawable mDrawable = new PaintDrawable();
        mDrawable.getPaint().setColor(toColor(box_color));
        mDrawable.setCornerRadius(7);
        
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,      
                LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0,0,0,10);
        
        LayoutInflater inflater = LayoutInflater.from(this);
		View ret = inflater.inflate(R.layout.box_view, null);
		ret.setPadding(15,5,15,5);
		
		//TODO: not introduced until api 16
		if(SDK < android.os.Build.VERSION_CODES.JELLY_BEAN) {
    		ret.setBackgroundDrawable(mDrawable);
    	} else {
    		ret.setBackground(mDrawable);
    	}
		ret.setLayoutParams(params);
		return ret;
	}
    protected DatePicker createDatePicker(){
    	DatePicker ret = new DatePicker(this);
    	return ret;
    }
    protected View createBoxView(View label){
    	View box = createBoxView();
    	TableLayout TableView = (TableLayout) box.findViewById(R.id.TableLayout);
    	TableView.addView(label);
    	return box;
    }
    protected EditText createEditText(){
    	EditText et = new EditText(this);
    	et.setTextColor(0xFF000000);
    	et.setBackgroundColor(0xFFFFFFFF);
    	return et;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //super.onConfigurationChanged(newConfig);
        //here you can handle orientation change
    }
    
	public void onSharedPreferenceChanged(SharedPreferences spref, String arg1) {  
	}
	protected void spinMouse(){
		LinearLayout ll = (LinearLayout)findViewById(R.id.FloatingLayout);
		ImageView mouse = new ImageView(this);
		mouse.setVisibility(View.VISIBLE);
		mouse.setBackgroundResource(R.drawable.medimouse);
		Animation mouseAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		mouse.startAnimation(mouseAnimation);
		ll.addView(mouse);
	}
	protected void stopMouse(){
		LinearLayout ll = (LinearLayout)findViewById(R.id.FloatingLayout);
		ll.removeAllViews();
	}
	protected void WhatsNew(){
		LinearLayout ll = (LinearLayout)findViewById(R.id.WhatsNew);
		ImageView info = new ImageView(this);
		info.setVisibility(View.VISIBLE);
		info.setBackgroundResource(R.drawable.info);
		ll.addView(info);
		info.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				HelpAbout();
			}
			
		});
	}
	private void HelpAbout(){
    	LayoutInflater inflater = LayoutInflater.from(this);

        View alertDialogView = inflater.inflate(R.layout.alert_dialog_layout, null);

        WebView myWebView = (WebView) alertDialogView.findViewById(R.id.DialogWebView);
        //String webView = getString(R.string.help_about_html);
        //Log.d(TAG,webView);
        //myWebView.loadData(webView, "text/html", "utf-8");
        myWebView.loadUrl("file:///android_asset/help_about.html");  
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogView);
       
       
       builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }
	private void bugreport(){
		LayoutInflater inflater = LayoutInflater.from(this);

        View bugreport = inflater.inflate(R.layout.bugreport, null);
        final EditText description = (EditText) bugreport.findViewById(R.id.bugDescription);
        //String webView = getString(R.string.help_about_html);
        //Log.d(TAG,webView);
        //myWebView.loadData(webView, "text/html", "utf-8");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Button report = (Button) bugreport.findViewById(R.id.send);
        builder.setView(bugreport);
        report.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				PackageInfo pInfo;
				String version = "";
				try {
					pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
					version = pInfo.versionName;
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String user_content = description.getText().toString();
				String body = 
						"MediMouse version: \t"+version+"\n"+
						"SDK: \t" + SDK+"\n"+
						"description: "+user_content;
				sendEmail(body);
			}});
       
       builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
	}
	private void sendEmail(String body){
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"chewnoill@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "MediMouse - bug report");
		i.putExtra(Intent.EXTRA_TEXT   , body);
		try {
		    startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
	}
}
