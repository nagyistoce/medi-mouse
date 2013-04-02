package medi.mouse;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class coretrax_args {
	public int pos;
	private String type;
	private String username;
	private String password;
	private String[] path;
	private HashMap<String,String> extra;
	private JSONObject data;
	public coretrax_args(String type,
			String username, 
			String password,
			String[] path,
			HashMap<String,String> extra){
		this.type = type;
		this.username = username;
		this.password = password;
		this.path = path;
		this.extra = extra;
		this.data = buildData();
	}
	public coretrax_args(String type,
			String username, 
			String password){
		this(type,username,password,null,null);
	}
	public coretrax_args(String json){
		try {
			this.data = new JSONObject(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public JSONObject buildData(){
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("type",type);
		map.put("username",username);
		map.put("password",password);
		if(path!=null){
			map.put("path",path);
		}
		if(extra!=null){
			map.put("extra",extra);
		}
		JSONObject data = new JSONObject(map);
		return data;
	}
	public JSONObject getData(){
		return data;
	}
}
