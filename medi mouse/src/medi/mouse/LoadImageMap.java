package medi.mouse;

import java.io.File;
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
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

public class LoadImageMap extends AsyncTask<String, Float, Bitmap[][]> {
	private Context context;
	private FacilityViewer mFacView;
	private int tlX,tlY,brX,brY;
	ProgressBar progressBar;
	public LoadImageMap(Canvas canvas, Rect source, Rect dest, File image){
		
		
		
	}
	
	@Override
	protected Bitmap[][] doInBackground(String... params) {
		String filename = params[0];
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;
		int screenHeight = size.y;
		
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds=true;
		
		try {
			Bitmap pictures = BitmapFactory.decodeStream(
					new FileInputStream(new File(filename)), null, o);
			
			int picture_width = o.outWidth;
			int picture_height = o.outHeight;
			
			InputStream istream =   null;
		    istream = new FileInputStream(new File(filename));
			BitmapRegionDecoder decoder = null;
	        try {
	        	decoder = BitmapRegionDecoder.newInstance(istream, false);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
	        
	        
	        int xdiv = picture_width/screenWidth+1;
			int ydiv = picture_height/screenHeight+1;
			Bitmap[][] mBitmap = new Bitmap[xdiv][ydiv];
			float progress = 0;
			float total = xdiv*ydiv;
			for (int x=0;x<xdiv;x++){
				int cX = x*screenWidth;
				int eX = cX+screenWidth;
				for(int y=0;y<ydiv;y++){
					progress++;
					publishProgress(progress/total);
					int cY = y*screenHeight;
					int eY = cY+screenHeight;
					Bitmap bMap = decoder.decodeRegion(new Rect(cX,cY, eX,eY), null);
					mBitmap[x][y]=bMap;
				}
			}
			return mBitmap;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
		
	}
	
	@Override
	protected void onProgressUpdate(Float... values){
		progressBar.setProgress((int) (values[0]*100));
	}
	
	@Override
	protected void onPostExecute(Bitmap[][] mBitmap){
		progressBar.setVisibility(View.GONE);
		//this.mFacView.setBitmap(mBitmap);
	}

}
