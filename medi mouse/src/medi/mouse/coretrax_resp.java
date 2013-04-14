package medi.mouse;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class coretrax_resp {
	public int pos;
	private String data;
	public coretrax_resp(String data,int pos){
		this.data = data;
		this.pos = pos;
	}
	public String toString(){
		return data;
	}
	public JSONObject getData(){
		JSONObject ret;
		try {
			ret = new JSONObject(data);
		} catch (JSONException e) {
			HashMap<String,String> error = new HashMap<String,String>();
			error.put("error","malformed JSON string");
			error.put("detail",data);
			
			ret = new JSONObject(error);
		}

		return ret;
	}
}
