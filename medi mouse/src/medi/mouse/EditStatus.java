package medi.mouse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EditStatus extends medi_mouse_activity {
	static int TRANS_START=800;
	static int TRANS_DUR=300;
	
	static final int GET_MENU = 100;
	static final int GET_INFO = 101;
	static final int SET_STATUS = 102;
	
	private JSONObject menu_top;
	protected boolean one_shot=false;
	boolean menu_built = false;
	private int action;
	public void onCreate(Bundle savedInstanceState) {
			
	        super.onCreate(savedInstanceState);
	        //setContentView(R.layout.main_view);   
	        
	        
	        menu = spref.getString("trax_menu","");
			
	        if(menu.length()==0){
	        	get_menu();
	    	} else {
	    		build_menu(menu);
	    	}        
	}
	private void get_menu(){
		spinMouse();
		action = GET_MENU;
		coretrax_post ctpost = new coretrax_post(this);
		ctpost.execute(new coretrax_args("get_menu","",""));
	}

	protected void submit(coretrax_args args) {
		
		spinMouse();
		action = SET_STATUS;
		coretrax_post ctpost = new coretrax_post(this);
		ctpost.execute(args);
		
	}

	@Override
	public void onPostExecute(Object result) {
		stopMouse();
		
		String res = result.toString();
		switch(action){
		case GET_MENU:
			SharedPreferences spref=
			PreferenceManager.getDefaultSharedPreferences(this);

			SharedPreferences.Editor editor = spref.edit();
			
			editor.putString("trax_menu", res);
			editor.commit();
			build_menu(res);
			break;
		case GET_INFO:
		case SET_STATUS:
			save_status(res);
			finish();
			break;
		}

		
		
	}
	public void build_menu(String menu_data){
		JSONObject data;
		try {
			data = new JSONObject(menu_data);
			if(data.has("error")){
				if(data.has("code")&&data.getString("code")=="401"){
					
				}else{
					get_menu();
					return;
				}
			} else{
				menu_top = data;
			}
			LayoutInflater inflater = LayoutInflater.from(this);
            View breadcrumbs = inflater.inflate(R.layout.breadcrumbs, null);
            View main = inflater.inflate(R.layout.main, null);

			LinearLayout ll = (LinearLayout) findViewById(R.id.MainLinearLayout);
			ll.removeAllViews();
			ll.addView(breadcrumbs);
			ll.addView(main);
			
			menu_node root = new menu_node(menu_top,
					new ArrayList<String>(),
					(LinearLayout) main.findViewById(R.id.MainLinearLayout),
					(LinearLayout) breadcrumbs.findViewById(R.id.MainLinearLayout));
			root.clicked();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	private void save_status(String res) {
		SharedPreferences spref=
				PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = spref.edit();
		editor.putString("full_name", full_name);
		editor.putString("status", status);
		editor.commit();
		
	}
	class menu_node implements OnClickListener{
		private static final String TAG = "menu_node";
		JSONObject submenu;
		ArrayList<String> path;
		private LinearLayout layout;
		private String key;
		private HashMap<String,String> extra;
		private LinearLayout breadcrumbs;
		private boolean submitable = false;
		private View extraView = createBoxView();
		menu_node(JSONObject submenu,
				ArrayList<String> path, 
				LinearLayout layout,
				LinearLayout breadcrumbs){
			this.key = "";
			this.submenu = submenu;
			this.path = path;
			this.layout = layout;
			this.breadcrumbs = breadcrumbs;
			
			if(path.size()==0){
				this.breadcrumbs.removeAllViews();
				View v = createBoxView(createTextView("top"));
				addBreadCrumb(v);
			}
			
		}
		menu_node(String key, JSONObject submenu,ArrayList<String> path, LinearLayout layout,LinearLayout breadcrumbs){
			this.key = key;
			this.submenu = submenu;
			this.path = path;
			this.layout = layout;
			this.breadcrumbs = breadcrumbs;
		}
		private void addExtraBreadCrumb(String info){
			
			TableLayout TableView = (TableLayout) extraView.findViewById(R.id.TableLayout);
	    	TableView.removeAllViews();
	    	TableView.addView(createTextView(info));
	    	addBreadCrumb(extraView);
			
		}
		
		void clicked(){
			Iterator<?> keyIter = submenu.keys();
			layout.removeAllViews();
			
			ArrayList<String> keys = new ArrayList<String>();
			while(keyIter.hasNext()){
				String key = (String) keyIter.next();
				keys.add(key);
			}
			Collections.sort(keys);
			for(String key : keys){
			
				
				if(key.equals("phone_ext_name")){
					submitable = true;
					//free text box
					EditText et = createEditText();
					et.addTextChangedListener(new TextWatcher(){

						@Override
						public void afterTextChanged(Editable arg0) {
							menu_node.this.extra = new HashMap<String,String>();
							menu_node.this.extra.put("phone_ext_name",arg0.toString());
							Log.d(TAG,"Text Changed: "+arg0.toString());
							addExtraBreadCrumb(arg0.toString());
						}

						@Override
						public void beforeTextChanged(CharSequence arg0,
								int arg1, int arg2, int arg3) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onTextChanged(CharSequence arg0, int arg1,
								int arg2, int arg3) {
							// TODO Auto-generated method stub
							
						}
						
					});
					View box = createBoxView(et);
					layout.addView(box);
				}else if(key.equals("date_select_name")){
					submitable = true;
					//date selector
					DatePicker dp = createDatePicker();
					GregorianCalendar cal = new GregorianCalendar();
					int year = cal.get(Calendar.YEAR);
					int month = cal.get(Calendar.MONTH);
					int day = cal.get(Calendar.DAY_OF_MONTH);
					int m = month+1;
					String mm;
					String dd;
					if(m<10){
						mm = "0"+m;
					}else{
						mm = ""+m;
					}
					if(day<10){
						dd = "0"+day;
					}else{
						dd = ""+day;
					}
					addExtraBreadCrumb(mm+"/"+dd+"/"+year);
					dp.init(year,month,day,new DatePicker.OnDateChangedListener(){
						
						@Override
						public void onDateChanged(DatePicker arg0, int year,
								int month, int day) {
							month++;
							String mm;
							String dd;
							if(month<10){
								mm = "0"+month;
							}else{
								mm = ""+month;
							}
							if(day<10){
								dd = "0"+day;
							}else{
								dd = ""+day;
							}
							String yyyymmdd = year + mm + dd;
							addExtraBreadCrumb(mm+"/"+dd+"/"+year);
							Log.d(TAG,"picked day: "+yyyymmdd);
							menu_node.this.extra = new HashMap<String,String>();
							menu_node.this.extra.put("date_select_name",yyyymmdd);
						}
						
					});
					View box = createBoxView(dp);
					layout.addView(box);
				}else if(key.equals("Save")){
					submitable = true;
				}else if(key.equals("Note")){
					//don't show Note label here
				}else{
					
					TextView label = createTextView(key);
					View box = createBoxView(label);
					ArrayList<String> npath = new ArrayList<String>(path);
					if(!key.equals("extra")){
						npath.add(key);
					}
					try {
						box.setOnClickListener(new menu_node(submenu.getJSONObject(key),npath,layout,breadcrumbs));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					layout.addView(box);
				}
				
				
				
				
			}
			if(submitable){
				TextView label = createTextView("Submit");
				View box = createBoxView(label);
				layout.addView(box);
				box.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						coretrax_args args = new coretrax_args("set_status",
								EditStatus.this.username,
								EditStatus.this.password,
								path,
								extra);
						EditStatus.this.submit(args);
						
					}
					
				});
			}
		}
		
		private void addBreadCrumb(View v){
			v.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					int num_children = breadcrumbs.getChildCount();
					ArrayList<String> p = new ArrayList<String>();
					JSONObject sub = menu_top;
					int remove_after = 0;
					String selected = ((TextView)
							((TableLayout) arg0.findViewById(R.id.TableLayout)).getChildAt(0))
							.getText().toString();
					//build path from breadcrumbs
					for(int x = 0; x<num_children; x++){
						View child = breadcrumbs.getChildAt(x);
						Log.d(TAG,"x: "+x);
						String opt = ((TextView)
								((TableLayout) child.findViewById(R.id.TableLayout)).getChildAt(0))
								.getText().toString();
						Log.d(TAG,"found: "+opt);
						
						
						
						if(opt.equals("top")){
							//don't put top in path
						} else {
							Log.d(TAG,"adding to path: "+opt);
							p.add(opt);
							try {
								sub = sub.getJSONObject(opt);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if(selected.equals(opt)){
							remove_after = x;
							break;
						}
					}
					remove_after++;
					breadcrumbs.removeViews(remove_after,num_children-remove_after);
					//load new menu
					menu_node m = new menu_node(sub, p, layout, breadcrumbs);
					m.clicked();
					
				}
				
			});
			TableLayout.LayoutParams params = new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT,     
	                LayoutParams.WRAP_CONTENT
	        );
			params.setMargins(0,0,15,0);
			v.setLayoutParams(params);
			layout.removeView(v);
			breadcrumbs.removeView(v);
			breadcrumbs.addView(v);
			//((HorizontalScrollView) breadcrumbs.getParent()).fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			HorizontalScrollView hsv = ((HorizontalScrollView) breadcrumbs.getParent());
			
			if(SDK < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				//???
			}else{
				hsv.setScrollX(hsv.getWidth());
			}
		}
		@Override
		public void onClick(View arg0) {
			Log.d(TAG,path.toString());
			addBreadCrumb(arg0);
			this.clicked();
		}
	}
	

		
}
