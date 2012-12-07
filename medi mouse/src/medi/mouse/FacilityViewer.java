package medi.mouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;


public class FacilityViewer extends View {

	private static final long LONGPRESS_THRESHOLD = 500;
	private int mImage;
    private Bitmap mBitmap;
	private float mPosX=0;
    private float mPosY=0;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = MotionEvent.INVALID_POINTER_ID;

    private int screenWidth;
    private int screenHeight;
    private int curX=0;
    private int curY=0;
    private ScaleGestureDetector mScaleDetector;
    private OnLongClickListener mLongTouchDetector;
    LongPressTimer longpressTimer = new LongPressTimer();
    private float mScaleFactor = 1/4.f;
	private int picture_width;
	private int picture_height;
	private Point spot;
	private boolean markSpot = false;
	Vibrator vibrator;
	private Rect clipBounds_canvas;
	private Bitmap lowres_image;
	public FacilityViewer(Context context) {
		this(context,null,0);
	}
	public FacilityViewer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
	public FacilityViewer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE); 

	
    }
	
	public FacilityViewer(CoreActivity context,String filename) {
		this(context);
		context.setContentView(R.layout.facility);
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(filename, opts);
		
		opts.inJustDecodeBounds=false;
		opts.inSampleSize=4;
		lowres_image = BitmapFactory.decodeFile(filename, opts);
		invalidate();
	}
	public void setBitmap(Bitmap bitmap){
		mBitmap = bitmap;
		
		Log.d("FacilityViewer", "invalidate");
	}
	
	
	@Override
    public boolean onTouchEvent(final MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        final int action = ev.getAction();
        
        switch (action) {
        
        case MotionEvent.ACTION_DOWN: {
        	Log.d("FacilityView","DOWN: "+ev.getEventTime());
            final float x = ev.getX();
            final float y = ev.getY();
            
            mLastTouchX = x;
            mLastTouchY = y;
            
            float mX = ev.getX() / (mScaleFactor) + clipBounds_canvas.left;
            float mY = ev.getY() / (mScaleFactor) + clipBounds_canvas.top;
            
            mActivePointerId = ev.getPointerId(0);
            
            longpressTimer = new LongPressTimer();
            longpressTimer.execute(mX,mY);
            break;
        }

        case MotionEvent.ACTION_MOVE: {
        	Log.d("FacilityView","MOVE: "+ev.getEventTime());
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);
        
            // Only move if the ScaleGestureDetector isn't processing a gesture.
            if (!mScaleDetector.isInProgress()) {
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;
                Log.d("FacilityViewer","dx/dy: "+dx+"/"+dy);
                if(dx*dx+dy*dy>3){
                	longpressTimer.cancel(true);
	                mPosX += dx;
	                mPosY += dy;
                
	                invalidate();
                }else{
                	return false;
                }

            } else {
            	longpressTimer.cancel(true);
            	return false;
            }
	        mLastTouchX = x;
	        mLastTouchY = y;
            break;
        }
        
        case MotionEvent.ACTION_UP: {
        	longpressTimer.cancel(true);
        	Log.d("FacilityView","UP: "+ev.getEventTime());
            mActivePointerId = MotionEvent.INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_CANCEL: {
        	Log.d("FacilityView","CANCEL: "+ev.getEventTime());
            mActivePointerId = MotionEvent.INVALID_POINTER_ID;
            break;
        }
        
        case MotionEvent.ACTION_POINTER_UP: {
        	Log.d("FacilityView","POINTER_UP: "+ev.getEventTime());
            final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastTouchX = ev.getX(newPointerIndex);
                mLastTouchY = ev.getY(newPointerIndex);
                mActivePointerId = ev.getPointerId(newPointerIndex);
            }
            break;
         
        }
        default:
        	Log.d("FacilityView","NOTHING: "+ev.getEventTime());
        }

        return true;
        
    }
	protected void longpressHandler(float x, float y) {
		
        spot = new Point();
        spot.x=(int) x;
        spot.y=(int) y;
        markSpot = true;
        if(vibrator.hasVibrator()){
			vibrator.vibrate(100);
		}
        invalidate();

		
	}
	
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*
         * 
         * render the low res image on the whole canvas
         * when you zoom in you can render a piece of the 
         * high def version close up
         */
        canvas.save();
        
        
        //canvas.translate(-mPosX * mScaleFactor , -mPosY * mScaleFactor);
        canvas.translate(mPosX, mPosY);
        
        clipBounds_canvas = canvas.getClipBounds();
        float centerX = clipBounds_canvas.left + (clipBounds_canvas.right-clipBounds_canvas.left)/2;
        float centerY = clipBounds_canvas.top + (clipBounds_canvas.bottom-clipBounds_canvas.top)/2;
        
        canvas.scale(mScaleFactor, mScaleFactor,centerX,centerY);
        //canvas.translate(2*mPosX, 2*mPosY);
        
        //canvas.scale(4, 4, centerX, centerY);
        
        canvas.drawBitmap(lowres_image,0, 0, null);
        
		clipBounds_canvas = canvas.getClipBounds();
		if(mScaleFactor>=1) {
			//load high res image
			
			
		}
		
		
		Log.d("FacilityViewer",clipBounds_canvas.left+","+clipBounds_canvas.top+":"+
				clipBounds_canvas.right+","+clipBounds_canvas.bottom);
		if(markSpot){
			
			int size = 15;
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStrokeWidth(4);
			Log.d("FacilityViewer","marking spot: "+spot.x+","+spot.y);
			canvas.drawLine(spot.x-size, spot.y+size, spot.x+size, spot.y-size, paint);
			canvas.drawLine(spot.x-size, spot.y-size, spot.x+size, spot.y+size, paint);
			
		}
        canvas.restore();
    }
    private class LongPressTimer extends AsyncTask<Float,Void,Float[]>{

		@Override
		protected Float[] doInBackground(Float... params) {
			try {
				Thread.sleep(LONGPRESS_THRESHOLD);
			} catch (InterruptedException e) {
				// canceled, do nothing
			}
			
			return params;
		}
		@Override
		protected void onPostExecute(Float[] result){
			
			FacilityViewer.this.longpressHandler(result[0],result[1]);
		}
    	
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
        	
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.05f, Math.min(mScaleFactor, 10.0f));
            Log.d("FacilityViewer","scalling... "+mScaleFactor);
            invalidate();
            return true;
        }
    }



}
