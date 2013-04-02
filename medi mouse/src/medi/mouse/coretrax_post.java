package medi.mouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class coretrax_post 
		extends AsyncTask<coretrax_args,
						  Integer,
						  coretrax_resp>{
	private static String TAG = "coretrax_post";
	private static String SITE= "https://corebackend.appspot.com/handler.html";
	//private static String SITE= "http://localhost:9080/handler.html";
	
	public static ClientConnectionManager CM=null;
	private medi_mouse_activity context;
	BasicHttpContext mHttpContext;

	public coretrax_post(medi_mouse_activity context){
		
		this.context = context;
	}
	
	public void execute(String type,
			String username, 
			String password,
			String[] path,
			HashMap<String,String> extra){
		
		coretrax_args arg = new coretrax_args(type,username,password,path,extra);
		super.execute(arg);
	}
	public void execute(coretrax_args arg){
		super.execute(arg);
	}
	@Override
	protected coretrax_resp doInBackground(coretrax_args... arg0) {
		String resp = makeRequest(SITE,arg0[0].getData());
		return new coretrax_resp(resp,arg0[0].pos);
		
	}
	public static String makeRequest(String path, JSONObject params) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(path);
		String error = "";
		
		StringEntity se;
		try {
			se = new StringEntity(params.toString());
		
			httpost.setEntity(se);
			httpost.setHeader("Content-type", "application/json");
			
			HttpResponse response = httpclient.execute(httpost);
			
			String file = "";
			String line = "";
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			while((line=in.readLine())!=null) {
				file += line;				
			}
			
			response.getEntity().consumeContent();
			Log.d(TAG,file);
			
			
			return file;
		
		}catch (ClientProtocolException e) {
			error = e.getMessage();
			//e.printStackTrace();
		} catch (IOException e) {
			error = e.getMessage();
			//e.printStackTrace();
		}
		
		return "{\"error\": \"network error\",\"message\":\""+error+"\"}";
	} 
	@Override
	protected void onPostExecute(coretrax_resp resp)  {

        this.context.onPostExecute(resp);
	}
	
	
  
}
