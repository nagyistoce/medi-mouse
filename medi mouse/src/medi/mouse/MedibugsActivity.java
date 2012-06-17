package medi.mouse;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.SslError;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import medi.mouse.EditStatus.menu_node;

import org.apache.http.impl.client.DefaultHttpClient;

 
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
        setContentView(R.layout.set_status);
                
        
        
        //the date spinners
        //------------------------------------------------------------------------------------------
        //establish connection to server
        
        
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	boolean reload = spref.getBoolean("reload_onresume", false);
    	
    	spref.registerOnSharedPreferenceChangeListener(this);
    	
    	client = medi_post.connect(username, password);
		
    	me = new medi_person(this);
    	if (reload){
    		//onResume handles full reloads
    		reload();
    	}else{
    		reload(1);
    	}
    	ListView lv = (ListView) findViewById(R.id.list_view);
        
        lv.requestLayout();
        String[] options = {"Sign In/Out",
        		"Find Person",
        		"Refresh"};
        lv.setAdapter(new ArrayAdapter<String>(this, 
        			R.layout.list_item, options));
        lv.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(MedibugsActivity.this.me.hasStafflink()){
					
					switch (arg2) {
					case 0:
						//Sign In/Out
						startActivity(new Intent(MedibugsActivity.this, EditStatus.class));
						break;
					case 1:
						//Find Person
						MedibugsActivity.this.getLookup();
						startActivity(new Intent(MedibugsActivity.this, FindPerson.class));
						break;
					case 2:
						//Refresh
						MedibugsActivity.this.reload();
					}
					
				}else {
					Toast.makeText(MedibugsActivity.this, "Please wait for initial load to complete", 
							Toast.LENGTH_SHORT).show();
				}
					
			}});

    	
    	/*
    	picture.setOnClickListener(new picture_listener());
    	File imageFile = new File(me.imgfile);
    	
    	System.out.println("imgfile: "+me.imgfile);
		if(imageFile.exists()){
			
			System.out.println("displaying picture");
			//Bitmap myBitmap = BitmapFactory.decodeFile(me.imgfile);
			
			//picture.setImageBitmap(myBitmap);
		}
		*/
    	
    }
        
    protected void getLookup() {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void onDestroy(){
    	//disconnect
    	//medi_post.disconnect(client);
    	super.onDestroy();
    }
    /**
     * picture_listener
     * @author will
     * Does not work!  There is something wrong with meditech's webserver
     * and my download requests are not working.  I kept getting file not
     * found exceptions or html files instead of the jpg I requested.  
     * 
     * I tested with another remote jpg file and it pulled in just fine,
     * so thats why I think its a problem with the server.
     */
    class picture_listener implements OnClickListener{

		public void onClick(View arg0) {
			if(me.imglink!=null){
				/*
				String filelink = me.username+".jpg";
				ImageManager.DownloadFromUrl(medi_post.SITE_IMG_DIR+"/"+me.imglink,filelink);
				SharedPreferences spref=
						PreferenceManager.getDefaultSharedPreferences(me.context);
				SharedPreferences.Editor editor = spref.edit();
				editor.putString("imgfile", "/sdcard/data/medi.mouse/"+filelink);
				editor.commit();
				File imageFile = new File(filelink);
				if(imageFile.exists()){
					Bitmap myBitmap = BitmapFactory.decodeFile("/sdcard/data/medi.mouse/"+filelink);
					ImageView myImage = (ImageView) findViewById(R.id.picture_view);
					myImage.setImageBitmap(myBitmap);
				}
				*/
			}
		}
    	
    }

    public void reload(){
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(MedibugsActivity.this);
    	
		String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	System.out.println("user "+username);
    	//release lock when you close connection
    	me.network_lock = false;
    	client.getConnectionManager().shutdown();
    	client = medi_post.connect(username, password);
    	me.client=client;
    	medi_post postme;
    	
    	if (!me.hasStafflink()||
    			me.username!=username||
    			!me.network_auth){
    		me.username=username;
    		me.data = new HashMap<String, String>();
    		postme = new medi_post(me.data);
    		postme.execute(me);
    	} else {	
			me.secondaryLoad();
			postme = new medi_post(me.data);
	    	postme.execute(me);
    	}
	}
    @Override
    public void onResume(){
    	super.onResume();

    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	boolean doreload = spref.getBoolean("reload_onresume", true);
    	if (doreload){
    		System.out.println("full reload");
    		reload();
    	}else{
    		System.out.println("partial reload");
	    	reload(1);
	    	//set to invisible to avoid a full reload
	    	WebView web_view = (WebView) this.findViewById(R.id.webview);
	    	web_view.setVisibility(View.INVISIBLE);
    	}
    }
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
    	
    }    	
/*
    class submit_listener implements OnClickListener{

		public void onClick(View arg0) {
			boolean post = false;
			if(out_spinner.getSelectedItemPosition()!=0){
				
				me.out=(String) out_spinner.getSelectedItem();
				me.loc=me.bldg=me.in="";
				myDate date = inputDates.get(date_spinner.getSelectedItemPosition());

				me.date = date.human;
				me.YYYYmmdd = date.YYYYmmdd;
				post = true;
				System.out.println("-->out"+me.out);
			} else if (in_spinner.getSelectedItemPosition()!=0&&bldg_spinner.getSelectedItemPosition()!=0){
				
				
				me.bldg = (String) bldg_spinner.getSelectedItem();
				me.loc = me.in = (String) in_spinner.getSelectedItem();
				me.out="";
				
				post = true;
				System.out.println("-->in"+me.loc);
				System.out.println("-->in"+me.bldg);
			}
			System.out.println("in: "+in_spinner.getSelectedItemPosition());
			System.out.println("out: "+out_spinner.getSelectedItemPosition());
			System.out.println("post: "+post);
			if (post){
				
				me.submit(MedibugsActivity.this);
				
			}	
			//me.secondaryLoad();
			//medi_post postme = new medi_post(me.data);
	    	//postme.execute(me);
	    	
	    	
			
		}
    	
    }
  */  
	private class MyWebViewClient extends WebViewClient {
    	Activity activity;
    	public MyWebViewClient(Activity activity){
    		super();
    		this.activity=activity;
    	}
    	@Override
    	public void onReceivedHttpAuthRequest(WebView view,
    	        HttpAuthHandler handler, String host, String realm) {

        	SharedPreferences spref=MedibugsActivity.this.getPreferences(0);
        	String username = spref.getString("user_name", "");
        	String password = spref.getString("user_password","");
        		
    	    handler.proceed(username, password);
    	    
    	}
    @Override
	 public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		  handler.proceed() ;
		  }
	 
	 @Override
	 public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		 Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
	   	}
	 
	 @Override
     public boolean shouldOverrideUrlLoading( WebView view, String url )
     {
         return false;
     }

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
            	new AlertDialog.Builder(this)
            	  .setTitle(R.string.help_about).setMessage(R.string.help_about_message)
            	  .setPositiveButton(R.string.OK,
            	   new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
            	    
            	   }
            	    )
            	  .show();
            	return true;
        }
        return false;
    }

	public void onSharedPreferenceChanged(SharedPreferences spref, String arg1) {  
		String value = "reload_onresume";
		boolean equals = arg1.length()== value.length();
    	int len = arg1.length();
    	for (int x=0; x<len&&equals;x++){
    		if((arg1.charAt(x)!=(value.charAt(x)))){ equals=false;}
    	}
    	if(!equals){
			
			System.out.println(arg1);
			String username = spref.getString(arg1, "");
			value = "user_password";
	    	
	    	char[] t = arg1.toCharArray();
	    	
	    	equals = arg1.length()== value.length();
	    	len = arg1.length();
	    	//not sure why this is false
	    	//System.out.println(arg1+"==user_name? "+(arg1==value));
	    	for (int x=0; x<len&&equals;x++){
	    		if((arg1.charAt(x)!=(value.charAt(x)))){ equals=false;}
	    	}
	    	
			if(equals){
				//password has been updated
				//refresh
				reload();
			}
		}
	}
    
}

class unauthorized extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}