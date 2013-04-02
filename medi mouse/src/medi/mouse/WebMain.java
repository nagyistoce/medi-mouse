package medi.mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class WebMain extends medi_mouse_activity implements OnSharedPreferenceChangeListener{
	private final static String TAG = "WebMain";
	public JSONObject result;
	public boolean result_done;
	private ArrayList<coretrax_args> post_q;
	private ArrayList<coretrax_resp> result_q;
	private int posting = 0;
	private boolean isPosting = false;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webmain);
		WebView myWebView = (WebView) findViewById(R.id.webmain);
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		myWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
		myWebView.loadUrl("file:///android_asset/main.html");  
		
		post_q = new ArrayList<coretrax_args>();
		result_q = new ArrayList<coretrax_resp>();
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPostExecute(Object result) {
		coretrax_resp resp = (coretrax_resp)result;
		int pos = resp.pos;
		result_q.add(pos,resp);
		post_q.set(pos,null);
		isPosting=false;
		
		//maybe post the next thing
		startPosting(pos+1);
	}
	public int addToPostQueue(coretrax_args args){
		args.pos = posting;
		Log.d(TAG,"post queue 1");
		post_q.add(posting,args);
		Log.d(TAG,"post queue 2");
		posting++;
		if(!isPosting){
			Log.d(TAG,"start posting..");
			startPosting(args.pos);
		}
		return args.pos;
	}
	private void startPosting(int pos) {
		coretrax_args arg;
		if(pos<post_q.size() &&  (arg = post_q.get(pos))!=null){
			isPosting = true;
			coretrax_post trax_post = new coretrax_post(this);
			trax_post.execute(post_q.get(pos));
		}
	}
	public String getResult(int result){
		return result_q.get(result).toString();
	}
	public boolean isDone(int pos){
		if(pos>=result_q.size()){
			return false;
		}
		coretrax_resp resp = result_q.get(pos);
		if(resp!=null){
			return true;
		}else{
			return false;
		}
			
	}

}

class WebAppInterface {
	static final String TAG = "jsconsole";
    Context mContext;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
    @JavascriptInterface
    public void log(String msg) {
        Log.d(TAG,msg);
    }
    public int getWhatsNew(){
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(mContext);
    	return spref.getInt("whatsNew", 0);
    }
    public void saveSettings(String message){
    	Log.d(TAG,"saving: "+message);
    	JSONObject settings = new JSONObject();
		try {
			settings = new JSONObject(message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(mContext);
    	SharedPreferences.Editor editor = spref.edit();
    	
    	Iterator<?> keys = settings.keys();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            Object obj;
			try {
				obj = settings.get(key);
			    if( obj instanceof String ){
	            	editor.putString(key, (String)obj);
	            }
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    	
		editor.commit();
    }
    public String getSetting(String var){
    	Log.d(TAG,"getting var: "+var);
    	SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(mContext);
    	String def = "";
    	if(var.equals("text-color")){
    		def = "#000000";
    	}else if(var.equals("background-color")){
    		def = "#000000";
    	}else if(var.equals("box-color")){
    		def = "#F2F200";
    	}
    	String ret = spref.getString(var, def);
    	
    	return ret;
    }
    public String getStatusExec(String username,String password){
    	coretrax_args arg = new coretrax_args("get_status",username,password);
    	int postq_pos = ((WebMain) mContext).addToPostQueue(arg);
    	return "Android.getStatusCheck("+postq_pos+")";
    }
    public String getStatusCheck(int postq_pos){
    	if(((WebMain)mContext).isDone(postq_pos)){
    		return ((WebMain)mContext).getResult(postq_pos);
    	} else {
    		return "{\"error\":\"result not done\"}";
    	}
    }
    public String post(String data){
    	coretrax_args arg = new coretrax_args(data);
    	int postq_pos = ((WebMain) mContext).addToPostQueue(arg);
    	return "Android.checkPost("+postq_pos+")";
    }
    public String checkPost(int postq_pos){
    	if(((WebMain)mContext).isDone(postq_pos)){
    		return ((WebMain)mContext).getResult(postq_pos);
    	} else {
    		return "{\"error\":\"result not done\"}";
    	}
    }
    
}
