package medi.mouse;

import java.util.HashMap;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

public class Location{
	private int img;
	private String tag;
	private float pos_x,pos_y;
	private String filename;
	private String building;
	private String layer;
	private int rank;
	private static String TAG = "Location";
	public Location(float pos_x,float pos_y,
			int rank,
			String tag,
			String building,
			String filename,
			String layer){
		this.tag = tag;
		this.pos_x = pos_x;
		this.pos_y = pos_y;
		this.rank = rank;
		this.img = R.drawable.red_x;
		this.building = building;
		this.filename = filename;
		
	}
	public Location(float pos_x,float pos_y,
			int rank,
			String tag,
			String building,
			String filename,
			String layer,
			int img){
		this(pos_x,pos_y,rank,tag,building,filename,layer);
		this.img = img;
	}
	public Location(float pos_x,float pos_y){
		//dummy location to compare event locations to locations
		this.pos_x=pos_x;
		this.pos_y=pos_y;
		
	}
	public JSONObject toJson(){
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("name", tag);
		map.put("building", building);
		map.put("layer", layer);
		HashMap<String,Object> position = new HashMap<String,Object>();
		position.put("x", pos_x);
		position.put("y", pos_y);
		map.put("position", new JSONObject(position));
		filename = filename.replace(shared.PATH, "");
		map.put("image", filename);
		
		return new JSONObject(map);
	}
	public String getName(){
		return this.tag;
	}
	public int getRank(){
		return this.rank;
	}
	public int addRank(int rank){
		this.rank+=rank;
		return this.rank;
	}
	public String getBuilding(){
		return this.building;
	}
	
	public String getFilename(){
		return filename;
	}
	public String toString(){
		return tag+":"+building;
	}
	public void render(Canvas canvas, View view,int width,int height){
		float x = width*pos_x;
		float y = height*pos_y;

		//BitmapFactory.Options opts = new BitmapFactory.Options();
		//opts.inDensity=1;
		Log.d(TAG,"location: "+x+","+y+":"+img);
		Bitmap icon = BitmapFactory.decodeResource(view.getResources(),img);
		
		canvas.drawBitmap(icon, x-icon.getWidth()/2,y-icon.getHeight()/2,null);

	}
	public float compair(Location that){
		double dx = Math.pow(this.pos_x-that.pos_x,2);
		double dy = Math.pow(this.pos_y-that.pos_y,2);
		
		return (float)Math.sqrt(dx+dy)*100;
	}
}
