package medi.mouse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

public class Downloader extends AsyncTask<Integer,Integer,Boolean> {
	// constants
    private static final int DOWNLOAD_BUFFER_SIZE = 2097152;
    public static final String TAG = "Downloader";
    private String downloadUrl;
    private FacilityViewerActivity parentActivity;
    private int size;
	private File target;
	private boolean chk_file;
	private boolean is_checked=false;
	private boolean is_good;
    /**
     * Instantiates a new Downloader object.
     * @param parentActivity Reference to AndroidFileDownloader activity.
     * @param inUrl String representing the URL of the file to be downloaded.
     * @param target location to save file too.
     */
    public Downloader(FacilityViewerActivity parentActivity, String inUrl, File target) {
    	downloadUrl = inUrl != null?inUrl:"";
        this.parentActivity = parentActivity;
        this.target = target;
        chk_file=false;
        parentActivity.downloadIncomplete = true;
    }
    public Downloader(FacilityViewerActivity parentActivity, String inUrl, File target,
    		boolean chk_file){
    	this(parentActivity,inUrl,target);
    	this.chk_file= chk_file;
    }
    public Downloader(Downloader that){
    	this(that.parentActivity,that.downloadUrl,that.target);
    	
    }
    private boolean chk_file(){
    	URL url;
        URLConnection conn;
        int fileSize, lastSlash;
        String fileName;
        boolean ret = false;
        try {
			url = new URL(downloadUrl);
		
	        conn = url.openConnection();
	        conn.setUseCaches(false);
	        long lastmod = conn.getLastModified();
	        
	        Log.d(TAG,"this: "+target.lastModified());
	        Log.d(TAG,"that: "+lastmod);
	        if(target.exists()&&target.lastModified()>=lastmod){
	        	ret = true;
	        }
	        	
        } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.is_checked=true;
        this.is_good = ret;
        Log.d(TAG,"is_good: "+ret);
        return ret;
        
    }
    public boolean isChecked(){
    	return is_checked;
    }
    public boolean isGood(){
    	return is_good;
    }
    public void run()
    {
            URL url;
            URLConnection conn;
            int fileSize, lastSlash;
            String fileName;
            BufferedInputStream inStream;
            BufferedOutputStream outStream;
            //File outFile;
            FileOutputStream fileStream;
            Message msg;
            
            // we're going to connect now
            msg = Message.obtain(parentActivity.activityHandler,
                            FacilityViewerActivity.MESSAGE_CONNECTING_STARTED,
                            0, 0, downloadUrl);
            parentActivity.activityHandler.sendMessage(msg);
            
            try
            {
                    url = new URL(downloadUrl);
                    conn = url.openConnection();
                    conn.setUseCaches(false);
                    long lastmod = conn.getLastModified();
                    size = fileSize = conn.getContentLength();
                    if(target.exists()){
                    	target.delete();
                    }
                    // get the filename
                    lastSlash = url.toString().lastIndexOf('/');
                    
                    // notify download start
                    int fileSizeInKB = fileSize / 1024;
                    msg = Message.obtain(parentActivity.activityHandler,
                    				FacilityViewerActivity.MESSAGE_DOWNLOAD_STARTED,
                                    fileSizeInKB, 0, target.toString());
                    parentActivity.activityHandler.sendMessage(msg);
                    
                    // start download
                    inStream = new BufferedInputStream(conn.getInputStream());
                    
                    fileStream = new FileOutputStream(target);
                    outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
                    byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
                    int bytesRead = 0, totalRead = 0;
                    
                    while(!isCancelled() && (bytesRead = inStream.read(data, 0, data.length)) >= 0)
                    {
                            outStream.write(data, 0, bytesRead);
                            
                            // update progress bar
                            totalRead += bytesRead;
                            int totalReadInKB = totalRead / 1024;
                            
                            //Log.d("Downloader",totalRead+", "+bytesRead);
                            
                            msg = Message.obtain(parentActivity.activityHandler,
                                            FacilityViewerActivity.MESSAGE_UPDATE_PROGRESS_BAR,
                                            (int)(((float)totalReadInKB/fileSizeInKB)*100), 0);
                            parentActivity.activityHandler.sendMessage(msg);
                            
                    }
                    outStream.flush();
                    
                    outStream.close();
                    fileStream.close();
                    inStream.close();
                    boolean failed_lastmod = target.setLastModified(lastmod);
                    Log.d(TAG,"failed_lastmod: "+failed_lastmod);
                    if(isCancelled())
                    {
                            // the download was canceled, so let's delete the partially downloaded file
                            target.delete();
                    }
                    else
                    {
                            // notify completion
                            msg = Message.obtain(parentActivity.activityHandler,
                            		FacilityViewerActivity.MESSAGE_DOWNLOAD_COMPLETE);
                            parentActivity.activityHandler.sendMessage(msg);
                    }
            } catch(MalformedURLException e) {
                    String errMsg = parentActivity.getString(R.string.error_message_bad_url);
                    msg = Message.obtain(parentActivity.activityHandler,
                    				FacilityViewerActivity.MESSAGE_ENCOUNTERED_ERROR,
                                    0, 0, errMsg);
                    parentActivity.activityHandler.sendMessage(msg);
            } catch(FileNotFoundException e) {
                    String errMsg = parentActivity.getString(R.string.error_message_file_not_found);
                    msg = Message.obtain(parentActivity.activityHandler,
                                    FacilityViewerActivity.MESSAGE_ENCOUNTERED_ERROR,
                                    0, 0, errMsg);
                    parentActivity.activityHandler.sendMessage(msg); 
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
    }

    
	@Override
	protected Boolean doInBackground(Integer... params) {
		if(chk_file){
			return chk_file();
		}else{
			run();
			Log.d("Downloader","done...");
			File appDir = new File(Environment.getExternalStorageDirectory()+"/mm/");
			Log.d("Downloader",appDir.toString());
			File[] files = appDir.listFiles();
			boolean found = false;
			if(files!=null){
				for(File f : files){
					if(f.toString().compareTo(target.toString())==0){
						found=true;
						extract(target);
					}
				}
				
			}
			this.is_checked=true;
	        this.is_good = true;
			return true;
		}
		
	}
	
	private void extract(File file){
		try {
			Message msg;
			msg = Message.obtain(parentActivity.activityHandler,
                    FacilityViewerActivity.MESSAGE_EXTRACTION_STARTED,
                    0, 0);
			Log.d(TAG,"start extract");
			parentActivity.activityHandler.sendMessage(msg); 
			String destinationPath = parentActivity.PATH +
					System.getProperty("file.separator") + 
					file.getName().replaceFirst(".zip", "") +
					System.getProperty("file.separator");
			
			
			File destination = new File(destinationPath);
			recursiveDelete(destination);
			new File(destinationPath).mkdirs();
			ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
			
			size *= 1.25;
			ZipEntry entry = zis.getNextEntry();
			byte[] buf = new byte[1024];
			int totalReadIn=0;
			while(entry!=null){
				String outFilePath = destinationPath + 
						 entry.getName();
				if(entry.isDirectory()){
					new File(destinationPath+entry.getName()).mkdirs();
					//Log.d("Downloader","Create dir: "+entry.getName());
					entry = zis.getNextEntry();
					continue;
				}
				Log.d(TAG,"Extracting "+outFilePath);
				File outFile = new File(outFilePath);
				if(outFile.exists()&&outFile.length()==entry.getSize()){
					//already extracted
					
					entry = zis.getNextEntry();
					totalReadIn+=outFile.length();
					Log.d(TAG,"already extracted, skipping..."+(int)(((float)totalReadIn/size)*100));

					msg = Message.obtain(parentActivity.activityHandler,
                            FacilityViewerActivity.MESSAGE_UPDATE_PROGRESS_BAR,
                            (int)(((float)totalReadIn/size)*100), 0);
					parentActivity.activityHandler.sendMessage(msg);
					continue;
				}
				
				FileOutputStream fileoutputstream = new FileOutputStream(outFile); 
				
				int n;
				while ((n = zis.read(buf, 0, 1024)) > -1)
	                    fileoutputstream.write(buf, 0, n);
				
				fileoutputstream.close();
				
				totalReadIn+=outFile.length();
				Log.d(TAG,"update progress: "+(int)(((float)totalReadIn/size)*100));
				msg = Message.obtain(parentActivity.activityHandler,
                        FacilityViewerActivity.MESSAGE_UPDATE_PROGRESS_BAR,
                        (int)(((float)totalReadIn/size)*100), 0);
				parentActivity.activityHandler.sendMessage(msg);
				Log.d("CoreActivity","Done");
				entry = zis.getNextEntry();
				
			}
			zis.close();
			Log.d(TAG,"Extraction complete");
			msg = Message.obtain(parentActivity.activityHandler,
                    FacilityViewerActivity.MESSAGE_EXTRACTION_COMPLETED,
                    0, 0);
			parentActivity.activityHandler.sendMessage(msg);
            
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private void recursiveDelete(File destination) {
		if(destination.exists()){
			if(destination.isDirectory()){
				for(File file: destination.listFiles()){
					recursiveDelete(file);
				}
			}
			destination.delete();
		}

	}
	@Override
	protected void onPostExecute(Boolean ret){
		if(this.isCancelled()){
			//cancalled, do nothing
			return;
		}
		if(chk_file){
			Log.d(TAG,"load alert and download"+ret);
			if(!ret){
				parentActivity.LargeFilerAlertAndDownload();
				
			}
			else{
				parentActivity.buildUI();
			}
		} else {
			parentActivity.buildUI();
		}
		
	}
	
	

}
