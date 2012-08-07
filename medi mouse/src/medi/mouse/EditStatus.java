package medi.mouse;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EditStatus extends medi_mouse_activity {
	static int TRANS_START=800;
	static int TRANS_DUR=500;
	menu_node root,current;
	ArrayList<myDate> inputDates;
	protected boolean one_shot=false;
	public void onCreate(Bundle savedInstanceState) {
			
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.view_user);   
	        String username,stafflink,full_name,status,mydate;
	        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
	        
	        username = spref.getString("user_name", "");
	    	stafflink = spref.getString("stafflink", "");
	    	String password = spref.getString("user_password","");
	    	
	        client = medi_post.connect(username, password);
	        me = new medi_person(this,1);
	    	
	    	
	    	full_name = spref.getString("full_name", "");
	    	status = spref.getString("status", "");
	    	mydate = spref.getString("date", "");
	    	//-------------------------------------------------------------------------
	    	//setup ui stuff
	    	TextView name_view = (TextView) findViewById(R.id.name_view);
	    	TextView status_view = (TextView) findViewById(R.id.status_view);
	    	TextView date_view = (TextView) findViewById(R.id.date_view);
	        ImageView picture = (ImageView) findViewById(R.id.picture_view);
	        
	    	name_view.setText(full_name);
	    	status_view.setText(status);
	    	date_view.setText(mydate);
	    	
	        inputDates = new ArrayList<myDate>();
	        Calendar cal = Calendar.getInstance();
	        String[] date = new String[7];
	        
	        for(int x = 0; x<7;x++){
	        	inputDates.add(x, new myDate(cal));
	        	date[x]=inputDates.get(x).human;
	            cal.add(Calendar.DATE,1);
	        }
	        menu_node dates = new menu_node(date);
	        
	        String[] options;
	        
	        if (me.is_lss){
		        options = getResources().getStringArray(R.array.lss_bldg);
	        } else {
	        	options = getResources().getStringArray(R.array.bldg);
	        }
	        menu_node building = new menu_node(options);
	        
	        
	        options = getResources().getStringArray(R.array.in);
	        
	        menu_node sign_in = new menu_node(options,building);
	        
	        
	        options = getResources().getStringArray(R.array.out);
	        menu_node sign_out = new menu_node(options,dates);
	        
	        menu_node[] root_futures = {sign_in,sign_out};
	        options = getResources().getStringArray(R.array.in_out);
	        
	        root = new menu_node(options, root_futures);
	        
	        root.display(new ArrayList<String>(),new ArrayList<Integer>());
	        
	}

	class menu_node  {
		
		String[] options;
		menu_node[] futures=null;
		ArrayList<Integer> selection;
		boolean doSubmit = false;
		ArrayList<String> prefix;
		public menu_node(String[] options) {
			this.options=options;
			this.doSubmit = true;
			Log.d("EditStatus","1"+doSubmit+this.options[0]);
		}
		public menu_node(String[] options,menu_node future) {
			this.options=options;
			this.futures=new menu_node[options.length];
			for(int x = 0; x<options.length;x++){
				futures[x]=future;
			}
			Log.d("EditStatus","2"+doSubmit+this.options[0]);
		}
		public menu_node(String[] options,menu_node[] futures){
			this.options=options;
			this.futures=futures;
			Log.d("EditStatus","3"+doSubmit+this.options[0]);
		}
		public void display(ArrayList<String> prefix,ArrayList<Integer> selection){
	        this.prefix=prefix;
	        this.selection=selection;
	        
	        System.out.println("options: "+options);
	        for(int d = 0;d<options.length;d++){
	        	Log.d("EditStatus",options[d]);
	        }
			TranslateAnimation anim = new TranslateAnimation(EditStatus.TRANS_START, 0, 0, 0);
		    anim.setDuration(EditStatus.TRANS_DUR);
		    anim.setFillAfter(true);
		    
	        TableLayout lp = (TableLayout) me.context.findViewById(R.id.options_table_view);
	        lp.startAnimation(anim);
	        
	        
	        
		        LinearLayout ll = (LinearLayout) findViewById(R.id.options_menu_view);
		        ll.removeAllViews();
		        
		        for (int x=0;x<this.options.length;x++){
		        	
		        	View v = EditStatus.this.getLayoutInflater().inflate(R.layout.menu, null);
			        TextView tv = (TextView) v.findViewById(R.id.name);
			        tv.setText(options[x]);
			        final int pos = x;
			        if (this.doSubmit) {
		        		tv.setOnClickListener(new OnClickListener(){

							public void onClick(View arg0) {
								ArrayList<String> next_prefix = menu_node.this.prefix;
								String next_options = menu_node.this.options[pos];
								next_prefix.add(next_options);
								ArrayList<String> passdown = next_prefix;
								ArrayList<Integer> selections = menu_node.this.selection;
								selections.add(pos);
								new submit_button(next_prefix,selections);
							}
		        			
		        		});
					} else {
						tv.setOnClickListener(new menu_clicker(x));
					}
			        ll.addView(v, x);
			        
		        }
		        
		        					
			
		}
	
		class menu_clicker implements OnClickListener {
			int pos;
	    	public menu_clicker(int pos){
	    		super();
	    		this.pos=pos;
	    	}
	    	
			public void onClick(View arg0) {
				ArrayList<String> next_prefix = menu_node.this.prefix;
				String next_options = menu_node.this.options[pos];
				next_prefix.add(next_options);
				ArrayList<String> passdown = next_prefix;
				ArrayList<Integer> selections = menu_node.this.selection;
				selections.add(pos);
				
				menu_node[] futures = menu_node.this.futures;
				
				if(futures[pos]!=null){
					
					futures[pos].display(passdown,selections);
			        
		        }
			}
	        
	}
	
	class submit_button {
		
		public submit_button(final ArrayList<String> passdown,final ArrayList<Integer> selection) {
			
			
	        
			LinearLayout ll = (LinearLayout) findViewById(R.id.options_menu_view);
			ll.removeAllViews();
	        String display = "";
	        for(int x =1; x < passdown.size(); x++){
	        	display += passdown.get(x)+" ";
	        }
	        
	        View v = EditStatus.this.getLayoutInflater().inflate(R.layout.menu, null);
	        TextView tv = (TextView) v.findViewById(R.id.name);
	        tv.setText("Please Confirm Selection");
	        ll.addView(v, 0);
	        
	        v = EditStatus.this.getLayoutInflater().inflate(R.layout.menu, null);
	        tv = (TextView) v.findViewById(R.id.name);
	        tv.setText(display);
	        tv.setOnTouchListener(new OnTouchListener(){
				private boolean one_shot=true;

				public boolean onTouch(View arg0, MotionEvent arg1) {
					if(one_shot) {
						one_shot=false;
						
						if(selection.get(0)==0){
							//signing in
							me.loc = me.in = passdown.get(1);
							me.bldg = passdown.get(2);
							me.out="";
							me.status = me.loc+", "+me.bldg;
							me.date=me.YYYYmmdd = "";
						}else if (selection.get(0)==1){
							
							//signing out
							me.out=(String) passdown.get(1);
							me.loc=me.bldg=me.in="";
							myDate date = inputDates.get(selection.get(2));
							me.status = me.out;
							me.date = date.human;
							me.YYYYmmdd = date.YYYYmmdd;
	
						}
						me.submit(EditStatus.this);
					}
					return true;
				}});
	        ll.addView(v, 1);
	        
			TranslateAnimation anim = new TranslateAnimation(EditStatus.TRANS_START, 0, 0, 0);
		    anim.setDuration(EditStatus.TRANS_DUR);
		    anim.setFillAfter(true);
		    anim.setDuration(TRANS_DUR);
	        anim.setFillAfter(true);
	        
	        TableLayout lp = (TableLayout) me.context.findViewById(R.id.options_table_view);
	        lp.startAnimation(anim);
	        
		}

		
	}
	}
	public class myDate{
    	Calendar date;
    	String human;
    	String YYYYmmdd;
    	public myDate(Calendar date){
    		this.date = date;
    		human = this.toString();
    		YYYYmmdd = this.toString(1);
    	}
    	
    	public String toString(){
    		int day = date.get(Calendar.DAY_OF_WEEK);
        	String day_of_week="";
        	switch(day){
        		case Calendar.SUNDAY:
        			day_of_week="Sun";
        			break;
        		case Calendar.MONDAY:
        			day_of_week="Mon";
        			break;
        		case Calendar.TUESDAY:
        			day_of_week="Tue";
        			break;
        		case Calendar.WEDNESDAY:
        			day_of_week="Wed";
        			break;
        		case Calendar.THURSDAY:
        			day_of_week="Thu";
        			break;
        		case Calendar.FRIDAY:
        			day_of_week="Fri";
        			break;
        		case Calendar.SATURDAY:
        			day_of_week="Sat";
        			break;
        	}
        	day =  date.get(Calendar.MONTH);
        	String month="";
        	String mm="";
        	switch(day){
        		case Calendar.JANUARY:
        			month="Jan";
        			mm="01";
        			break;
        		case Calendar.FEBRUARY:
        			month="Feb";
        			mm="02";
        			break;
        		case Calendar.MARCH:
        			month="Mar";
        			mm="03";
        			break;
        		case Calendar.APRIL:
        			month="Apr";
        			mm="04";
        			break;
        		case Calendar.MAY:
        			month="May";
        			mm="05";
        			break;
        		case Calendar.JUNE:
        			month="Jun";
        			mm="06";
        			break;
        		case Calendar.JULY:
        			month="Jul";
        			mm="07";
        			break;
        		case Calendar.AUGUST:
        			month="Aug";
        			mm="08";
        			break;
        		case Calendar.SEPTEMBER:
        			month="Sep";
        			mm="09";
        			break;
        		case Calendar.OCTOBER:
        			month="Oct";
        			mm="10";
        			break;
        		case Calendar.NOVEMBER:
        			month="Nov";
        			mm="11";
        			break;
        		case Calendar.DECEMBER:
        			month="Dec";
        			mm="12";
        			break;
        	}
        	
        	
        	int day_of_month = date.get(Calendar.DAY_OF_MONTH);
        	String yyyy = date.get(Calendar.YEAR)+"";
        	
        	String dd = day_of_month+"";
        	if(dd.length()==1){
        		dd="0"+dd;
        	}
        	this.YYYYmmdd=yyyy+mm+dd;
        	System.out.println(yyyy+mm+dd);
        	return month+" "+day_of_month+" "+day_of_week;

    	}
    	public String toString(int cmp){
    		this.toString();
    		return this.YYYYmmdd;
    		
    	}
    }

	@Override
	public void onPostExecute(medi_person result) {

		//double fix for issue 1
		if(me!=null&&me.webview!=null){
			
			SharedPreferences spref=
					PreferenceManager.getDefaultSharedPreferences(me.context);
			SharedPreferences.Editor editor = spref.edit();
			editor.putString("full_name", me.full_name);
			editor.putString("status", me.status);
			editor.putString("date", me.date);
			if(me.hasStafflink()){
				editor.putString("stafflink",me.stafflink);
			} else {
				editor.putString("stafflink","");
			}
			editor.putString("imglink",me.imglink);
			editor.commit();
			
		}

		
		if(me.webview!=null){
			WebView web_view = (WebView) me.context.findViewById(R.id.webview);
			
			/*
			web_view.setVisibility(View.VISIBLE);
			web_view.loadDataWithBaseURL(medi_post.BASE_URL, me.webview, 
					"text/html", "", medi_post.SITE);
			*/
			
		}
		finish();
	}

		
	}
