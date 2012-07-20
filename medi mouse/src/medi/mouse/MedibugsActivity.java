package medi.mouse;


import java.util.HashMap;

import org.acra.ErrorReporter;
import org.apache.http.conn.ManagedClientConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.http.SslError;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


 
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
    	boolean doreload = spref.getBoolean("reload_onresume", true);
    	
    	spref.registerOnSharedPreferenceChangeListener(this);
    	
    	client = medi_post.connect(username, password);
		
    	me = new medi_person(this);
    	//screen refresh
    	if (doreload){
    		//onResume handles full reloads
    		reload(1);
    	}else{
    		//System.out.println("initial load");
    		//reload();
    	}
    	
    	
    	//----------------------------------------------------------------------------------------
    	
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
				//client.getConnectionManager().shutdown();
				
				me.network_lock=false;
				if(MedibugsActivity.this.me.hasStafflink()){
					
					switch (arg2) {
					case 0:
						//Sign In/Out
						startActivity(new Intent(MedibugsActivity.this, EditStatus.class));
						break;
					case 1:
						//Find Person
						startActivity(new Intent(MedibugsActivity.this, FindPerson.class));
						break;
					case 2:
						//Refresh
						medi_post.disconnect(client);
						me.network_lock=false;
						reload();
						
					}
					
				}else {
					if(arg2==2){
						medi_post.disconnect(client);
						me.network_lock=false;
						reload();
					}else {
						Toast.makeText(MedibugsActivity.this, "Please Refresh", 
								Toast.LENGTH_SHORT).show();
					}
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
    	System.out.println("aborting...");
    	//client.getConnectionManager().shutdown();
    	client = medi_post.connect(username, password);
    	me.client=client;
    	medi_post postme;
    	System.out.println("stafflink: "+me.stafflink);
    	
    	if (!me.hasStafflink()||
    			me.username!=username||
    			!me.network_auth){
    		System.out.println("no stafflink");
    		me.stafflink=null;
    		me.username=username;
    		me.data = new HashMap<String, String>();
    		postme = new medi_post(me.data,me.is_lss);
    		postme.execute(me);
    	} else {	
			me.secondaryLoad();
			postme = new medi_post(me.data,me.is_lss);
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

		//double fix for issue 1
		if(me!=null&&me.webview!=null){
			
			SharedPreferences spref=
					PreferenceManager.getDefaultSharedPreferences(me.context);
			SharedPreferences.Editor editor = spref.edit();
			editor.putString("full_name", me.full_name);
			editor.putString("status", me.status);
			editor.putString("date", me.date);
			if(me.hasStafflink()){
				editor.putString("stafflink",me.stafflink);
			} else {
				//editor.putString("stafflink","");
				//
			}
			editor.putString("imglink",me.imglink);
			editor.commit();
			
		}

		reload(1);
		
	}
    
}

class unauthorized extends Exception {
	private static final long serialVersionUID = 1L;
	
}
