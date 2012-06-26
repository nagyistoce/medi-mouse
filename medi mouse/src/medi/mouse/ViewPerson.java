package medi.mouse;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
		medi_post postme = new medi_post(me.data);
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


		ListView lv = (ListView) findViewById(R.id.list_view);
        
        lv.requestLayout();
        String[] options = {"Call Desk (x"+me.phone_ext+")",
        		"",
        		"Refresh"};
        
        
        
        lv.setAdapter(new ArrayAdapter<String>(this, 
        			R.layout.list_item, options));
        lv.setOnItemClickListener(new OnItemClickListener(){
        	//stafflink\cohen,william.cdb
        	
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
					case 0:
						//call desk
						String url = "tel:781774"+me.phone_ext;
						System.out.println("url: "+url);
						Intent callIntent = new Intent(Intent.ACTION_CALL);
				        callIntent.setData(Uri.parse(url));
				        startActivity(callIntent);
				        
					case 1:
						//Find Person
						
						
						break;
					case 2:
						//Refresh
						
					}
					
				
					
			}});

    	

	}

}
