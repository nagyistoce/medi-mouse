package medi.mouse;


import java.util.ArrayList;
import org.acra.ErrorReporter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MedibugsActivity extends medi_mouse_activity implements OnSharedPreferenceChangeListener{

	TextView name_view;
	TextView status_view;
	TextView date_view;
	ImageView picture;
	String status_message="";
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        
       super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.view_user);
        //medi_post.setConnectionManager();
        
        
        //------------------------------------------------------------------------------------------
        //establish connection to server
        
        
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	boolean enable_core = spref.getBoolean("enable_core", true);
    	
    	spref.registerOnSharedPreferenceChangeListener(this);
    	
    	//client = medi_post.connect(username, password);
		
    	me = new medi_person(this);
    	
    	//create list of linear layouts
        
        LinearLayout ll = (LinearLayout) findViewById(R.id.options_menu_view);
        ll.removeAllViews();
        View v = this.getLayoutInflater().inflate(R.layout.menu, null);
        TextView tv = (TextView) v.findViewById(R.id.name);
        tv.setText("Sign In/Out");
        tv.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				startActivity(new Intent(MedibugsActivity.this,EditStatus.class));
			}});
        ll.addView(v, 0);
        
        v = this.getLayoutInflater().inflate(R.layout.menu, null);
        tv = (TextView) v.findViewById(R.id.name);
        /* no longer supported in coretrax
        tv.setText("Find Person");
        tv.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				startActivity(new Intent(MedibugsActivity.this,FindPerson.class));
			}});
        ll.addView(v, 1);
        */
        v = this.getLayoutInflater().inflate(R.layout.menu, null);
        tv = (TextView) v.findViewById(R.id.name);
        
        tv.setText("Refresh");
        tv.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				MedibugsActivity.this.reload();
			}});
        ll.addView(v, 1);
        
        Log.d("UI","-------------");
        final ArrayList<View> views = new ArrayList<View>();
        views.add(ll);
        Log.d("UI","views size:"+views.size());
        //screen refresh
        if(enable_core){
        	core_post cpost = new core_post(false);
        	cpost.execute(me);
        }
    	
    	//----------------------------------------------------------------------------------------
    	    	

    	
    }
        
	@Override
    public void onDestroy(){
    	//disconnect
    	//medi_post.disconnect(client);
    	super.onDestroy();
    }
 
    /**
     * full reload.  Creates new connection with the server and requests current state.
     */
    public void reload(){
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(MedibugsActivity.this);
    	
		String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	
    	coretrax_post trax_post;
    	
    	me.username=username;
		me.password=password;
		
		trax_post = new coretrax_post();
		trax_post.execute(me);
		reload(1);
	}
    @Override
    public void onResume(){
    	super.onResume();
    	reload(1);
    }
    /**
     * reloads screen fields with saved data
     * @param t - ignored
     */
    public void reload(int t){
    	//just refresh screen
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
        me.full_name = spref.getString("full_name", "");
        me.status = spref.getString("status", "");
        me.date = spref.getString("date", "");
    	
    	name_view = (TextView) findViewById(R.id.name_view);
        status_view = (TextView) findViewById(R.id.status_view);
        date_view = (TextView) findViewById(R.id.date_view);
        picture = (ImageView) findViewById(R.id.picture_view);
        
    	name_view.setText(me.full_name);
    	status_view.setText(me.status);
    	date_view.setText(me.date);
    	
    	status_view.refreshDrawableState();

    	//ImageView picture = (ImageView) context.findViewById(R.id.picture_view);
    	/*
		WebView web_view = (WebView) findViewById(R.id.webview);
		web_view.setVisibility(View.VISIBLE);
		web_view.loadDataWithBaseURL(medi_post.BASE_URL, me.webview, 
				"text/html", "", medi_post.SITE);
		*/
		
    }
    
    /**
	 * create options menu
	 * 
	 * 
	 */
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "settings");
        menu.add(Menu.NONE, 1, 1, "about");
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this, EditPreferences.class));
                return true;
            case 1:
                LayoutInflater inflater = LayoutInflater.from(MedibugsActivity.this);

                // error here
                View alertDialogView = inflater.inflate(R.layout.alert_dialog_layout, null);

                WebView myWebView = (WebView) alertDialogView.findViewById(R.id.DialogWebView);
                
                myWebView.loadUrl("file:///android_asset/help_about.html");  
                AlertDialog.Builder builder = new AlertDialog.Builder(MedibugsActivity.this);
                Button report = (Button) alertDialogView.findViewById(R.id.crash_report);
                builder.setView(alertDialogView);
                report.setOnClickListener(new OnClickListener(){

					public void onClick(View arg0) {
						ErrorReporter.getInstance().handleException(
								new Exception("haha, I found an error"));
						
					}});
               
               builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
               	//alertDialogView.requestLayout();
            	return true;
        }
        return false;
    }

	public void onSharedPreferenceChanged(SharedPreferences spref, String arg1) {  
		
		boolean is_lss = spref.getBoolean("is_lss", false);
    	
		me.is_lss = is_lss;
	}

	@Override
	public void onPostExecute(medi_person result) {
		me = result;

		reload(1);
		
	}
    
}

class unauthorized extends Exception {
	private static final long serialVersionUID = 1L;
	
}
