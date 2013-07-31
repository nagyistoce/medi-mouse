package medi.mouse;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import medi.mouse.ScheduleFragment.ActivityCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BigEventFragment extends Fragment {
	private static final String TAG = "BigEventFragment";
	private ActivityCallback mCallback;
	public View onCreateView(
			final LayoutInflater inflater, 
			ViewGroup container, 
	        Bundle savedInstanceState) {
		View event_big = inflater.inflate(R.layout.event_big, null);
		
		Bundle info = getArguments();
		String ev = info.getString("event");
		try {
			JSONObject event = new JSONObject(ev);
			//-------------------------------------------------
			//build general info
			//event name
			String name = event.getString("name");
			((TextView)event_big.findViewById(R.id.name)
					).setText(name);
			//event notes
			String notes = event.getString("notes");
			((TextView)event_big.findViewById(R.id.notes_view)
					).setText(notes);
			
			
			
			
			//-------------------------------------------------
			//build event list
			JSONArray event_dates;
			event_dates = event.getJSONArray("events_dates");
			for(int x = 0; x<event_dates.length(); x++){
				JSONObject event_date = event_dates.getJSONObject(x);
				String date = event_date.getString("date");
				SimpleDateFormat inFormat = new SimpleDateFormat("yyyyMMdd");
				SimpleDateFormat outFormat = new SimpleDateFormat("MM/dd");
				date = outFormat.format(inFormat.parse(date));
				String time_start = event_date.getJSONObject("time")
						.getString("start");
				String time_end = event_date.getJSONObject("time")
						.getString("end");
				String location = event_date.getString("where");
				
				View event_time = inflater.inflate(R.layout.event_time, null);
				((TextView)event_time.findViewById(R.id.location)
						).setText(location);
				((TextView)event_time.findViewById(R.id.date)
						).setText(date);
				((TextView)event_time.findViewById(R.id.time)
						).setText(time_start+" - "+time_end);
				
				((LinearLayout)event_big.findViewById(R.id.event_list)
						).addView(event_time);
				
			}
			//-------------------------------------------------
			//build participant list
			JSONArray json_participants = event.getJSONArray("participants");
			
			for(int x = 0; x<json_participants.length(); x++){
				String participant=json_participants.getString(x);
				View event_participant = inflater.inflate(R.layout.event_participant, null);
				event_participant.setOnClickListener(
						new personClickListener(participant));
				
				((TextView)event_participant.findViewById(R.id.person)
						).setText(participant);
				
				((LinearLayout)event_big.findViewById(R.id.participants_list)
						).addView(event_participant);
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return event_big;
	}
	class personClickListener implements OnClickListener {
		
		private Bundle args;
		private String person;
		public personClickListener(String person){
			this.person = person;
			args = new Bundle();
			args.putString("person", person);
		}
		@Override
		public void onClick(View arg0) {
			Log.d(TAG,"clicked person: "+person);
			mCallback.loadPerson(person);
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
                    + " must implement BigEventFragment.ActivityCallback");
        }
        
    }

	public interface ActivityCallback {
		abstract void loadPerson(String name);
	}
}
