package medi.mouse;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ViewPerson extends medi_mouse_activity {

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.view_user);
		//view user:
		// stafflink: xxx
		// TYPE: ViewUser
		// view: Photo
        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String username = spref.getString("user_name", "");
    	String password = spref.getString("user_password","");
    	String stafflink = spref.getString("lookup_stafflink","");
    	String myStafflink = spref.getString("stafflink", "");
    	boolean reload = spref.getBoolean("reload_onresume", false);
    	
    	client = medi_post.connect(username, password);
		
    	me = new medi_person(this,stafflink,myStafflink);
    	me.client = client;
    	me.data = new HashMap<String, String>();
    	me.data.put("stafflink", stafflink);
    	me.data.put("TYPE", "ViewUser");
    	me.data.put("view", "Photo");
		medi_post postme = new medi_post(me.data,me.is_lss);
		postme.execute(me);
        
		
	}
	@Override
	public void onPostExecute(medi_person result) {
		// TODO Auto-generated method stub
		TextView name_view = (TextView) findViewById(R.id.name_view);
        TextView status_view = (TextView) findViewById(R.id.status_view);
        TextView date_view = (TextView) findViewById(R.id.date_view);
        ImageView picture = (ImageView) findViewById(R.id.picture_view);
        
    	name_view.setText(me.full_name);
    	status_view.setText(me.status);
    	date_view.setText(me.date);
    	
    	status_view.refreshDrawableState();


		//ListView lv = (ListView) findViewById(R.id.select_view);
		LinearLayout ll = (LinearLayout) findViewById(R.id.options_menu_view);
		ll.removeAllViews();
		View v = this.getLayoutInflater().inflate(R.layout.menu, null);
		TextView tv = (TextView) v.findViewById(R.id.name);
		tv.setText("Call Desk (x"+me.phone_ext+")");
		tv.setOnClickListener(new OnClickListener(){
		public void onClick(View arg0) {
			String prefix = me.is_lss?"tel:952918":"tel:781774";
			String url = prefix+me.phone_ext;
			System.out.println("url: "+url);
			Intent callIntent = new Intent(Intent.ACTION_CALL);
	        callIntent.setData(Uri.parse(url));
	        startActivity(callIntent);
		}});
		ll.addView(v, 0);
		ll.requestLayout();
		
	}

}
