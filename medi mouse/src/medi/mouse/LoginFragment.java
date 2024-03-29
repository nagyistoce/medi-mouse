package medi.mouse;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginFragment extends Fragment {
	private ActivityCallback mCallback;
	private String username;
	private String password;
	private EditText username_view;
	private EditText password_view;
	public View onCreateView(
			final LayoutInflater inflater, 
			ViewGroup container, 
	        Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login, null);
		
		username_view = (EditText)view.findViewById(R.id.username);
		username_view.setText(username);
		username_view.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				String val = username_view.getText().toString();
				username = val;
				mCallback.setUsername(val);
				return false;
			}
			
		});
		password_view = (EditText)view.findViewById(R.id.password);
		password_view.setText(password);
		password_view.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				String val = password_view.getText().toString();
				password = val;
				mCallback.setPassword(val);
				return false;
			}
			
		});
		
		Button login = (Button)view.findViewById(R.id.login);
		login.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getActionMasked()==MotionEvent.ACTION_UP){
					username = username_view.getText().toString();
					mCallback.setUsername(username);
					password = password_view.getText().toString();
					mCallback.setPassword(password);
					mCallback.login();
				}
				return true;
			}
			
		});
		return view;
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
                    + " must implement LoginFragment.ActivityCallback");
        }
        username = mCallback.getUsername();
        password = mCallback.getPassword();
        
    }
	public interface ActivityCallback {
		//getters
		abstract String getUsername();
		abstract String getPassword();
		//setters
		abstract void setUsername(String username);
		abstract void setPassword(String password);
		//login
		abstract void login();
	}

}
