package medi.mouse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

public class Downloader extends AsyncTask<Integer,Integer,Integer> {
	// constants
    private static final int DOWNLOAD_BUFFER_SIZE = 2097152;
    
    private String downloadUrl;
    private CoreActivity parentActivity;

	private File target;
    /**
     * Instantiates a new Downloader object.
     * @param parentActivity Reference to AndroidFileDownloader activity.
     * @param inUrl String representing the URL of the file to be downloaded.
     * @param target location to save file too.
     */
    public Downloader(CoreActivity parentActivity, String inUrl, File target) {
    	downloadUrl = inUrl != null?inUrl:"";
        this.parentActivity = parentActivity;
        this.target = target;
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
                            CoreActivity.MESSAGE_CONNECTING_STARTED,
                            0, 0, downloadUrl);
            parentActivity.activityHandler.sendMessage(msg);
            
            try
            {
                    url = new URL(downloadUrl);
                    conn = url.openConnection();
                    conn.setUseCaches(false);
                    fileSize = conn.getContentLength();
                    
                    // get the filename
                    lastSlash = url.toString().lastIndexOf('/');
                    
                    // notify download start
                    int fileSizeInKB = fileSize / 1024;
                    msg = Message.obtain(parentActivity.activityHandler,
                    				CoreActivity.MESSAGE_DOWNLOAD_STARTED,
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
                            /*
                            msg = Message.obtain(parentActivity.activityHandler,
                                            CoreActivity.MESSAGE_UPDATE_PROGRESS_BAR,
                                            totalReadInKB, 0);
                            parentActivity.activityHandler.sendMessage(msg);
                            */
                    }
                    outStream.flush();
                    
                    outStream.close();
                    fileStream.close();
                    inStream.close();
                    
                    if(isCancelled())
                    {
                            // the download was canceled, so let's delete the partially downloaded file
                            target.delete();
                    }
                    else
                    {
                            // notify completion
                            msg = Message.obtain(parentActivity.activityHandler,
                            		CoreActivity.MESSAGE_DOWNLOAD_COMPLETE);
                            parentActivity.activityHandler.sendMessage(msg);
                    }
            } catch(MalformedURLException e) {
                    String errMsg = parentActivity.getString(R.string.error_message_bad_url);
                    msg = Message.obtain(parentActivity.activityHandler,
                    				CoreActivity.MESSAGE_ENCOUNTERED_ERROR,
                                    0, 0, errMsg);
                    parentActivity.activityHandler.sendMessage(msg);
            } catch(FileNotFoundException e) {
                    String errMsg = parentActivity.getString(R.string.error_message_file_not_found);
                    msg = Message.obtain(parentActivity.activityHandler,
                                    CoreActivity.MESSAGE_ENCOUNTERED_ERROR,
                                    0, 0, errMsg);
                    parentActivity.activityHandler.sendMessage(msg); 
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
    }

    
	@Override
	protected Integer doInBackground(Integer... params) {
		run();
		
		return 0;
	}
	@Override
	protected void onPostExecute(Integer ret){
		Log.d("Downloader","done...");
		File appDir = new File(Environment.getExternalStorageDirectory()+"/mm/");
		Log.d("Downloader",appDir.toString());
		File[] files = appDir.listFiles();
		boolean found = false;
		if(files!=null){
			for(File f : files){
				Log.d("Downloader",f.toString());
				if(f.toString()==appDir+"/Framingham.zip"){
					found=true;
				}
			}
		}
	}
}
