package medi.mouse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

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
	static int TRANS_DUR=100;
	menu_node root,current;
	protected boolean one_shot=false;
	protected coretrax_post trax_post;
	boolean menu_built = false;
	public void onCreate(Bundle savedInstanceState) {
			
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.view_user);   
	        String username,stafflink,full_name,status,mydate;
	        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
	        
	        username = spref.getString("user_name", "");
	    	stafflink = spref.getString("stafflink", "");
	    	String password = spref.getString("user_password","");
	    	
	        me = new medi_person(this,1);
	        
	        me.username=username;
			me.password=password;
			
	        trax_post = new coretrax_post();
			trax_post.execute(me);
	    	
			
			
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
	    	
	    		        
	}
	private String[] toArray(medi.mouse.coretrax_post.menu_node menu_node){
		
		
		String[] ret = new String[menu_node.keyList.size()];
		for (int x=0; x<menu_node.keyList.size();x++){
			ret[x]=menu_node.keyList.get(x);
		}
		return ret;
	}
	public void build_menu(){
		Log.d("EditStatus","bulding menu 1");
    	String[] date = toArray(trax_post.root_menu.menu.get("date"));
        menu_node dates = new menu_node(date);
        Log.d("EditStatus","bulding menu 2");
        String[] bldg = toArray(trax_post.root_menu.menu.get("bldg"));
        
        menu_node building = new menu_node(bldg);
        Log.d("EditStatus","bulding menu 3");
        String[] in = toArray(trax_post.root_menu.menu.get("in"));
        
        menu_node sign_in = new menu_node(in,building);
        
        String[] out = toArray(trax_post.root_menu.menu.get("out"));
        
        menu_node sign_out = new menu_node(out,dates);
        
        menu_node[] root_futures = {sign_in,sign_out};
        
        
        String[] options = getResources().getStringArray(R.array.in_out);
        
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
							new submit_button(passdown,selections);
							
						}
	        			
	        		});
				} else {
					tv.setOnClickListener(new menu_clicker(x));
				}
		        ll.addView(v, x);
		        
		        
	        }
	        
	        
	        
	        TranslateAnimation anim = new TranslateAnimation(EditStatus.TRANS_START, 0, 0, 0);
		    anim.setDuration(EditStatus.TRANS_DUR);
	        //anim.setDuration(100);
		    anim.setFillAfter(true);
		    
	        TableLayout lp = (TableLayout) me.context.findViewById(R.id.options_table_view);
	        lp.refreshDrawableState();
	        lp.startAnimation(anim);
	        
	        Log.d("EditStatus","refresh screen");
	        
	        
	        
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
					for(String s: passdown){
						Log.d("EditStatus","passdown: "+s);
					}
					
					futures[pos].display(passdown,selections);
			        
		        }
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
                                        //passdown holds all of the coretrax_post.menu keys
                                        Log.d("EditStatus","submit this!");
                                        for(String s: passdown){
                                        	Log.d("EditStatus",s);
                                        }
                                        coretrax_post trax_post = new coretrax_post();
                                        trax_post.postthis(passdown,me);
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

	@Override
	public void onPostExecute(medi_person result) {
		if(!menu_built){
			if(result.webview.contains("Network Error")){
				finish();
				return;
			} 
			menu_built=true;
			build_menu();
			
			
		} else {
			
			SharedPreferences spref=
					PreferenceManager.getDefaultSharedPreferences(me.context);
			SharedPreferences.Editor editor = spref.edit();
			editor.putString("full_name", me.full_name);
			editor.putString("status", me.status);
			editor.putString("date", me.date);
			
			editor.commit();
			finish();
		}

		
		
	}

		
	}
