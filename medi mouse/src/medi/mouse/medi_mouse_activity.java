package medi.mouse;

import org.acra.ErrorReporter;
import org.apache.http.client.HttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public abstract class medi_mouse_activity extends Activity {
	protected Activity context;
	protected HttpClient client;
	protected medi_person me;
	
	public abstract void onPostExecute(medi_person result);
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.action_bar, menu);
        return true;
        
        //return super.onCreateOptionsMenu(menu);
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
        //  preparation code here
        return super.onPrepareOptionsMenu(menu);
    }


    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.d("ActionBar",item.getItemId()+":");
        switch (item.getItemId()) {
        	case android.R.id.home:
        		Intent intent = new Intent(this, MedibugsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        	case R.id.signinout:
        		startActivity(new Intent(this,EditStatus.class));
        		return true;
        		
            case R.id.settings:
                startActivity(new Intent(this, EditPreferences.class));
                return true;
            case R.id.helpabout:
                LayoutInflater inflater = LayoutInflater.from(this);

                // error here
                View alertDialogView = inflater.inflate(R.layout.alert_dialog_layout, null);

                WebView myWebView = (WebView) alertDialogView.findViewById(R.id.DialogWebView);
                
                myWebView.loadUrl("file:///android_asset/help_about.html");  
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                Button report = (Button) alertDialogView.findViewById(R.id.crash_report);
                builder.setView(alertDialogView);
                report.setOnClickListener(new OnClickListener(){

					public void onClick(View arg0) {
						ErrorReporter.getInstance().handleException(
								new Exception("haha, I found an error"));
						
					}});
               
               builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
               	//alertDialogView.requestLayout();
            	return true;
        }
        return false;
    }

	public void onSharedPreferenceChanged(SharedPreferences spref, String arg1) {  
		
		boolean is_lss = spref.getBoolean("is_lss", false);
    	
		me.is_lss = is_lss;
	}


}
