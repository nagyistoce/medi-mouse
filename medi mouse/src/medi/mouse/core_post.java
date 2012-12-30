package medi.mouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
//import org.apache.commons.codec.binary.Base64;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
//import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class core_post extends AsyncTask<medi_person,Integer,medi_person>{

	public static String SITE = "https://core.meditech.com/core-coreWebHH.desktop.mthh";
	public static String SITE2= "https://core.meditech.com/signon.mthz";
	public static String BASE = "https://core.meditech.com/";
	public static String TAG = "core_post";
	private Map<String,String> data;
	
	public static ClientConnectionManager CM=null;
	BasicHttpContext mHttpContext;

	private DefaultHttpClient mHttpClient;
	private CookieStore  mCookieStore;
	private String user;
	private String pass;


	
	public core_post(boolean is_lss){

		this.data=new HashMap<String, String>();
		mHttpContext = new BasicHttpContext();
		mCookieStore = new BasicCookieStore();        
		mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);

		mHttpClient = new DefaultHttpClient();
		
		

	}


	public static String submit(HttpClient client,Map<String, String> data,
			String method){
		return null;
	}

	public String doSubmit(HttpClient client,
			String method, 
			String username,
			String password,
			Activity context) throws unauthorized {		

		try{
			HttpPost post;
			HttpGet httpget = new HttpGet(SITE);
			Log.d(TAG,"first post");
			HttpResponse response = mHttpClient.execute(httpget,this.mHttpContext);
			HttpEntity entity = response.getEntity();
			this.user = username;
			this.pass = password;
			//System.out.println("Login form get: " + response.getStatusLine());
			if (entity != null) {
				entity.consumeContent();
			}
			//System.out.println("Initial set of cookies:");
			List<Cookie> cookies = mCookieStore.getCookies();
			if (cookies.isEmpty()) {
				//System.out.println("None");
			} else {
				for (int i = 0; i < cookies.size(); i++) {
					//System.out.println("- " + cookies.get(i).toString());
				}
			}

			Log.d(TAG,"second post");
			//System.out.println("==========second post");
			post = new HttpPost(SITE2);


			List<NameValuePair> nameValuePairs = 
					new ArrayList<NameValuePair>(2);

			nameValuePairs.add(new BasicNameValuePair("userid", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));

			

			post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
			post.setHeader("Content-Type","application/x-www-form-urlencoded");

			post.setEntity(new  UrlEncodedFormEntity(nameValuePairs));

			response = client.execute(post, mHttpContext);


			String file = "";
			String line = "";

			//webview.setHttpAuthUsernamePassword(SITE, "meditech.com", username, password);
			//webview.postUrl(SITE, EncodingUtils.getBytes(post_data, "BASE64"));

			BufferedReader in = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			


			while((line=in.readLine())!=null) {
				file += line;				
			}
			Log.d(TAG,"file: "+file);
			response.getEntity().consumeContent();
			int t1 = file.indexOf("./Images\\HHIcon_LogOutBig.png")-40;
			if(t1>0){
				t1 = file.indexOf("href=\"./",t1)+8;
				int t2 = file.indexOf("\"",t1);
				String logout = file.substring(t1,t2);
				post = new HttpPost(BASE+logout);
				Log.d(TAG,"logout" );
				response = client.execute(post, mHttpContext);
				
				
				in = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				
	
				String file2 = "";
				while((line=in.readLine())!=null) {
					file2 += line;				
				}
				response.getEntity().consumeContent();
				Log.d(TAG,"logout link"+ BASE+logout);
				Log.d("core_post","logout link"+ file2);

			}
			if(file.contains("Invalid username/password")||
					file.contains("Missing field(s)")){
				throw new unauthorized();
			}

			in.close();
			
			return file;
		} catch (IOException e) {
			Log.d(TAG,":::"+e.getMessage());
			return "Network Error: "+e.getMessage();

		}


	}


	@Override
	protected medi_person doInBackground(medi_person... params) {
		
		if(params.length>0){
			medi_person me = params[0];
			if(shared.debug){
				return me;
			}
			SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(me.context);
	    	user = spref.getString("user_name", "");
	    	pass = spref.getString("user_password","");
	    	
			//actual fix for issue 1 (the easy way)
			me.webview="Network Error: Failed to connect with core";
			
			try {
				String ret = doSubmit(this.mHttpClient,"POST",user,
						pass,me.context);
				
				me.webview=ret;
			} catch (unauthorized e) {
				me.webview+=":  Unauthorized \nUsername/Password rejected";

			}
			
			return me;

		}
		return null;
	}
	@Override
	protected void onPostExecute(medi_person me)  {
		final ArrayList<event> events = new ArrayList<event>();
		//parse file for event info:
		
		//check for errors
		int t = me.webview.indexOf("Network Error");
		if(t!=-1&&t<10){
			Toast.makeText(me.context, me.webview, Toast.LENGTH_LONG).show();
			
			//add test event
			//events.add(new event("M123","420", "", "Fake event", "Gloucester (Framingham)"));
			//loadUI(me,events);
			if(me.webview.contains("Unauthorized")){
				//coretrax_post will start this activity
				//me.context.startActivity(new Intent(me.context, EditPreferences.class));
			}
		}else {
			Toast.makeText(me.context, "connected to core", Toast.LENGTH_SHORT).show();
			if(shared.debug){
				//creating a fake event for testing
				events.add(new event("today", "later", "none", "A event", "my desk's (Lowder Brook 100)"));
				Log.d(TAG,events.get(0).toString());
				loadUI(me,events);
			} else {
				//start parsing dates
				parseEvents(me.webview,events);
				loadUI(me,events);
			}
		}

	}
	private void parseEvents(String webview,ArrayList<event> events) {
		String event_date;
		String event_time;
		String event_info_link;
		String event_name;
		String event_place;
		int c = webview.indexOf("<td class=\"style9\">");
		int e = webview.indexOf("LogOutBig.png",c);
		int d = webview.indexOf("</td>", c);
		c=c+"<td class=\"style9\">".length();
		d = webview.indexOf("</td>", c);
		
		if(e<d||c>=webview.length()||c==-1||d==-1){
			events.add(new event("No Events"));
			
		} else {
			event_date = webview.substring(c,d);

			while (d != -1){

				c = webview.indexOf("<td class=\"style12\">",d);


				c=c+"<td class=\"style12\">".length();
				d = webview.indexOf("</td>",c);
				event_time = webview.substring(c,d);

				c = webview.indexOf("<td class=\"style12\"><a href=\"",d);
				c=c+"<td class=\"style12\"><a href=\"".length();
				d = webview.indexOf("\">",c);
				event_info_link = webview.substring(c,d);

				c = d + "\">".length();
				d = webview.indexOf("</a>",c);
				event_name = webview.substring(c,d);

				c = webview.indexOf("<td class=\"style12\">",d);
				c=c+"<td class=\"style12\">".length();
				d = webview.indexOf("</td>",c);
				event_place = webview.substring(c,d);

				//complete event should now have been read in
				events.add(new event(event_date,event_time,event_info_link,
						event_name,event_place));


				int c1 = webview.indexOf("<td class=\"style9\">",d);
				int c2 = webview.indexOf("<td class=\"style12\">",d);
				if (c2<c1 || (c1 == -1)){
					//maybe read in more events on this date 
					d = c2;
				} else if (c1!=-1) {
					//next date
					c = c1;
					c=c+"<td class=\"style9\">".length();
					d = webview.indexOf("</td>", c);
					event_date = webview.substring(c,d);

				} else {
					//done
					d=-1;
				}
			}
		}
		

		
	}


	private void loadUI(medi_person me, ArrayList<event> events){
		LinearLayout ll = (LinearLayout) me.context.findViewById(R.id.core_view);
		ll.removeAllViews();
		for (int x = 0; x<events.size(); x++){
			event ev = events.get(x);
			View v = ev.toView(me.context);
			ll.addView(v, x);

		}
	}

	class event{
		public String event_date,event_time,event_info_link,event_name,event_place;
		boolean no_event;
		ArrayList<String> ignored_words;
		private HashMap<String, String> replace_words;
		public event(String event_date,
				String event_time,
				String event_info_link,
				String event_name,
				String event_place) {
			this.ignored_words = new ArrayList<String>();
			ignored_words.add("&nbsp;");
			ignored_words.add("<br>");
			this.replace_words = new HashMap<String,String>();
			replace_words.put("&lt;", "<");
			replace_words.put("&gt;", ">");
			replace_words.put("&quot;", "\"");
			replace_words.put("'", "\\'");
			no_event = false;
			this.event_date = fix(event_date);
			this.event_time = fix(event_time);
			this.event_info_link = fix(event_info_link);
			this.event_name = fix(event_name);
			this.event_place = fix(event_place);
			
					
		}
		private String fix(String input){
			for (String key : ignored_words){
				input = input.replaceAll(key, "");
			}
			for (String key : replace_words.keySet()) {
				input = input.replaceAll(key, replace_words.get(key));
			}
			return input;
		}
		public event(String name) {
			this.event_name = name;
			this.no_event = true;
		}
		public String toString(){
			//System.out.println(event_date+"\n"+event_time+"\n"+event_name+"\n"+event_place);
			
			if (no_event){
				return event_name;
			} else if(event_place.length()>0) {
				return "event date: "+event_date+"\n"+
						"event time: "+event_time+"\n"+
						"event name: "+event_name+"\n"+
						"event place: "+event_place;
			} else {
				return event_date+" "+event_time+"\n"+event_name;
			}
		}
		public View toView(final medi_mouse_activity context){
			View v = context.getLayoutInflater().inflate(R.layout.core_item, null);
			TextView tv_name = (TextView) v.findViewById(R.id.name);
			tv_name.setText(event_name);
			
			Log.d(TAG,"this event: "+this.toString());
			if(no_event){
				
			}else if(event_date.length()>0){
				TextView tv_start_time = (TextView) v.findViewById(R.id.start_time);
				TextView tv_end_time = (TextView) v.findViewById(R.id.end_time);
				Log.d(TAG,"set date: "+event_date+"+++"+event_time);
				tv_start_time.setText(event_date+" "+event_time);
				//tv_end_time.setText(event_date+" "+event_time);
				//tv_end_time.setText(event_date+" "+event_time);
			}else{
				v.findViewById(R.id.time_wrapper).setVisibility(View.GONE);
			}
			final String building = parseBldg();
			final String room = parseRoom();
			if(building.length()>0&&room.length()>0){
				TextView tv_place = (TextView) v.findViewById(R.id.location);
				tv_place.setText(event_place);
				ImageView iv = (ImageView) v.findViewById(R.id.map_icon);
				
				iv.setOnClickListener(new OnClickListener(){
					
					public void onClick(View v) {
						//send place information to the facility viewer
						//the facility viewer will need to use the facility post 
						//methods to figure out where the rooms is.
						Intent intent = new Intent(context, FacilityViewerActivity.class);
					    intent.putExtra("core_post", true);
					    intent.putExtra("layer", "core");
					    intent.putExtra("building", building);
					    intent.putExtra("name", room);
					    
					    context.startActivity(intent);
						
					}});
						
			} else {
				v.findViewById(R.id.location_wrapper).setVisibility(View.GONE);
				
			}
			
			return v;
		}
		private String parseBldg(){
			int start = event_place.indexOf("(");
			int end = event_place.indexOf(")");
			String ret = "";
			if(start!=-1&&end!=-1&&start+1<end){
				ret = event_place.substring(start+1, end);
				return ret;
			}
			return "";
		}
		private String parseRoom(){
			int end = event_place.indexOf("(");
			String ret = event_place;
			if(end!=-1){
				ret = ret.substring(0, end);
			}
			return ret;
			
		}
	}
}

