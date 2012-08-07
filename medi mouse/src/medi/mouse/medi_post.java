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

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
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
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
//import org.apache.commons.codec.binary.Base64;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Credentials;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewManager;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
//import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class medi_post extends AsyncTask<medi_person,Integer,medi_person>{

	public static String SITE= "https://www.meditech.com/employees/RATweb/RATWeb.mps";
	public static String LSS_SITE= "https://www.staff.lssdata.com/RATweb/RATweb.mps";
	
	private Map<String,String> data;
	private boolean is_lss = false;
	public static ClientConnectionManager CM=null;
	
	public static int counter=0;
	
	public medi_post(Map<String, String> data,boolean is_lss){
		this.data=data;
		this.is_lss=is_lss;
	}
	public medi_post(boolean is_lss){
		
		this.data=new HashMap<String, String>();
		this.is_lss=is_lss;
	}
	private static void init(){
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
		
		BasicHttpParams params = new BasicHttpParams();
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		CM = new ThreadSafeClientConnManager(params, schemeRegistry);
		
	}
	public static HttpClient connect(String username,String password){
		
		if(CM==null) {
			init();
		}
		Log.d("medi_post","new http client: "+(counter++));
		BasicHttpParams params = new BasicHttpParams();
		HttpClient httpclient;
		
		httpclient = new DefaultHttpClient(CM, params);
		((AbstractHttpClient) httpclient).getCredentialsProvider().setCredentials(
				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), 
				new UsernamePasswordCredentials(username, password));
		CM.closeIdleConnections(120, TimeUnit.SECONDS);
		return httpclient;
		

	}

	public static String submit(HttpClient client,Map<String, String> data,
			String method){
		return null;
	}
	public static void disconnect(HttpClient client){
		CM.shutdown();
		
		//CM.releaseConnection(client, (long)0, TimeUnit.DAYS);
		//CM=null;
	}
	
	public void execute(medi_person me){
		Log.d("medi_post","exec1");
		
		LinearLayout lp = (LinearLayout) me.context.findViewById(R.id.refresh_view);
		final ImageView imageView = new ImageView(me.context);   
		imageView.setImageResource(R.drawable.medimouse);
		lp.addView(imageView, 0);
		
		imageView.setVisibility(View.VISIBLE);
		imageView.startAnimation(AnimationUtils.loadAnimation(me.context, R.anim.rotate));
		Log.d("medi_post","exec2");
		super.execute(me);
	}
	
	
	public String doSubmit(HttpClient client,
			String method, 
			String username,
			String password,
			Activity context) throws unauthorized {		

		List<NameValuePair> nameValuePairs = 
				new ArrayList<NameValuePair>(data.size());
		Set<String> keys = data.keySet();
		Iterator<String> keyIter = keys.iterator();
		while( keyIter.hasNext() ) {
			String key = (String) keyIter.next();
			if (key.length()>0){
				nameValuePairs.add(new BasicNameValuePair(key, data.get(key)));
			}
		}

		try{
			Log.d("medi_post","post 1");
			HttpPost post;
			if(is_lss){
				post = new HttpPost(LSS_SITE);
				System.out.println(LSS_SITE);
			} else {
				post = new HttpPost(SITE);
				System.out.println(SITE);
			}
			
			//fix for Bad Request (Invalid Verb) error
			post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
			post.setHeader("Content-Type","application/x-www-form-urlencoded");
			post.setEntity(new  UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = client.execute(post);
			//client.sendRequestEntity(post);
			//client.receiveResponseEntity(response);
			Log.d("medi_post","post 2");
			String file = "";
			String line = "";
			
			//webview.setHttpAuthUsernamePassword(SITE, "meditech.com", username, password);
			//webview.postUrl(SITE, EncodingUtils.getBytes(post_data, "BASE64"));

			BufferedReader in = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			System.out.println(6);
			Log.d("medi_post","post 3");

			while((line=in.readLine())!=null) {
				file += line;				
			}
			
			if(file.contains("not authorized")){
				throw new unauthorized();
			}
			
			in.close();
			Log.d("medi_post",file);

			return file;
		} catch (IOException e) {
			Log.d("medi_post",":::"+e.getMessage()+"\n");
			
			return "Network Error: "+e.getMessage();
			
		}

		
	}


	@Override
	protected medi_person doInBackground(medi_person... params) {
		Log.d("medi_post","exec3");
		if(params.length>0){
			medi_person me = params[0];
			//actual fix for issue 1 (the easy way)
			me.webview="Network Error";
			
			String ret = "";
			if(this.data.containsKey("TYPE")){
				
				try {
					Log.d("medi_post","exec4");
					
						ret=doSubmit(me.client,"POST",me.username,
								me.password,me.context);
						me.network_lock=false;
						if(ret!=null){
							Log.d("medi_post","exec4.1");
							me.parseresponse(ret,this.data.get("TYPE"));
					Log.d("medi_post","exec4.2");
					me.webview=ret;
					}
					
					me.network_auth=true;
				} catch (unauthorized e) {
					
					me.webview+=": username/password rejected";
					me.network_auth=false;
				}
				return me;
			} else{
				try {
					Log.d("medi_post","exec5");
					if(!me.hasStafflink()){
							me.primaryLoad();
							this.data = me.data;
							ret=doSubmit(me.client,"POST",me.username,
									me.password,me.context);
							me.network_lock=false;
							if(ret!=null){
								me.parseresponse(ret,this.data.get("TYPE"));
							}
							me.webview=ret;
					}

					if(me.hasStafflink()){
						Log.d("medi_post","exec6");
						SharedPreferences spref=
								PreferenceManager.getDefaultSharedPreferences(me.context);
						spref.edit().putString("stafflink", me.stafflink);
						spref.edit().commit();
						System.out.println(":----:"+me.stafflink);

						me.secondaryLoad();
						this.data = me.data;
						ret=doSubmit(me.client,"POST",me.username,me.password,me.context);
						me.network_lock=false;
						if(ret!=null){
							me.parseresponse(ret,this.data.get("TYPE"));
						}
						me.webview=ret;
					}			    	
					me.network_auth=true;
				} catch (unauthorized e) {
					me.network_auth=false;
					me.webview+=": username/password rejected";
					//me.context.startActivity(new Intent(me.context, EditPreferences.class));
				}
			}
			
			return me;
		}
		return null;
	}
	@Override
	protected void onPostExecute(medi_person me)  {
		//Now remove them 
        
        final LinearLayout lp = (LinearLayout) me.context.findViewById(R.id.refresh_view);
        lp.getChildAt(0).clearAnimation();
        
        lp.removeViewAt(0);
        
        //fix issue where user had the word Error in their trax it would display the html
        int t = me.webview.indexOf("Network Error");
        if(t!=-1&&t<10){
        	Toast.makeText(me.context, me.webview, Toast.LENGTH_LONG).show();
        }else {
        	Toast.makeText(me.context, "Success!", Toast.LENGTH_SHORT).show();
        }
        
        
		int bad = me.webview.indexOf("<img");
		int end;
		while (bad != -1){
			end = me.webview.indexOf(">", bad);
			me.webview = me.webview.substring(0, bad)+
					me.webview.substring(end+1, me.webview.length());
			bad = me.webview.indexOf("<img");
		}
		
		System.out.println("====================>>>\n"+me.webview);
		
		

		me.context.onPostExecute(me);
		//super.onPostExecute(me);
		
	}
}

