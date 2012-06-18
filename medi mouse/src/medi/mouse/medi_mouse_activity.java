package medi.mouse;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;

public abstract class medi_mouse_activity extends Activity {
	protected Activity context;
	protected DefaultHttpClient client;
	protected medi_person me;
	
	public abstract void onPostExecute(medi_person result);
}
