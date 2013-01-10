package medi.mouse;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;


public class FacilityViewer extends View implements FacilityPostInterface{
	
	private static final long LONGPRESS_THRESHOLD = 500;
	private static final String TAG = "FacilityViewer";
	
	private float mPosX=0;
    private float mPosY=0;

    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = MotionEvent.INVALID_POINTER_ID;

    private int screenWidth;
    private ScaleGestureDetector mScaleDetector;
    LongPressTimer longpressTimer = new LongPressTimer();
    private float mScaleFactor = 1.f;
	private Point spot;
	private boolean markSpot = false;
	Vibrator vibrator;
	private Rect clipBounds_canvas;
	private Bitmap lowres_image;
	private String filename;
	private Bitmap highResImg;
	private Rect highResRect;
	private LoadImageMap loadHighresImg;
	private int LowresSampleSize;
	private int high_width;
	private int high_height;
	private int low_width;
	private int low_height;
	private ArrayList<Location> locations;
	private String location_to_find;
	private boolean find;
	private String building;
	private CharSequence floorname;
	private boolean confirming;
	private FacilityViewerActivity context;
	private boolean dontLoadNewLocations;
	
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
        locations = null;
        location_to_find = "";
        confirming = false;
    }
	
	/*
	 * TODO
	 * 
	 * needs to be able to display a list of locations
	 * probably with names attached
	 * 
	 * prompt user to save location after a location is selected.
	 */
	public FacilityViewer(FacilityViewerActivity context,
			String filename,
			String building) {
		this(context);
		this.context = context;
		this.building= building;
		this.filename = filename;
		lookupLayers(filename);
		loadFile(context,filename);
	}
	public FacilityViewer(FacilityViewerActivity context,
			String filename,
			String building,
			String locationName) {
		this(context,filename,building);
		Log.d(TAG,"location: "+locationName);
		if(locationName!=null){
			this.location_to_find = locationName;
			this.find = true;
		}
		
		loadFile(context,filename);
	}
	public FacilityViewer(FacilityViewerActivity context,
			ArrayList<Location> locations,
			String filename,
			String building){
		this(context,filename,building);
		this.locations = locations;
		this.dontLoadNewLocations = true;
		loadFile(context,filename);
	}
	public void lookupLayers(String filename){
		FacilityPost fp = new FacilityPost(this);
		fp.execute(FacilityPost.lookupLayers(filename));
	}
	@SuppressWarnings("deprecation")
	public void loadFile(FacilityViewerActivity context, String filename){
		this.filename=filename;
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		try{
			display.getSize(size);
			screenWidth = size.x;
		} catch (java.lang.NoSuchMethodError ignore){
			//device might not have getSize :(
			screenWidth = display.getWidth();
		}
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds=true;
		opts.inSampleSize=1;
		BitmapFactory.decodeFile(filename, opts);
		high_width = opts.outWidth;
		high_height = opts.outHeight;
		LowresSampleSize = Math.round(Math.max(opts.outWidth, opts.outHeight)/(float)screenWidth);
		opts.inJustDecodeBounds=false;
		opts.inSampleSize=LowresSampleSize;
		Log.d(TAG,"Sample size: "+LowresSampleSize);
		lowres_image = BitmapFactory.decodeFile(filename, opts);
		low_width = opts.outWidth;
		low_height = opts.outHeight;
		invalidate();
	}
	
	@Override
    public boolean onTouchEvent(final MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        final int action = ev.getAction();
        
        switch (action) {
        
        case MotionEvent.ACTION_DOWN: {
        	//Log.d(TAG,"DOWN: "+ev.getEventTime());
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
            if(pointerIndex==-1){
            	break;
            }
            //Log.d(TAG,"pointerIndex: "+pointerIndex);
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);
        
            // Only move if the ScaleGestureDetector isn't processing a gesture.
            if (!mScaleDetector.isInProgress()) {
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;
                //Log.d("FacilityViewer","dx/dy: "+dx+"/"+dy);
                if(dx*dx+dy*dy>3){
                	longpressTimer.cancel(true);
	                mPosX += dx/mScaleFactor;
	                mPosY += dy/mScaleFactor;
                
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
        	Log.d("FacilityView","ACTION_UP: "+ev.getEventTime());
        	
			float p_x = (ev.getX() / (mScaleFactor) + clipBounds_canvas.left)/low_width;
			float p_y = (ev.getY() / (mScaleFactor) + clipBounds_canvas.top)/low_height;
			Location dummy = new Location(p_x,p_y);
			if(locations!=null){
				for(Location location: locations){
					float d = dummy.compair(location);
					if(d<2.){
						//vote
						VoteOnLocation(location);
						break;
					}
					Log.d(TAG,"compairing: "+d);
				}
			}

    	
        	//Log.d("FacilityView","UP: "+ev.getEventTime());
        	/*
        	 * as long as this wasn't a move event
        	 * I should check if a location was clicked
        	 * a dialog should be displayed to vote/change
        	 * the current location
        	 * 
        	 */
        	
            mActivePointerId = MotionEvent.INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_CANCEL: {
        	//Log.d("FacilityView","CANCEL: "+ev.getEventTime());
            mActivePointerId = MotionEvent.INVALID_POINTER_ID;
            break;
        }
        
        case MotionEvent.ACTION_POINTER_UP: {
        	Log.d("FacilityView","ACTION_POINTER_UP: "+ev.getEventTime());
        	
        	}
        	//Log.d("FacilityView","POINTER_UP: "+ev.getEventTime());
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
         
        
        default:
        	Log.d(TAG,"NOTHING: "+ev.getEventTime());
        }

        return true;
        
    }
	protected void longpressHandler(float x, float y) {
		
        spot = new Point();
        spot.x=(int) x;
        spot.y=(int) y;
        markSpot = true;
        try{
	        if(vibrator.hasVibrator()){
				vibrator.vibrate(100);
			}
        }catch (NoSuchMethodError e){
        	//no vibrator :(
        }
        invalidate();

		
	}
	
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /* render the low res image on the whole canvas
         * when you zoom in you can render a piece of the 
         * high def version close up
         */
        canvas.save();
        
        
        
        canvas.translate(mPosX, mPosY);
        clipBounds_canvas = canvas.getClipBounds();
        float centerX = clipBounds_canvas.exactCenterX();
        float centerY = clipBounds_canvas.exactCenterY();
        canvas.scale(mScaleFactor, mScaleFactor,centerX,centerY);
        canvas.drawBitmap(lowres_image,0, 0, null);
        
        
		
		clipBounds_canvas = canvas.getClipBounds();
		
		
		
		if(mScaleFactor/LowresSampleSize>=.8) {
			if(highResImg!=null&&
	        		highResRect!=null){
	        	canvas.drawBitmap(highResImg, null, highResRect, null);
	        }
			
			
			
			Rect dest_rect = clipBounds_canvas;
			Rect source_rect = new Rect(Math.round(((float)dest_rect.left)/low_width*high_width),
					Math.round(((float)dest_rect.top)/low_height*high_height),
					Math.round(((float)dest_rect.right)/low_width*high_width),
					Math.round(((float)dest_rect.bottom)/low_height*high_height));
			Log.d(TAG,"last: "+highResRect+"\n"+"next: "+dest_rect+":::"+LowresSampleSize);
			if(highResRect==null||!highResRect.contains(dest_rect)){
				Log.d(TAG,"load high res");
				Log.d(TAG,"source: ("+((float)source_rect.left)/high_width+", "+
						((float)source_rect.top)/high_height+"),("+
						((float)source_rect.right)/high_width+", "+
						((float)source_rect.bottom)/high_height+")");
				Log.d(TAG,"dest: ("+
						((float)dest_rect.left)/low_width+", "+
						((float)dest_rect.top)/low_height+"),("+
						((float)dest_rect.right)/low_width+", "+
						((float)dest_rect.bottom)/low_height+")");
				if(loadHighresImg!=null){
					if(!loadHighresImg.dest.contains(dest_rect)){
						loadHighresImg.cancel(true);
						loadHighresImg = new LoadImageMap(this,source_rect,dest_rect,filename);
						loadHighresImg.execute("");
					}
						
				} else {
					loadHighresImg = new LoadImageMap(this,source_rect,dest_rect,filename);
					loadHighresImg.execute("");
				}
				
			}
			
		}
		
		
		if(locations!=null){
			Log.d(TAG,"locations: "+locations.size());
			for(Location location: locations){
				
				location.render(canvas, this, low_width, low_height);
			}
		}
		if(markSpot){
			
			int size = 15;
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStrokeWidth(4);
			
			canvas.drawLine(spot.x-size, spot.y+size, spot.x+size, spot.y-size, paint);
			canvas.drawLine(spot.x-size, spot.y-size, spot.x+size, spot.y+size, paint);
			if(!confirming){
				confirming=true;
				if(find){
					//ask if this is the correct location
					LocationConfirmationAlert(building,
							location_to_find,
							filename,
							(float)spot.x/low_width,
							(float)spot.y/low_height);
				}else{
					LocationConfirmationAlert(building,
							null,
							filename,
							(float)spot.x/low_width,
							(float)spot.y/low_height);
				}
			}
		}
        canvas.restore();
    }
    private void VoteOnLocation(final Location location){
    	Log.d(TAG,"vote this");
    	LayoutInflater inflater = LayoutInflater.from(this.getContext());

		View alertDialogView = inflater.inflate(R.layout.location_vote, null);
		
		EditText building_name_view = (EditText)alertDialogView.findViewById(R.id.building_name);
		building_name_view.setText(location.getBuilding());
		building_name_view.setClickable(false);
		building_name_view.setKeyListener(null);
		
		EditText room_name_view = (EditText)alertDialogView.findViewById(R.id.room_name);
		room_name_view.setText(location.getName());
		room_name_view.setClickable(false);
		room_name_view.setKeyListener(null);
		
		final TextView rank_view = (TextView) alertDialogView.findViewById(R.id.rank);
		rank_view.setText(" "+location.getRank());
		
		ImageView up_view = (ImageView) alertDialogView.findViewById(R.id.up);
		up_view.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				new FacilityPost(
						(FacilityPostInterface) FacilityViewer.this.getContext()).execute(
								FacilityPost.voteLocation(location, 1));
				rank_view.setText(" "+location.addRank(1));
			}});
		ImageView down_view = (ImageView) alertDialogView.findViewById(R.id.down);
		down_view.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				new FacilityPost(
						(FacilityPostInterface) FacilityViewer.this.getContext()).execute(
								FacilityPost.voteLocation(location, -1));
				rank_view.setText(" "+location.addRank(-1));
				
			}});
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
		builder.setView(alertDialogView); 
		builder.setPositiveButton("Ok", null).show();
    }
    
    private void LocationConfirmationAlert(final String building, 
    		final String name, 
    		final String filename, 
    		final float x, 
    		final float y) {
    	Log.d(TAG,"confirm this");
		LayoutInflater inflater = LayoutInflater.from(this.getContext());

		View alertDialogView = inflater.inflate(R.layout.save_location_new_label, null);
		
		final EditText building_name_view = (EditText)alertDialogView.findViewById(R.id.building_name);
		building_name_view.setText(building);
		if(building!=null){
			building_name_view.setText(building);
			building_name_view.setEnabled(false);
		}
		final EditText room_name_view = (EditText)alertDialogView.findViewById(R.id.room_name);
		if(name!=null){
			room_name_view.setText(name);
			room_name_view.setEnabled(false);
			
		}
		
		final EditText layer_name_view = (EditText)alertDialogView.findViewById(R.id.layer_name);
		if(name!=null){
			layer_name_view.setText("core");
			layer_name_view.setEnabled(false);
		}
		
		//-------------------------------------------------------------------------------
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
		builder.setView(alertDialogView); 
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	FacilityViewer.this.markSpot=false;
	        	FacilityViewer.this.spot=null;
	        	FacilityPost fp = new FacilityPost((FacilityPostInterface) FacilityViewer.this.getContext());
	        	String bldg = building==null?building_name_view.getText().toString():building;
	        	String layer = layer_name_view.getText().toString();
	        	String n = name==null?room_name_view.getText().toString():name;
	        	fp.execute(FacilityPost.saveLocation(building, 
	        			layer,
	        			n, filename, x, y));
	        	FacilityViewer.this.confirming = false;
	        	
	        }
	    });
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				FacilityViewer.this.markSpot=false;
	        	FacilityViewer.this.spot=null;
	        	FacilityViewer.this.confirming = false;
	        	FacilityViewer.this.invalidate();
			}
		}).show();
	    
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
            //Log.d("FacilityViewer","scalling... "+mScaleFactor);
            invalidate();
            return true;
        }
    }
	public void highResListener(Bitmap highResImg, Rect dest) {
		Log.d("FacilityViewer","saving highres bitmap");
		this.highResImg = highResImg;
		this.highResRect = dest;
		this.loadHighresImg = null;
		invalidate();
	
		
	}
	public void PostExecute(JSONObject result) {
		if(dontLoadNewLocations){
			return;
		}
		if(result.has("type")){
			String type;
			try {
				type = result.getString("type");
			
				if(type.compareTo("layer_list")==0){
						
					int num = result.getInt("layer_num");
					if(num>0){
						LinearLayout ll = (LinearLayout) this.context.findViewById(R.id.hud_layout);
						ll.removeAllViews();
						String[] layers = new String[num];
						JSONArray json_layers = result.getJSONArray("layers");
						for(int x = 0; x<num;x++){
							layers[x]=json_layers.getJSONObject(x).getString("layer");
						}
						
						Spinner layer_spinner = new Spinner(context);
						ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.context,
								R.layout.list_item,layers);
						layer_spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
							public void onItemSelected(AdapterView<?> arg0, View arg1,
									int arg2, long arg3) {
								TextView tv = (TextView) arg0.findViewById(R.id.primary_text_item);
								String layer = (String) tv.getText();
								
								FacilityPost fp = new FacilityPost(FacilityViewer.this);
								fp.execute(FacilityPost.lookupLocationByLayer(filename, layer));
							}
							public void onNothingSelected(AdapterView<?> arg0){
								
							}
						});
						layer_spinner.setAdapter(adapter);
						ll.addView(layer_spinner);
					}
				} else if(type.compareTo("location_list")==0){
					//returning from a lookup_location call
					
					int places_num = result.getInt("places_num");
					String building = result.getString("building");
					if(places_num>0){
						//some locations have been found 
						ArrayList<Location> locations = new ArrayList<Location>();					
						JSONArray list = result.getJSONArray("places");
						for(int x=0;x<places_num;x++){
							//create an array list of FacilityViewer.locations
							JSONObject place = list.getJSONObject(x);
							JSONObject pos = place.getJSONObject("position");
							float pos_x = new Float(pos.getString("x"));
							float pos_y = new Float(pos.getString("y"));
							String name = place.getString("name");
							String filename = place.getString("image");
							String layer = place.getString("layer");
							int rank = place.getInt("rank");
							Location loc = new Location(pos_x, pos_y, rank, name, building, filename, layer);
							locations.add(loc);
						}
						this.locations = locations;
						//display locations
						this.invalidate();
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}	
		
	}
	
	



}
