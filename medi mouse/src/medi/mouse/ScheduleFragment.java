package medi.mouse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import medi.mouse.LoginFragment.ActivityCallback;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScheduleFragment extends Fragment {
	private static final String TAG = "ScheduleFragment";
	private ActivityCallback mCallback;
	private String events;
	private SharedPreferences spref;
	private LayoutInflater inflater;
	private View mView;
	public View onCreateView(
			final LayoutInflater inflater, 
			ViewGroup container, 
	        Bundle savedInstanceState) {
		this.inflater = inflater;
		mView = inflater.inflate(R.layout.schedule, null);
		if(!events.equals("")){
			JSONObject JSONEvents;
			try{
				JSONEvents = new JSONObject(events);
				loadSchedule(JSONEvents);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Button refresh = (Button) mView.findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				mCallback.getSchedule(ScheduleFragment.this);
			}
			
		});
		return mView;
	}

	public void loadSchedule(JSONObject events) {
		try {
			((LinearLayout) mView.findViewById(R.id.event_list)
					).removeAllViews();
			Iterator<String> keys = events.keys();
			while(keys.hasNext()){
				String key = keys.next();
				JSONObject event = events.getJSONObject(key);
				Log.d(TAG+":loadSchedule",key+": "+event.toString());
				JSONArray event_dates = event.getJSONArray("events_dates");
				JSONObject next_event_date=null;
				JSONObject last_event_date=null;
				for(int x = 0; x<event_dates.length(); x++){
					JSONObject event_date = event_dates.getJSONObject(x);
					String type = event_date.getString("type");
					if(type.equals("future_event")){
						next_event_date = event_date;
						break;
					}
					last_event_date=event_date;
				}
				if(next_event_date==null&&last_event_date==null){
					break;
				}else if(next_event_date==null){
					next_event_date=last_event_date;
				}
				
				String name = event.getString("name");
				String notes = event.getString("notes");
				String date = next_event_date.getString("date");
				
				SimpleDateFormat inFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat outFormat = new SimpleDateFormat("MM/dd");
				date = outFormat.format(inFormat.parse(date));
				String time_start = next_event_date.getJSONObject("time")
						.getString("start");
				String time_end = next_event_date.getJSONObject("time")
						.getString("end");
				String location = next_event_date.getString("where");
				JSONArray json_participants = event.getJSONArray("participants");
				String[] participants = new String[json_participants.length()];
				for(int x = 0; x<json_participants.length(); x++){
					participants[x]=json_participants.getString(x);
				}
				View event_view = inflater.inflate(R.layout.event_item, null);
				((TextView) event_view.findViewById(R.id.name)).setText(name);
				((TextView) event_view.findViewById(R.id.location)).setText(location);
				((TextView) event_view.findViewById(R.id.date)).setText(date);
				((TextView) event_view.findViewById(R.id.time)
						).setText(time_start+" - "+time_end);
				event_view.setOnClickListener(new miniEventClickListener(event.toString()));
				
				((LinearLayout) mView.findViewById(R.id.event_list)
						).addView(event_view);
				
		
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	class miniEventClickListener implements OnClickListener {
		private Bundle args;
		public miniEventClickListener(String event){
			args = new Bundle();
			args.putString("event", event.toString());
		}
		@Override
		public void onClick(View arg0) {
			FragmentManager fragmentManager = mCallback.getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			BigEventFragment bigEventFragment = new BigEventFragment();
			bigEventFragment.setArguments( args );
			fragmentTransaction.replace(R.id.main_content, bigEventFragment);
			fragmentTransaction.addToBackStack( "main_fragment" ).commit();
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
                    + " must implement ScheduleFragment.ActivityCallback");
        }
        spref = mCallback.getSharedPreferences();
        events = spref.getString("events","");
		if(events.equals("")){
			mCallback.getSchedule(this);
		}
    }

	public interface ActivityCallback {
		//getters
		abstract String getUsername();
		abstract String getPassword();
		abstract void getSchedule(ScheduleFragment fragment);
		abstract SharedPreferences getSharedPreferences();
		abstract FragmentManager getSupportFragmentManager();
	}
}
