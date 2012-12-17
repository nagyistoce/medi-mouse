package medi.mouse;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

public class LoadImageMap extends AsyncTask<String, Float, Bitmap> {
	
	private Canvas canvas;
	private Rect source;
	public Rect dest;
	private String filename;
	private FacilityViewer fv;

	public LoadImageMap(FacilityViewer fv, Rect source, Rect dest, String filename){
		this.fv = fv;
		this.source = source;
		this.dest = dest;
		this.filename = filename;
		
	}
	
	@Override
	protected Bitmap doInBackground(String... params) {
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds=false;
		opts.inSampleSize=1;
		
		BitmapRegionDecoder decoder;
		try {
			decoder = BitmapRegionDecoder.newInstance(filename, false);
			//return highres slice of image
			Log.d("LoadImageMap","getting bitmap: "+source);
			Bitmap ret = decoder.decodeRegion(source, opts);
			if(this.isCancelled()){
				return null;
			}
			return ret;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result){
		if (result != null) {
			Log.d("LoadImageMap","drawing bitmap: "+dest+":"+result.getByteCount());
			fv.highResListener(result,dest);
		}
		//this.mFacView.setBitmap(mBitmap);
	}

}
