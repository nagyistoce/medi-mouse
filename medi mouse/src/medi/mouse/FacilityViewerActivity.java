package medi.mouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.acra.ErrorReporter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FacilityViewerActivity extends medi_mouse_activity {
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
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.building_select);
		progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setMax(100);
		progressBar.setVisibility(View.GONE);
		/*
		 * download schematics
		 * extract zips
		 */
		File appDir = new File(PATH);
		
		appDir.mkdirs();
		File[] files = appDir.listFiles();
		boolean found = false;
		
		
		Downloader dl = new Downloader(FacilityViewerActivity.this, 
				"http://medi-mouse.googlecode.com/files/schematics.zip", 
				new File(PATH+"/schematics.zip"),
				true);
		dl.execute(0);
	
				
	}
	public void LargeFilerAlertAndDownload(){
		LayoutInflater inflater = LayoutInflater.from(this);
	
	   View alertDialogView = inflater.inflate(R.layout.large_file_alert, null);
	   AlertDialog.Builder builder = new AlertDialog.Builder(this);
	   builder.setView(alertDialogView); 
	    
	   builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	progressBar.setVisibility(View.VISIBLE);
	        	Downloader dl = new Downloader(FacilityViewerActivity.this, 
						"http://medi-mouse.googlecode.com/files/schematics.zip", 
						new File(PATH+"/schematics.zip"));
				dl.execute(0);
	            dialog.cancel();
	        }
	    }).show();
	   
	}
	public void buildUI(){
		buildFacSpinners(PATH + "schematics");
		Button load = (Button) findViewById(R.id.load);
		load.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				String image = PATH + "schematics/"+
						facility_spinner.getSelectedItem()+"/"+ 
						floor_spinner.getSelectedItem();
				FacilityViewer fv = new FacilityViewer(FacilityViewerActivity.this,image);
				LinearLayout ll = (LinearLayout) findViewById(R.id.main);
				ll.removeAllViews();
				ll.addView(fv);
				
			}
		
		});

	}
	
	private void buildFacSpinners(final String path){
		Log.d("CoreActivity",":::"+path);
		File file = new File(path);
		facility_spinner = (Spinner) findViewById(R.id.fac_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		File[] files = file.listFiles();
		ArrayList<String> fac = new ArrayList<String>();
		
		for(File f : files){
			fac.add(f.getName());
		}
		Collections.sort(fac);
		
		String[] facilities = new String[files.length];
		int x = 0;
		for(String f : fac){
			facilities[x]=f;
			x++;
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_item, facilities);
		facility_spinner.setSelected(false);
		facility_spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				TextView tv = (TextView) arg0.findViewById(R.id.primary_text_item);
				
				Log.d("CoreActivity","text "+tv.getText());
				FacilityViewerActivity.this.buildFloorSpinners(path+"/"+tv.getText());
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

}
