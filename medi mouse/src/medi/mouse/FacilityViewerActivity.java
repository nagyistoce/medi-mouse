package medi.mouse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FacilityViewerActivity extends medi_mouse_activity implements FacilityPostInterface{
	public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
    public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
    public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
    public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
    public static final int MESSAGE_CONNECTING_STARTED = 1004;
    public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;
    public static final int MESSAGE_EXTRACTION_STARTED = 1006;
    public static final int MESSAGE_EXTRACTION_COMPLETED = 1007;
    
    public static final String PATH = Environment.getExternalStorageDirectory()+"/mm/";
    public static final String TAG = "FacilityViewerActivity";
	private Spinner facility_spinner;
	private Spinner floor_spinner;
	private ProgressBar progressBar;
	private String locationToFind;
	private String building;
	private String layer;
	private boolean isCore;
	
	boolean downloadIncomplete = true;
	private Downloader download;
	private boolean LFADopen;
	private View LargeFilerAlertDialogView;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.building_select);
		progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setMax(100);
		progressBar.setVisibility(View.GONE);
		
		isCore = getIntent().getExtras().getBoolean("core_post", false);
		if(isCore){
			//intent created by core_post
			//should include building and room name info
			building = getIntent().getExtras().getString("building");
			locationToFind = getIntent().getExtras().getString("name");
			//layer name should be "core" if coming from core_post
			layer = getIntent().getExtras().getString("layer");
			startDownload(true);
		} else {
			/*
			 * download schematics
			 * extract zips
			 */
			File appDir = new File(PATH);
			
			appDir.mkdirs();
			//chk file instance of downloader
			//will check to see if the schematics.zip is at the correct file size
			//if it isn't it will warn the user and download the new file.
			startDownload(true);
		}
	
				
	}
	private void lookup_place_and_view(String building, String name) {
		Log.d(TAG,"looking up: "+building+":"+name);
		new FacilityPost(this).execute(FacilityPost.lookupLocationByName(building,name));
	}
	public void LargeFilerAlertAndDownload(){
		LayoutInflater inflater = LayoutInflater.from(this);
		
		LargeFilerAlertDialogView = inflater.inflate(R.layout.large_file_alert, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(LargeFilerAlertDialogView); 
		LFADopen = true;
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				startDownload(false);
				LFADopen = false;
	            dialog.cancel();
	        }
	    }).show();
		
		
	}
	
	private void startDownload(boolean chk){
		progressBar.setVisibility(View.VISIBLE);
		download = new Downloader(FacilityViewerActivity.this, 
				"http://medi-mouse.googlecode.com/files/schematics.zip", 
				new File(shared.PATH+"/schematics.zip"),
				chk);
		download.execute(0);
	}
	public void buildUI(){
		if(isCore){
			downloadIncomplete = false;
			download=null;
			Log.d(TAG,"locationToFind: "+building+":"+locationToFind);
			lookup_place_and_view(building,locationToFind);
		}else{
			buildUI(null,null,null);
		}
	}
	public void buildUI(HashMap<String,ArrayList<String>> floor_filter,
			String building,
			final String findMe){
		buildFacSpinners(PATH + "schematics",floor_filter);
		Button load = (Button) findViewById(R.id.load);
		load.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				String image = PATH + "schematics/"+
						facility_spinner.getSelectedItem()+"/"+ 
						floor_spinner.getSelectedItem();
				String building = facility_spinner.getSelectedItem().toString();
				FacilityViewerActivity.this.setContentView(R.layout.facility);
				FacilityViewer fv;
				TextView label = (TextView) findViewById(R.id.canvas_label);
				LinearLayout hud_layout = (LinearLayout) findViewById(R.id.hud_layout);
				
				String floorname = image.substring(image.lastIndexOf("/")+1,image.lastIndexOf(".jpg"));
				if(findMe!=null){
					fv = new FacilityViewer(FacilityViewerActivity.this,image,building,findMe);
					label.setText(building+":"+floorname+"\n"+findMe);
				} else {
					fv = new FacilityViewer(FacilityViewerActivity.this,image,facility_spinner.getSelectedItem().toString());
					label.setText(building+":"+floorname);

				}
				RelativeLayout ll = (RelativeLayout) findViewById(R.id.main);

				ll.addView(fv,0);
				
			}
		
		});

	}
	private void buildFacSpinners(final String path){
		buildFacSpinners(path,null);
	}
	private void buildFacSpinners(final String path,
			final HashMap<String,ArrayList<String>> floor_filter){
		Log.d("CoreActivity",":::"+path);
		File file = new File(path);
		facility_spinner = (Spinner) findViewById(R.id.fac_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		File[] files = file.listFiles();
		ArrayList<String> fac = new ArrayList<String>();
		if(files==null){
			return;
		}
		for(File f : files){
			Log.d(TAG,f.getName());
			
			if(floor_filter==null||floor_filter.containsKey(f.getName())) {
				fac.add(f.getName());
			}
				
		}
		Collections.sort(fac);
		
		String[] facilities = new String[fac.size()];
		int x = 0;
		for(String f : fac){
			Log.d(TAG,x+": "+f);
			facilities[x]=f;
			x++;
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_item, facilities);
		
		if(facilities.length==0){
			//no facilities found, quit

			Toast.makeText(this, "Unknown Facility, giving up...", Toast.LENGTH_LONG).show();			
			this.finish();
		}else if (facilities.length==1){
			facility_spinner.setEnabled(false);
		}
		
		facility_spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				TextView tv = (TextView) arg0.findViewById(R.id.primary_text_item);
				
				Log.d("CoreActivity","text "+tv.getText());
				if(floor_filter==null){
					FacilityViewerActivity.this.buildFloorSpinners(path+"/"+tv.getText());
				}else{
					FacilityViewerActivity.this.buildFloorSpinners(path+"/"+tv.getText(),
							floor_filter.get(tv.getText()));
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				
			}});
	
	facility_spinner.setAdapter(adapter);
	
	}
	protected void buildFloorSpinners(String path) {
		buildFloorSpinners(path,null);
		
	}
	protected void buildFloorSpinners(String path,ArrayList<String> filter) {
		File file = new File(path);
		floor_spinner = (Spinner) findViewById(R.id.floor_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		File[] files = file.listFiles(); 
		if(files != null){
			String[] facilities = new String[files.length];
			int x = 0;
			for(File f : files){
				if(filter==null||filter.contains(f.getName())){
					facilities[x]=f.getName();
				}
				Log.d("CoreActivity",x+":"+f.getName());
				x++;
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_item, facilities);
			floor_spinner.setSelected(false);
			
			floor_spinner.setAdapter(adapter);
		}
	}
	
	public void PostExecute(JSONObject result) {
		
		JSONObject res = (JSONObject) result;
		Log.d(TAG,"on post execute: "+res.toString());
		String type;
		try {
			type = res.getString("type");
			
			if(type.compareTo("location_list")==0){
				//returning from a lookup_location call
				int places_num = res.getInt("places_num");
				String building = res.getString("building");
				if(places_num>0){
					//some locations have been found 
					ArrayList<Location> locations = new ArrayList<Location>();
					ArrayList<Location> top_ranked = new ArrayList<Location>();
					HashMap<String,Integer> image_ranks = new HashMap<String,Integer>();
					
					JSONArray list = res.getJSONArray("places");
					int max = 0;
					String maxFile = "";
					for(int x=0;x<list.length();x++){
						//create an array list of FacilityViewer.locations
						JSONObject place = list.getJSONObject(x);
						JSONObject pos = place.getJSONObject("position");
						float pos_x = new Float(pos.getString("x"));
						float pos_y = new Float(pos.getString("y"));
						String name = place.getString("name");
						String filename = place.getString("image");
						String layer = place.getString("layer");
						int rank = place.getInt("rank");
						int t = 0;
						if(image_ranks.containsKey(filename)){
							t = image_ranks.get(filename)+rank;
							image_ranks.put(filename, t);
						} else {
							t = rank;
							image_ranks.put(filename, rank);
						}
						if(t>max){
							max = t;
							maxFile = filename;
						}
						Location loc = new Location(pos_x, pos_y, rank, name, building, filename, layer);
						
						locations.add(loc);
					}
					ArrayList<String> num_hits = new ArrayList<String>(); 
					for(Location location: locations){
						String filename = location.getFilename();
						Log.d(TAG,"filename: "+filename+"\nmax: "+maxFile+"\nthis: "+
								location.getRank());
						if(location.getRank()<0){
							//this location obviously sucks, don't even bother
							continue;
						}
						if(filename.compareTo(maxFile)==0){
							top_ranked.add(location);
							if(!num_hits.contains(filename)){
								num_hits.add(shared.PATH+filename);
							}
						}
					}
					
					Log.d(TAG,"hits: "+num_hits.size());
					//if only one image file is used for each location in list, 
					//load file and display all 
					//if multiple image files exist, load file with highest ranking
					if(num_hits.size()==1){
						Log.d(TAG,"loading facilityViewer: "+top_ranked.size());
						setContentView(R.layout.facility);
						FacilityViewer fv = new FacilityViewer(FacilityViewerActivity.this,
								top_ranked,
								num_hits.get(0),
								building);
						RelativeLayout ll = (RelativeLayout) findViewById(R.id.main);
						//ll.removeAllViews();
						TextView tv = (TextView) findViewById(R.id.canvas_label);
						String image = num_hits.get(0);
						String floorname = image.substring(image.lastIndexOf("/")+1,image.lastIndexOf(".jpg"));
						tv.setText(building+":"+floorname);
						ll.addView(fv,0);
					}else if(num_hits.size()>1){
						Log.d(TAG,"to many hits");
						//if multiple image files exist with the same ranking, 
						//load UI and have the user choose a map
						
						HashMap<String,ArrayList<String>> filter =
								new HashMap<String,ArrayList<String>>();
						filter.put(building, num_hits);
						buildUI(filter,building, null);
						
					}else{
						buildUI(null,null,null);
					}
					
					
				} else {
					//No locations found
					Log.d(TAG,"no locations found: "+locationToFind);
					NoLocationsFoundAlert(building,locationToFind);
				}
				
			} else if(type=="layer_list"){
				if(res.getInt("layer_num")>0){
					JSONArray list = res.getJSONArray("layers");
				
					for(int x=0;x<list.length();x++){
						
					}
				}
			} else if(type=="location_saved"){
				//location has been saved
				
				//TODO:
				//notify user of the success 
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void NoLocationsFoundAlert(final String building,
			final String locationToFind) {
		final HashMap<String,ArrayList<String>> filter = new HashMap<String,ArrayList<String>>();
		filter.put(building, null);
		LayoutInflater inflater = LayoutInflater.from(this);

		View alertDialogView = inflater.inflate(R.layout.no_locations_found_alert, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(alertDialogView); 
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	
	        	buildUI(filter,building,locationToFind);
	        }
	    }).show();
	}

	/**
     * This is the Handler for this activity. It will receive messages from the
     * DownloaderThread and make the necessary updates to the UI.
     */
    public Handler activityHandler = new Handler(){
    	public void handleMessage(Message msg)
        {
    			Log.d("CoreActivity","handled message: "+msg.what+", "+msg.arg1+", "+msg.arg2+", "+msg.obj);
    			switch(msg.what){
    			case MESSAGE_UPDATE_PROGRESS_BAR:
    				Log.d(TAG,"update progress: "+msg.arg1);
    				progressBar.setProgress(msg.arg1);
    				break;
    			case MESSAGE_DOWNLOAD_STARTED:
    				Toast.makeText(FacilityViewerActivity.this, 
    						"dowload started", Toast.LENGTH_SHORT).show();
    				progressBar.setVisibility(View.VISIBLE);
    				break;
    			case MESSAGE_EXTRACTION_STARTED:
    				Toast.makeText(FacilityViewerActivity.this, 
    						"extracting files...", Toast.LENGTH_SHORT).show();
    				progressBar.setVisibility(View.VISIBLE);
    				progressBar.setProgress(0);
    				break;
    			case MESSAGE_EXTRACTION_COMPLETED:	
    				progressBar.setVisibility(View.GONE);
    				break;
    			}
                //Toast.makeText(CoreActivity.this, "message: "+msg.what, Toast.LENGTH_LONG).show();
        }
    };

	@Override
	public void onPostExecute(Object result) {
		// TODO Auto-generated method stub
		
	}


    

}
