package medi.mouse;

import java.util.ArrayList;
import java.util.HashMap;

import medi.mouse.InOutFragment.ActivityCallback;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NotesFragment extends Fragment {
	private static final String TAG = "NotesFragment";
	private LayoutInflater inflater;
	private View view;
	private static final String default_prefix = "<font size=\"2\"><span style=\"font-family:Verdana; color:rgb(0,0,0);\">";
	private static final String default_suffix = "<br></span></font>";
	private ArrayList<String> line_prefixes;
	private ArrayList<String> lines;
	private ActivityCallback mCallback;
	public View onCreateView(
			final LayoutInflater inflater, 
			ViewGroup container, 
	        Bundle savedInstanceState) {
		this.inflater = inflater;
		view = inflater.inflate(R.layout.notes, null);
		mCallback.getNotes(this);
		return view;
	}
	
	private void breakdown_notes(String notes){
		line_prefixes = new ArrayList<String>();
		lines = new ArrayList<String>();
		
		String font = "<font ";
		String span = "<span ";
		String span_end = "\">";
		String line_end = "<br>";
		
		int x = 0;
		while((x=notes.indexOf(font,x))!=-1){
			int y = notes.indexOf(span,x);
			if(y==-1){
				break;
			}
			int start_line = notes.indexOf(span_end,y);
			if(start_line==-1){
				break;
			}
			start_line+=span_end.length();
			String prefix = notes.substring(x,start_line);
			
			int end_line = notes.indexOf(line_end,start_line);
			String line = notes.substring(start_line,end_line);
			line_prefixes.add(prefix);
			lines.add(line);
			Log.d(TAG,line);
		}
		
	}
	private void displayNotes(String notes){
		breakdown_notes(notes);
		
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
                    + " must implement NotesFragment.ActivityCallback");
        }
        
    }

	public interface ActivityCallback {
		//getters
		abstract void getNotes(NotesFragment fragment);
	}
}
