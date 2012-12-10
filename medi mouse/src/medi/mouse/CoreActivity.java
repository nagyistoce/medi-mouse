package medi.mouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CoreActivity extends medi_mouse_activity {
	public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
    public static final int MESSAGE_DOWNLOAD_COMPLETE = 1001;
    public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
    public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
    public static final int MESSAGE_CONNECTING_STARTED = 1004;
    public static final int MESSAGE_ENCOUNTERED_ERROR = 1005;
    
    public static final String PATH = Environment.getExternalStorageDirectory()+"/mm/";
	private Spinner facility_spinner;
	private Spinner floor_spinner;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.building_select);
		
		/*
		 * download schematics
		 * extract zips
		 */
		File appDir = new File(PATH+"schematics");
		
		appDir.mkdirs();
		File[] files = appDir.listFiles();
		boolean found = false;
		
		if(files!=null&&files.length>0){
			Log.d("CoreActivity","files "+files[0]);
			for(File f : files){
				
				Log.d("CoreActivity",f.getName());
				if(f.getName().compareTo("Framingham")==0){
					found=true;
				}
				
			}
		}
		if(!found){
			Log.d("CoreActivity","downloading...");
			Downloader dl = new Downloader(this, 
					"https://medi-mouse.googlecode.com/files/Framingham.zip", 
					new File(PATH+"/Framingham.zip"));
			dl.execute(0);
		} else {
			buildUI();
		}
				
	}
	
	public void buildUI(){
		buildFacSpinners(PATH + "schematics");
		Button load = (Button) findViewById(R.id.load);
		load.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				String image = PATH + "schematics/"+
						facility_spinner.getSelectedItem()+"/"+ 
						floor_spinner.getSelectedItem();
				FacilityViewer fv = new FacilityViewer(CoreActivity.this,image);
				LinearLayout ll = (LinearLayout) findViewById(R.id.main);
				ll.addView(fv);
				Log.d("CoreActivity","blam");
				
			}
		
		});

	}
	
	private void buildFacSpinners(final String path){
		Log.d("CoreActivity",":::"+path);
		File file = new File(path);
		facility_spinner = (Spinner) findViewById(R.id.fac_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		File[] files = file.listFiles(); 
		String[] facilities = new String[files.length];
		int x = 0;
		for(File f : files){
			facilities[x]=f.getName();
			Log.d("CoreActivity",x+":"+f.getName());
			x++;
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_item, facilities);
		facility_spinner.setSelected(false);
		
		facility_spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				TextView tv = (TextView) arg0.findViewById(R.id.primary_text_item);
				
				Log.d("CoreActivity","text "+tv.getText());
				CoreActivity.this.buildFloorSpinners(path+"/"+tv.getText());
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		facility_spinner.setAdapter(adapter);
			
	}
	protected void buildFloorSpinners(String path) {
		File file = new File(path);
		floor_spinner = (Spinner) findViewById(R.id.floor_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		File[] files = file.listFiles(); 
		if(files != null){
			String[] facilities = new String[files.length];
			int x = 0;
			for(File f : files){
				facilities[x]=f.getName();
				Log.d("CoreActivity",x+":"+f.getName());
				x++;
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_item, facilities);
			floor_spinner.setSelected(false);
			
			floor_spinner.setAdapter(adapter);
		}
	}
	
	@Override
	public void onPostExecute(medi_person result) {
		
	}
	
	/**
     * This is the Handler for this activity. It will receive messages from the
     * DownloaderThread and make the necessary updates to the UI.
     */
    public Handler activityHandler = new Handler(){
    	public void handleMessage(Message msg)
        {
    			Log.d("CoreActivity","handled message: "+msg.what+", "+msg.arg1+", "+msg.arg2+", "+msg.obj);
    			
                //Toast.makeText(CoreActivity.this, "message: "+msg.what, Toast.LENGTH_LONG).show();
        }
    };

}
