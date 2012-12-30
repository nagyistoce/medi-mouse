package medi.mouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class FacilityPost extends AsyncTask<JSONObject,Integer,JSONObject>{
	private static String TAG = "FacilityPost";
	private static String SITE = "http://williamcohen.com/mm/maps_backend.php";
	private FacilityPostInterface parent;
	public FacilityPost(FacilityPostInterface parent) {
		this.parent = parent;
	}
	public static JSONObject saveLocation(
			String building, 
			String layer,
			String name,
			String imageName,
			float pos_x,
			float pos_y){
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("type", "save_location");
		map.put("building", building);
		map.put("name", name);
		imageName = imageName.replace(shared.PATH, "");
		map.put("image", imageName);
		map.put("layer", layer);
		HashMap<String,Float> position = new HashMap<String,Float>();
		position.put("x", pos_x);
		position.put("y", pos_y);
		
		map.put("position", new JSONObject(position));
		
		return new JSONObject(map);
	}
	public static JSONObject voteLocation(Location location,int vote){
		JSONObject ret = location.toJson();
		try {
			ret.put("type", "save_location");
			ret.put("rank", vote);
		} catch (JSONException e) {
			
		}
		return ret;
	}
	public static JSONObject lookupLayers(String image){
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("type", "lookup_layers");
		image = image.replace(shared.PATH, "");
		map.put("image", image);
		return new JSONObject(map);
	}
	public static JSONObject lookupLocationByName(String building, String name) {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("type", "lookup_location");
		map.put("building", building);
		map.put("name", name);
		return new JSONObject(map);
	}
	public static JSONObject lookupLocationByLayer(String image, String layer) {
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("type", "lookup_location");
		image = image.replace(shared.PATH, "");
		map.put("image", image);
		map.put("layer", layer);
		return new JSONObject(map);
	}
	@Override
	protected JSONObject doInBackground(JSONObject... params) {
		JSONObject response;

		try {
			response = makeRequest(SITE,
					params[0]);
			
			return response;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	public static JSONObject makeRequest(String path, JSONObject params) throws JSONException {
		Log.d(TAG,"sending request: "+params.toString());
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		HttpPost httpost = new HttpPost(path);
		
		
		StringEntity se;
		try {
			se = new StringEntity(params.toString());
		
		
			httpost.setEntity(se);
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");
			
			HttpResponse response = httpclient.execute(httpost,new BasicHttpContext());
			
			String file = "";
			String line = "";
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			while((line=in.readLine())!=null) {
				file += line;				
			}
			response.getEntity().consumeContent();
			Log.d(TAG,file);
			JSONObject ret = new JSONObject(file);
			
			return ret;
		
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e){
			return new JSONObject("{\"error\": \""+e.getMessage()+"\"}");
		}
		
		
		return new JSONObject("{\"error\": \"network error\"}");
	} 
	@Override
	protected void onPostExecute(JSONObject result){
		if(result!=null){
			
			
			if(result.has("error")){
				String error;
				try {
					error = result.getString("error");
					Log.d(TAG,"found an error: "+error);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}else{
				Log.d(TAG,"on post execute: "+result.toString());
				this.parent.PostExecute(result);
			}
		}
	}
	
	
}
