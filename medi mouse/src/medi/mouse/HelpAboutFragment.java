package medi.mouse;

import medi.mouse.ScheduleFragment.ActivityCallback;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class HelpAboutFragment extends Fragment {
	private LayoutInflater inflater;
	private View mView;
	private ActivityCallback mCallback;

	public View onCreateView(
			final LayoutInflater inflater, 
			ViewGroup container, 
	        Bundle savedInstanceState) {
		this.inflater = inflater;
		mView = inflater.inflate(R.layout.help_about, null);
		WebView help_about_view = (WebView) mView.findViewById(R.id.webview);
		help_about_view.loadUrl("file:///android_asset/help_about.html");
		help_about_view.addJavascriptInterface(new WebAppInterface(container.getContext()), "Android");
		WebSettings webSettings = help_about_view.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		return mView;
	}
	
	public class WebAppInterface {
	    Context mContext;

	    /** Instantiate the interface and set the context */
	    WebAppInterface(Context c) {
	        mContext = c;
	    }

	    /** Show a toast from the web page */
	    @JavascriptInterface
	    public void showToast(String toast) {
	        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	    }
	    @JavascriptInterface
	    public String getVersion() {
	        return mCallback.getVersion();
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

    }

	public interface ActivityCallback {
		//getters
		abstract String getVersion();
	}
}
