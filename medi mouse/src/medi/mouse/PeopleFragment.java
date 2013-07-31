package medi.mouse;

import java.util.ArrayList;
import java.util.HashMap;

import medi.mouse.InOutFragment.ActivityCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PeopleFragment extends Fragment {
	private static final String TAG = "PeopleFragment";
	private ActivityCallback mCallback;
	public View onCreateView(
			final LayoutInflater inflater, 
			ViewGroup container, 
	        Bundle savedInstanceState) {
		View view;
		
		Bundle args = getArguments();
		if(args == null){
			//load lookup
			view = loadPeopleLookup(inflater);
			return view;
		}
		String person = args.getString("person");
		Log.d(TAG,person);
		if(person == null){
			//load lookup
			 view = loadPeopleLookup(inflater);
			 
			 return view;
		} else {
			JSONObject stuff;
			try {
				stuff = new JSONObject(person);
			
				String type = stuff.optString("type","");
				Log.d(TAG,"type: "+type);
				if(type.equals("results")){
					JSONArray results = stuff.getJSONArray("results");
					view = loadPeopleLookup(inflater,results);
					return view;
				}else if(type.equals("info")){
					Log.d(TAG,"load person");
					JSONObject info = stuff.getJSONObject("info");
					view = loadPerson(inflater,info);
					return view;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private View loadPerson(LayoutInflater inflater, JSONObject info) throws JSONException {
		View view = inflater.inflate(R.layout.person, null);

		((TextView) view.findViewById(R.id.name)
				).setText(info.getString("name"));
		((TextView) view.findViewById(R.id.extension)
				).setText(info.getString("extension"));
		((TextView) view.findViewById(R.id.status)
				).setText(info.getString("status"));
		((TextView) view.findViewById(R.id.title1)
				).setText(info.getString("title1"));
		((TextView) view.findViewById(R.id.title2)
				).setText(info.getString("title2"));
		((TextView) view.findViewById(R.id.email)
				).setText(info.getString("email"));
		
		final String ext = info.getString("extension");
		Button call_desk = (Button) view.findViewById(R.id.call_desk);
		call_desk.setText("call ext: "+ext);
		
		call_desk.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				try {
			        Intent callIntent = new Intent(Intent.ACTION_CALL);
			        callIntent.setData(Uri.parse("tel:781774"+ext));
			        startActivity(callIntent);
			    } catch (ActivityNotFoundException e) {
			        Log.e("PeopleFragment dialing example", "Call failed", e);
			    }
			}
			
		});
		return view;
	}

	private View loadPeopleLookup(LayoutInflater inflater) {
		final View view = inflater.inflate(R.layout.people_lookup, null);
		Button lookup_button = (Button) view.findViewById(R.id.lookup_button);
		lookup_button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				EditText lookup_view = (EditText) view.findViewById(R.id.lookup);
				String key = lookup_view.getText().toString();
				if(key.length()>0){
					mCallback.loadPerson(key);
				}
			}
			
		});
		return view;
	}
	private View loadPeopleLookup(LayoutInflater inflater,JSONArray results) throws JSONException {
		View view = loadPeopleLookup(inflater);

		for(int x = 0; x<results.length(); x++){
			JSONObject result = results.getJSONObject(x);
			String name = result.getString("name");
			String extension = result.getString("extension");
			View person_list_item = inflater.inflate(R.layout.person_list_item, null);
			((TextView) person_list_item.findViewById(R.id.person_name)
					).setText(name);
			((TextView) person_list_item.findViewById(R.id.extension)
					).setText(extension);
			person_list_item.setOnClickListener(new person_listener(name));
			
			((LinearLayout) view.findViewById(R.id.person_list)
					).addView(person_list_item);
			
		}
		return view;
	}
	class person_listener implements OnClickListener{
		private String name;
		person_listener(String name){
			this.name=name;
		}
		@Override
		public void onClick(View arg0) {
			mCallback.loadPerson(name);
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
                    + " must implement PeopleFragment.ActivityCallback");
        }
        
    }

	public interface ActivityCallback {
		abstract void loadPerson(String name);
	}
}
