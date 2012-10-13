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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class coretrax_post extends AsyncTask<medi_person,Integer,medi_person>{
	private static String SITE= "http://core.meditech.com/core-coreWeb.traxDesktop.mthr";
	private static String POST_AUTH= "http://core.meditech.com/signon.mthz";
	private static String BASE = "http://core.meditech.com";
	private Map<String,String> data;
	private ArrayList<String> info = new ArrayList<String>();
	public static ClientConnectionManager CM=null;
	public menu_node root_menu = new menu_node("","");
	BasicHttpContext mHttpContext;

	private DefaultHttpClient mHttpClient;
	private CookieStore  mCookieStore;
	
	private boolean auth = false;
	private String post_link = "";
	public coretrax_post(){
		this.data=new HashMap<String, String>();
		
		mHttpContext = new BasicHttpContext();
		mCookieStore = new BasicCookieStore();        
		mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
		mHttpClient = new DefaultHttpClient();
	}
	public void postthis(ArrayList<String> info,medi_person me){
		this.info = info;
		this.execute(me);
	}
	public void execute(medi_person me){
		//add loading image to layout
		LinearLayout lp = (LinearLayout) me.context.findViewById(R.id.refresh_view);
		final ImageView imageView = new ImageView(me.context);   
		imageView.setImageResource(R.drawable.medimouse);
		lp.addView(imageView, 0);
		
		imageView.setVisibility(View.VISIBLE);
		//spin it
		imageView.startAnimation(AnimationUtils.loadAnimation(me.context, R.anim.rotate));
		super.execute(me);
	}
	private boolean getAuth(String username,
			String password) throws unauthorized, IllegalStateException, IOException{

		
		
		HttpGet httpget = new HttpGet(SITE);
		HttpResponse response;
		
		response = mHttpClient.execute(httpget,this.mHttpContext);
		
		response.getEntity();
		
		HttpPost post;
		post = new HttpPost(POST_AUTH);


		List<NameValuePair> nameValuePairs = 
				new ArrayList<NameValuePair>(2);

		nameValuePairs.add(new BasicNameValuePair("userid", username));
		nameValuePairs.add(new BasicNameValuePair("password", password));

		

		post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		post.setHeader("Content-Type","application/x-www-form-urlencoded");

		post.setEntity(new  UrlEncodedFormEntity(nameValuePairs));

		response = mHttpClient.execute(post, mHttpContext);
		
		String file = "";
		String line = "";

		//webview.setHttpAuthUsernamePassword(SITE, "meditech.com", username, password);
		//webview.postUrl(SITE, EncodingUtils.getBytes(post_data, "BASE64"));

		BufferedReader in = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));
		
		while((line=in.readLine())!=null) {
			file += line;				
		}
		response.getEntity().consumeContent();
		if(file.contains("Invalid username/password")||
				file.contains("Missing field(s)")){
			throw new unauthorized();
		}
		menu_node d = parse_info(file);
		
		Log.d("coretrax_post",file);
		Log.d("coretrax_post",d.menu.get("name").value);
		Log.d("coretrax_post",d.menu.get("primary_status").value);
		Log.d("coretrax_post",d.menu.get("secondary_status").value);
		Log.d("coretrax_post",d.menu.get("save").link);
		
		
		
		return true;
	}
	private menu_node parse_info(String file) {
		 
		
		//name
		int t1 = file.indexOf("z0-b-0-2-0-0_0s");
		t1 = file.indexOf(">",t1)+1;
		int t2 = file.indexOf("<",t1);
		root_menu.put("name", new menu_node("",file.substring(t1,t2)));
		
		//status
		t1 = file.indexOf("z0-b-0-2-0-0_1s");
		t1 = file.indexOf(">",t1)+1;
		t2 = file.indexOf("<",t1);
		Log.d("coretrax_post",file.substring(t1,t2));
		String[] t = file.substring(t1,t2).split(",");
		String status1 = t[0];
		String status2 = t.length>1?t[1]:"";
		
		root_menu.put("primary_status", new menu_node("",status1));
		root_menu.put("secondary_status", new menu_node("",status2));
		
		String next_img = "javascript:LinkClicked(\".";
		String end_next_img = "\"";
		String name_img = "<span>";
		String end_name_img = "</span>";
		String link;
		String link_name;
		int end;
		int next;
		//--------------------------------------------------------------		
		//in div
		t1 = file.indexOf("z0-b-0-4-1-0");
		t1 = file.indexOf(">",t1)+1;
		end = file.indexOf("</table>",t1);
		next = file.indexOf("<td",t1);
		menu_node in = new menu_node("","In");
		while(next<end&&next!=-1){
			t1 = file.indexOf(next_img,next)+next_img.length();
			t2 = file.indexOf(end_next_img,t1);
			link = file.substring(t1,t2);
			
			t1 = file.indexOf(name_img,t1)+name_img.length();
			t2 = file.indexOf(end_name_img,t1);
			link_name = file.substring(t1,t2);
			Log.d("coretrax_post",link_name+":"+link);
			in.put(link_name, new menu_node(link,""));
			next = file.indexOf("<td",t1);
		}
		root_menu.put("in",in);
		//--------------------------------------------------------------
		//bldg-div
		t1 = file.indexOf("z0-b-0-4-2-0");
		t1 = file.indexOf(">",t1)+1;
		end = file.indexOf("</table>",t1);
		next = file.indexOf("<td",t1);
		menu_node bldg = new menu_node("","bldg");
		while(next<end&&next!=-1){
			t1 = file.indexOf(next_img,next)+next_img.length();
			t2 = file.indexOf(end_next_img,t1);
			link = file.substring(t1,t2);
			t1 = file.indexOf(name_img,t1)+name_img.length();
			t2 = file.indexOf(end_name_img,t1);
			link_name = file.substring(t1,t2);
			Log.d("coretrax_post",link_name+":"+link);
			bldg.put(link_name, new menu_node(link,""));
			next = file.indexOf("<td",t1);
		}
		root_menu.menu.put("bldg",bldg);
		//--------------------------------------------------------------
		//out-div
		t1 = file.indexOf("z0-b-0-6-1-0");
		t1 = file.indexOf(">",t1)+1;
		end = file.indexOf("</table>",t1);
		next = file.indexOf("<td",t1);
		menu_node out = new menu_node("","out");
		while(next<end&&next!=-1){
			t1 = file.indexOf(next_img,next)+next_img.length();
			t2 = file.indexOf(end_next_img,t1);
			link = file.substring(t1,t2);
			t1 = file.indexOf(name_img,t1)+name_img.length();
			t2 = file.indexOf(end_name_img,t1);
			link_name = file.substring(t1,t2);
			Log.d("coretrax_post",link_name+":"+link);
			out.put(link_name, new menu_node(link,""));
			next = file.indexOf("<td",t1);
		}
		root_menu.menu.put("out",out);
		//date-div
		t1 = file.indexOf("z0-b-0-6-2-0");
		t1 = file.indexOf(">",t1)+1;
		end = file.indexOf("</table>",t1);
		next = file.indexOf("<td",t1);
		
		menu_node date = new menu_node("","bldg");
		while(next<end&&next!=-1){
			t1 = file.indexOf(next_img,next)+next_img.length();
			t2 = file.indexOf(end_next_img,t1);
			link = file.substring(t1,t2);
			t1 = file.indexOf(name_img,t1)+name_img.length();
			t2 = file.indexOf(end_name_img,t1);
			link_name = file.substring(t1,t2);
			Log.d("coretrax_post",link_name+":"+link);
			date.put(link_name, new menu_node(link,""));
			next = file.indexOf("<td",t1);
		}
		root_menu.menu.put("date",date);
		//--------------------------------------------------------------
		t1 = file.indexOf("z0-f-0-1-0-0-0",t1);
		//Log.d("coretrax_post",file.substring(t1,t1+300));
		t1 = file.indexOf(next_img,t1)+next_img.length();
		Log.d("coretrax_post",t1+":");
		t2 = file.indexOf(end_next_img,t1);
		Log.d("coretrax_post",t1+":"+t2);
		root_menu.menu.put("save", new menu_node(file.substring(t1,t2),""));
		
		return root_menu;
	}

	@Override
	protected medi_person doInBackground(medi_person... arg0) {
		// TODO Auto-generated method stub
		medi_person me = arg0[0];
		
		
		try {
			getAuth(me.username,me.password);
		} catch (unauthorized e) {
			me.webview = "Network Error: Unauthorized (username/password rejected)";
			
			return me;
		} catch (IllegalStateException e) {
			me.webview = "Network Error: "+e.getMessage();
			return me;
		} catch (IOException e) {
			me.webview = "Network Error: "+e.getMessage();
			return me;
		}
		
		SharedPreferences spref=
				PreferenceManager.getDefaultSharedPreferences(me.context);
		SharedPreferences.Editor editor = spref.edit();
		
		editor.putString("full_name", root_menu.menu.get("name").value);
		editor.putString("status", root_menu.menu.get("primary_status").value);
		editor.putString("date", root_menu.menu.get("secondary_status").value);
		me.webview="success!";
		editor.commit(); 
		
		if(this.info.size()==3){
			HttpPost post;
			Log.d("coretrax_post","submiting...");
			
			Log.d("coretrax_post",this.info.get(1));
			Log.d("coretrax_post",this.info.get(2));
			try {
				String link;
				if(this.info.get(0).charAt(0)=="in".charAt(0)) {
					link = this.root_menu.menu.get("in").menu.get(this.info.get(1)).link;
					Log.d("coretrax_post","link:"+link);
					post = new HttpPost(link);
					HttpResponse response = mHttpClient.execute(post, mHttpContext);
					response.getEntity().consumeContent();
					
					
					link = this.root_menu.menu.get("bldg").menu.get(this.info.get(2)).link;
					Log.d("coretrax_post","link:"+link);
					post = new HttpPost(link);
					response = mHttpClient.execute(post, mHttpContext);
					response.getEntity().consumeContent();
					
				}else if (this.info.get(0).charAt(0)=="out".charAt(0)) {
					link = this.root_menu.menu.get("out").menu.get(this.info.get(1)).link;
					Log.d("coretrax_post","link:"+link);
					post = new HttpPost(link);
					HttpResponse response = mHttpClient.execute(post, mHttpContext);
					response.getEntity().consumeContent();
					
					
					link = this.root_menu.menu.get("date").menu.get(this.info.get(2)).link;
					Log.d("coretrax_post","link:"+link);
					post = new HttpPost(link);
					response = mHttpClient.execute(post, mHttpContext);
					response.getEntity().consumeContent();
					
				}
				link = this.root_menu.menu.get("save").link;
				Log.d("coretrax_post","link:"+link);
				post = new HttpPost(link);
				HttpResponse response = mHttpClient.execute(post, mHttpContext);
				response.getEntity().consumeContent();

				me.status = this.info.get(1);
				me.date = this.info.get(2);
				me.webview = "core trax status saved!";
				return me;
			} catch (ClientProtocolException e) {
				me.webview = "Network Error: "+e.getMessage();
			} catch (IOException e) {
				me.webview = "Network Error: "+e.getMessage();
			}
			
		}
		return me;
	}
	
	@Override
	protected void onPostExecute(medi_person me)  {
		//Now remove them 
        
        final LinearLayout lp = (LinearLayout) me.context.findViewById(R.id.refresh_view);
        lp.getChildAt(0).clearAnimation();
        
        lp.removeViewAt(0);
        
        int t = me.webview.indexOf("Network Error");
		if(t!=-1&&t<10){
			Toast.makeText(me.context, me.webview, Toast.LENGTH_LONG).show();
			if(me.webview.contains("Unauthorized")){
				me.context.startActivity(new Intent(me.context, EditPreferences.class));
			}
			
		}else {
			Toast.makeText(me.context, me.webview, Toast.LENGTH_SHORT).show();
		}
        me.context.onPostExecute(me);
	}
	
	public class menu_node{
		public HashMap<String,menu_node> menu;
		public ArrayList<String> keyList;
		
		public String link = "";
		public String value = "";
		
		public menu_node(String link,String value){
			this.link = BASE+link;
			this.value = value;
			this.menu = new HashMap<String,menu_node>();
			this.keyList = new ArrayList<String>();
		}
		public void put(String key,menu_node value){
			if(menu.containsKey(key)){
				menu.put(key, value);
			}else{
				keyList.add(key);
				menu.put(key, value);
			}
		}
		
		
	}
  
}
