package medi.mouse;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.TextView;


public class InOutFragment extends Fragment {
	private static final String TAG = "InOutFragment";
	private ActivityCallback mCallback;
	private Breadcrumbs breadcrumbs;
	private JSONObject menu;
	private ArrayList<String> path;
	private LayoutInflater inflater;
	private View view;
	private float width;
	private float height;
	public View onCreateView(
			final LayoutInflater inflater, 
			ViewGroup container, 
	        Bundle savedInstanceState) {
		this.inflater = inflater;
		view = inflater.inflate(R.layout.in_out, null);
		
		width = view.getX();
		height = view.getY();
		Bundle info = getArguments();
		String menu_string = info.getString("menu");
		getStatus();
		try {
			Log.d(TAG,"menu: "+menu_string);
			menu = new JSONObject(menu_string);
			build(menu,true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return view;
	}
	private void getStatus(){
		mCallback.getStatus(this);
	}
	public void DisplayStatus(JSONObject info){
		TextView name = (TextView) view.findViewById(R.id.name);
		TextView status = (TextView) view.findViewById(R.id.status);
		
		name.setText(info.optString("full_name"));
		status.setText(info.optString("status"));
	}
	public void build(JSONObject menu){
		build(menu,false);
		
	}
	public void build(JSONObject menu, boolean is_root){
		breadcrumbs = new Breadcrumbs();
		breadcrumbs.add("Top",menu);
		buildMenu(menu,is_root);
		view.invalidate();
	}

	private void buildMenu(JSONObject menu, boolean is_root){
		MenuLayer ml = new MenuLayer(menu,
				breadcrumbs.getPath(),
				breadcrumbs.getExtra(),
				is_root);
		ml.updateView(view);
		Log.d(TAG,"ml: "+ml.toString());
	}
	class Breadcrumbs {
		class Breadcrumb {
			View view;
			JSONObject menu;
			String label;
			Breadcrumb(String label,JSONObject menu){
				this.label = label;
				this.menu = menu;
				view = inflater.inflate(R.layout.in_out_breadcrumb_item,null);
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						//update breadcrumb view
						removeAndUpdate(
								InOutFragment.this.view,
								Breadcrumb.this.view);
						//update menu
						Log.d(TAG+":Breadcrumb",
								"names: "+Breadcrumb.this.menu.names().toString());
						if(Breadcrumb.this.label.equals("Top")){
							(new MenuLayer(
									Breadcrumb.this.menu,
									getPath(),
									getExtra(),
									true)).updateView(
									InOutFragment.this.view);
						}else{
							(new MenuLayer(
									Breadcrumb.this.menu,
									getPath(),
									getExtra())).updateView(
									InOutFragment.this.view);
						}
					}
				});
				((TextView) view.findViewById(R.id.breadcrumb_item)
						).setText(label);
			}
		}
		ArrayList<Breadcrumb> breadcrumbs;
		View view;
		HashMap<String, String> extra;
		Breadcrumbs(){
			breadcrumbs = new ArrayList<Breadcrumb>();
			
		}
		public ArrayList<String> getPath(){
			ArrayList<String> path = new ArrayList<String>();
			for(Breadcrumb bc: breadcrumbs){
				if(bc.label.equals("Top")){
					continue;
				}
				path.add(bc.label);
			}
			return path;
		}
		public void setExtra(HashMap<String,String> extra){
			this.extra = extra;
		}
		public HashMap<String,String> getExtra(){
			return extra;
		}
		public void add(String label,JSONObject menu){
			Breadcrumb bc = new Breadcrumb(label,menu);
			breadcrumbs.add(bc);
			View mainView = InOutFragment.this.view;
			((LinearLayout) mainView.findViewById(R.id.breadcrumbs)
					).removeAllViews();
			for(Breadcrumb bci: breadcrumbs){
				((LinearLayout) mainView.findViewById(R.id.breadcrumbs)
						).addView(bci.view);
			}
			float end_x = bc.view.getX();
			float end_y = bc.view.getY();
			TranslateAnimation anim = new TranslateAnimation( 
					end_x + width, end_x, 
					end_y, end_y );
			anim.setDuration(400);
		    anim.setFillAfter( true );
		    bc.view.startAnimation(anim);
		    
		    mainView.invalidate();
			
		}
		public View getView(){
			view = inflater.inflate(R.layout.in_out_breadcrumbs,null);
			for(Breadcrumb bc: breadcrumbs){
				((LinearLayout) view.findViewById(R.id.breadcrumbs)
						).addView(bc.view);
			}
			return view;
		}
		public void updateView(View view){
			((LinearLayout) view.findViewById(R.id.breadcrumbs)
					).removeAllViews();
			for(Breadcrumb bc: breadcrumbs){
				((LinearLayout) view.findViewById(R.id.breadcrumbs)
						).addView(bc.view);
			}
			view.invalidate();
		}
		
		public void removeAndUpdate(final View view,View last_label){
			((LinearLayout) view.findViewById(R.id.breadcrumbs)
					).removeAllViews();
			boolean remove = false;
			ArrayList<Breadcrumb> remove_these = new ArrayList<Breadcrumb>(); 
			for(Breadcrumb bc: breadcrumbs){
				((LinearLayout) view.findViewById(R.id.breadcrumbs)
						).addView(bc.view);
				
				if(remove){
					remove_these.add(bc);
				}else if(bc.view.equals(last_label)){
					remove = true;
				}
			}
			for(final Breadcrumb bc : remove_these){
				breadcrumbs.remove(bc);
				
				float from_x = bc.view.getX();
				float from_y = bc.view.getY();
				TranslateAnimation anim = new TranslateAnimation( 
						from_x, from_x + width, 
						from_y, from_y );
				anim.setAnimationListener(new AnimationListener(){

					@Override
					public void onAnimationEnd(Animation arg0) {
						//remove view
						((LinearLayout) view.findViewById(R.id.breadcrumbs)
								).removeView(bc.view);

					}

					@Override
					public void onAnimationRepeat(Animation arg0) {}

					@Override
					public void onAnimationStart(Animation arg0) {}
					
				});
			    anim.setDuration(600);
			    anim.setFillAfter( true );
			    bc.view.startAnimation(anim);
			}

			
		}
	}

	class MenuLayer {
		class MenuItem {
			View view;
			MenuItem(final String label,final JSONObject menu){
				view = inflater.inflate(R.layout.in_out_menu_item, null);
				((TextView) view.findViewById(R.id.menu_item)
						).setText(label);
				view.setOnClickListener(new OnClickListener(){			
					@Override
					public void onClick(View arg0) {
						// add breadcrumb
						breadcrumbs.add(label,menu);
						// build menulayer
						buildMenu(menu,false);
					}
					
				});
			}
			MenuItem(){
				//do nothing
			}
			
		}
		class ExtraButton extends MenuItem{
			String type;
			String info;
			ExtraButton(String type){
				super();
				this.type = type;
				if(type.equals("phone_ext_name")){
					//phone_ext input
					view = inflater.inflate(R.layout.in_out_menu_item_phone, null);
					TextView phone_ext_view = (TextView) view.findViewById(R.id.phone_ext);
					phone_ext_view.setOnKeyListener(new OnKeyListener(){

						@Override
						public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
							info = ((TextView)arg0).getText().toString();
							updateExtra();
							return false;
						}
						
					});
				}else if(type.equals("date_select_name")){
					//date_select input
					view = inflater.inflate(R.layout.in_out_menu_item_date, null);
					DatePicker dp = (DatePicker)view.findViewById(R.id.date_picker);
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime(new Date());
					dp.init(cal.get(Calendar.YEAR), 
							cal.get(Calendar.MONTH), 
							cal.get(Calendar.DAY_OF_MONTH),
							new OnDateChangedListener(){

								@Override
								public void onDateChanged(DatePicker arg0,
										int year, int month, int day) {
									String YYYY = ""+year;
									String MM = month++<10?"0"+month:""+month;
									String DD = day<10?"0"+day:""+day;
									String date = YYYY+MM+DD;
									Log.d(TAG,"date: "+date);
									info=date;
									updateExtra();
								}
						
					});
				}
				
			}
			private void updateExtra(){
				extra = new HashMap<String,String>();
				extra.put(type,info);
				Log.d(TAG,"MenuLayer: "+MenuLayer.this.toString());
			}
			
			
		}
		class SaveButton extends MenuItem{
			
			SaveButton(){
				super();
				view = inflater.inflate(R.layout.in_out_menu_item, null);
				((TextView) view.findViewById(R.id.menu_item)
						).setText("Save");
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						mCallback.saveStatus(
								path,
								extra);
					}
					
				});
				view.setOnLongClickListener(new OnLongClickListener(){
					@Override
					public boolean onLongClick(View arg0) {
						Log.d(TAG,"onLongClick, quick save");
						mCallback.setQuickSave(
								path,
								extra,
								getLabel());
						build(InOutFragment.this.menu,true);
						return true;
					}
					
				});
				
			}
			private String getLabel(){
				String ret = "";
				for(String s : path){
					if(s.equals("In")||s.equals("Out")){
						continue;
					}
					ret += s+" ";
				}
				if(extra!= null && extra.size()>0){
					for(String key : extra.keySet()){
						ret +=  extra.get(key);
					}
				}
				return ret;
			}
			
			
		}
		class CustomDivider extends MenuItem{
			CustomDivider(){
				super();
				view = inflater.inflate(R.layout.in_out_menu_item_custom_divider, null);
			}
		}
		class CustomSaveButton extends MenuItem{
			CustomSaveButton(
					final String label,
					final ArrayList<String> path, 
					final HashMap<String,String> extra){
				super();
				view = inflater.inflate(R.layout.in_out_menu_item, null);
				((TextView) view.findViewById(R.id.menu_item)
						).setText(label);
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						mCallback.saveStatus(
								path,
								extra);
					}
					
				});		
				view.setOnLongClickListener(new OnLongClickListener(){
					@Override
					public boolean onLongClick(View arg0) {
						mCallback.delQuickSave(label);
						build(InOutFragment.this.menu,true);
						return true;
					}
					
				});	
			}
		}
		
		ArrayList<MenuItem> MenuItems;
		ArrayList<String> path;
		HashMap<String,String> extra;
		private boolean can_save;
		MenuLayer(
				JSONObject menu,
				ArrayList<String> path,
				HashMap<String,String> extra){
			can_save = false;
			MenuItems = new ArrayList<MenuItem>(); 
			this.path = path;
			this.extra = extra;
			Iterator<String> keys = menu.keys();
			
			boolean add_save = false;
			while(keys.hasNext()){
				String key = keys.next();
				//don't care about Notes here
				if(key.equals("Note")){
					continue;
				}
				Log.d(TAG,"key: "+key);
				try {
					if(key.equals("Save")){
						add_save = true;
					}else if(key.equals("extra")){
						JSONObject ex = menu.getJSONObject(key);
						JSONArray names = ex.names();
						String name = (String) names.get(0);
						MenuItems.add(new ExtraButton(name));
					}else {
						JSONObject sub_menu;
						sub_menu = menu.getJSONObject(key);
						MenuItems.add(new MenuItem(key,sub_menu));
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			if(add_save){
				//make sure save is on the bottom
				MenuItems.add(0,new SaveButton());
			}
			
		}
		MenuLayer(
				JSONObject menu,
				ArrayList<String> path,
				HashMap<String,String> extra,
				boolean is_root){
			this(menu,path,extra);
			if(is_root){
				JSONObject quick_saves = mCallback.getQuickSaves();
				Log.d(TAG,"quick_saves: "+quick_saves.toString());
				if(quick_saves.length()>0){
					MenuItems.add(new CustomDivider());
					for(int x=0; x<quick_saves.names().length(); x++){
						JSONObject quick_save;
						String name = quick_saves.names().optString(x);
						try {
							
							quick_save = quick_saves.getJSONObject(name);
							 
							JSONArray jcpath = quick_save.getJSONArray("path");
							ArrayList<String> cpath = new ArrayList<String>();
							for(int y=0; y<jcpath.length(); y++){
								cpath.add(jcpath.getString(y));
							}
							HashMap<String,String> cextra = new HashMap<String,String>(); 
							if(quick_save.has("extra")){
								JSONObject jcextra = quick_save.getJSONObject("extra");
								String extra_name = jcextra.names().getString(0);
								String extra_value = jcextra.getString(extra_name);
								cextra.put(extra_name,extra_value);
							}
							CustomSaveButton csb = new CustomSaveButton(name,cpath,cextra);
							MenuItems.add(csb);
						} catch (JSONException e) {
							e.printStackTrace();
							mCallback.delQuickSave(name);
						}
					}
				}
			}
		}

		public View createView(){
			View view = inflater.inflate(R.layout.in_out_menu, null);
			for(MenuItem mi : MenuItems){
				((LinearLayout) view.findViewById(R.id.menu)
						).addView(mi.view);
			}
			return view;
		}
		public void updateView(View view){
			LinearLayout ll = ((LinearLayout) view.findViewById(R.id.menu));
			ll.removeAllViews();
			for(MenuItem mi : MenuItems){
				((LinearLayout) view.findViewById(R.id.menu)
						).addView(mi.view);
			}
			view.invalidate();
		}
		public String toString(){
			String ret;
			ret = "[";
			if(path.size()>0){
				for(String p : path){
					ret += p +",";
				}
				
				ret = ret.substring(0,ret.length()-1);
			}
			ret += "]";
			
			if(extra!= null && extra.size()>0){
				ret += " ";
				for(String key : extra.keySet()){
					ret +=  extra.get(key);
				}
			}
			return ret;
			
		}
	}
	

	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ActivityCallback) activity;
            
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement InOutFragment.ActivityCallback");
        }
        
    }

	public interface ActivityCallback {
		abstract void saveStatus(
				ArrayList<String> path,
				HashMap<String,String> extra);
		
		//setter
		abstract void setQuickSave(
				ArrayList<String> path,
				HashMap<String,String> extra,
				String label);
		abstract void delQuickSave(String label);
		//getters
		abstract JSONObject getQuickSaves();
		abstract void getStatus(InOutFragment fragment);
	}
}
