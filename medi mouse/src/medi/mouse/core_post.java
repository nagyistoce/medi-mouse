package medi.mouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
//import org.apache.commons.codec.binary.Base64;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Credentials;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
//import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class core_post extends AsyncTask<medi_person,Integer,medi_person>{

	public static String SITE = "https://core.meditech.com/core-coreWebHH.desktop.mthh";
	public static String SITE2= "https://core.meditech.com/signon.mthz";

	private Map<String,String> data;
	private boolean is_lss = false;
	public static ClientConnectionManager CM=null;
	BasicHttpContext mHttpContext;

	private DefaultHttpClient mHttpClient;
	private CookieStore  mCookieStore;

	public core_post(boolean is_lss){

		this.data=new HashMap<String, String>();
		this.is_lss=is_lss;
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
			HttpResponse response = mHttpClient.execute(httpget,this.mHttpContext);
			HttpEntity entity = response.getEntity();

			System.out.println("Login form get: " + response.getStatusLine());
			if (entity != null) {
				entity.consumeContent();
			}
			System.out.println("Initial set of cookies:");
			List<Cookie> cookies = mCookieStore.getCookies();
			if (cookies.isEmpty()) {
				System.out.println("None");
			} else {
				for (int i = 0; i < cookies.size(); i++) {
					System.out.println("- " + cookies.get(i).toString());
				}
			}


			System.out.println("==========second post");
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

			if(file.contains("Invalid username/password")||
					file.contains("Missing field(s)")){
				throw new unauthorized();
			}

			in.close();
			System.out.println(file);
			System.out.println("==========done post");
			return file;
		} catch (IOException e) {
			System.out.println(":::"+e.getMessage());
			return "Network Error: "+e.getMessage();

		}


	}


	@Override
	protected medi_person doInBackground(medi_person... params) {
		System.out.println("core post hurray!!");
		if(params.length>0){
			medi_person me = params[0];
			//actual fix for issue 1 (the easy way)
			me.webview="Network Error: Failed to connect with core";
			String user = me.is_lss?me.lss_core_username:me.username;
			String pass = me.is_lss?me.lss_core_password:me.password;
			try {
				System.out.println(1);
				String ret = doSubmit(this.mHttpClient,"POST",user,
						pass,me.context);
				me.webview=ret;
				System.out.println(ret);
			} catch (unauthorized e) {
				System.out.println(2);
				me.webview+=":  Unauthorized \nUsername/Password rejected";

			}
			return me;

		}
		return null;
	}
	@Override
	protected void onPostExecute(medi_person me)  {
		int d;
		final ArrayList<String> events = new ArrayList<String>();
		String event_date;
		String event_time;
		String event_info_link;
		String event_name;
		String event_place;

		//parse file for event info:
		
		//check for errors
		int t = me.webview.indexOf("Network Error");
		if(t!=-1&&t<10){
			Toast.makeText(me.context, me.webview, Toast.LENGTH_LONG).show();
			if(me.webview.contains("Unauthorized")){
				me.context.startActivity(new Intent(me.context, EditPreferences.class));
			}
		}else {
			Toast.makeText(me.context, "connected to core", Toast.LENGTH_SHORT).show();

			//start parsing dates
			int c = me.webview.indexOf("<td class=\"style9\">");
			int e = me.webview.indexOf("LogOutBig.png",c);
			d = me.webview.indexOf("</td>", c);
			c=c+"<td class=\"style9\">".length();
			d = me.webview.indexOf("</td>", c);
			
			if(e<d||c>=me.webview.length()||c==-1||d==-1){
				events.add("No Events");
				
			} else {
				event_date = me.webview.substring(c,d);

				while (d != -1){

					c = me.webview.indexOf("<td class=\"style12\">",d);


					c=c+"<td class=\"style12\">".length();
					d = me.webview.indexOf("</td>",c);
					event_time = me.webview.substring(c,d);

					c = me.webview.indexOf("<td class=\"style12\"><a href=\"",d);
					c=c+"<td class=\"style12\"><a href=\"".length();
					d = me.webview.indexOf("\">",c);
					event_info_link = me.webview.substring(c,d);

					c = d + "\">".length();
					d = me.webview.indexOf("</a>",c);
					event_name = me.webview.substring(c,d);

					c = me.webview.indexOf("<td class=\"style12\">",d);
					c=c+"<td class=\"style12\">".length();
					d = me.webview.indexOf("</td>",c);
					event_place = me.webview.substring(c,d);

					//complete event should now have been read in
					events.add(new event(event_date,event_time,event_info_link,
							event_name,event_place).toString());


					int c1 = me.webview.indexOf("<td class=\"style9\">",d);
					int c2 = me.webview.indexOf("<td class=\"style12\">",d);
					if (c2<c1 || (c1 == -1)){
						//maybe read in more events on this date 
						d = c2;
					} else if (c1!=-1) {
						//next date
						c = c1;
						c=c+"<td class=\"style9\">".length();
						d = me.webview.indexOf("</td>", c);
						event_date = me.webview.substring(c,d);

					} else {
						//done
						d=-1;
					}
				}
			}
			LinearLayout ll = (LinearLayout) me.context.findViewById(R.id.core_view);
			ll.removeAllViews();
			for (int x = 0; x<events.size(); x++){
				View v = me.context.getLayoutInflater().inflate(R.layout.menu, null);
				TextView tv = (TextView) v.findViewById(R.id.name);
				tv.setText(events.get(x)+"\n");
				ll.addView(v, x);

			}
		}

	}

	class event{
		String event_date,event_time,event_info_link,event_name,event_place;
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
			no_event = false;
			this.event_date = fix(event_date);
			this.event_time = fix(event_time);
			this.event_info_link = fix(event_info_link);
			this.event_name = fix(event_name);
			this.event_place = fix(event_place);
			
					
		}
		private String fix(String input){
			for (int x = 0; x < ignored_words.size();x++){
				String ignore = ignored_words.get(x);
				int c = input.indexOf(ignore);
				while (c!=-1){
					input = input.substring(0, c) + input.substring(c+ignore.length());
					c = input.indexOf(ignore);
				}
				
			}
			for (String key : replace_words.keySet()) {
				int c = input.indexOf(key);
				while (c!=-1){
					input = input.substring(0, c) + 
							replace_words.get(key)+
							input.substring(c+key.length());
					c = input.indexOf(key);
				}
			}
			return input;
		}
		public event(String name) {
			this.event_name = name;
			this.no_event = true;
		}
		public String toString(){
			System.out.println(event_date+"\n"+event_time+"\n"+event_name+"\n"+event_place);
			if (no_event){
				return event_name;
			} else if(event_place.length()>0) {
				return event_date+" "+event_time+"\n"+event_name+"\n"+event_place;
			} else {
				return event_date+" "+event_time+"\n"+event_name;
			}
		}
	}
}

