package medi.mouse;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ManagedClientConnection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class medi_person extends Activity{
	//hidden
	String username,password,full_name,stafflink,imglink;
	HashMap<String,String> found_people;
	//postable
	String status,date,out,in,loc,bldg,YYYYmmdd;

	//this could be big
	String text;

	//ui stuff
	medi_mouse_activity context;
	public String humanDate;

	boolean network_lock = false;
	boolean network_auth = false;
	boolean is_lss = false;
	/*what to post 
	 * im not sure if i can throw more then one object through
	 * the AsyncTask gateway so im internalizing everything ill need.
	 */

	public Map<String, String> data = new HashMap<String, String>();
	HttpClient client;
	public String webview;
	public String filelink;
	public String imgfile;
	String phone_ext;
	private String myStafflink;
	private boolean nosave;
	String found_stafflink;
	public String networkError;
	public HttpClient trax_client;
	public HttpClient core_client;
	public String lss_core_username;
	public String lss_core_password;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
	}
	public medi_person(medi_mouse_activity context){
        this.context=context;

        
        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(context);
        full_name = spref.getString("full_name", "");
    	status = spref.getString("status", "");
    	date = spref.getString("date", "");
    	
        
        
        username = spref.getString("user_name", "");
        password = spref.getString("user_password", "");
    	stafflink = spref.getString("stafflink", "");
    	imglink = spref.getString("imglink", "");
    	full_name = spref.getString("full_name", "");
    	status = spref.getString("status", "");
    	date = spref.getString("date", "");
    	imgfile = spref.getString("imgfile", "");
    	
    	is_lss = spref.getBoolean("is_lss", false);
    

        lss_core_username = spref.getString("lss_core_username", "");
        lss_core_password = spref.getString("lss_core_password", "");
    	
    	client = context.client;
		//trax_client = context.trax_client;
		//core_client = context.core_client;
        data = new HashMap<String, String>();
        
        //get current status
        //medi_post post = new medi_post(data);
    	//post.execute(this);
    
	}
	/**
	 * medi_person
	 * @param context
	 * @param message
	 * doesn't do initial load, stafflink and current 
	 * status should have already been found
	 */
	public medi_person(medi_mouse_activity context,int message){
        this.context=context;

        
        SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(context);
        username = spref.getString("user_name", "");
    	stafflink = spref.getString("stafflink", "");
    	imglink = spref.getString("imglink", "");
    	full_name = spref.getString("full_name", "");
    	status = spref.getString("status", "");
    	date = spref.getString("date", "");
    	imgfile = spref.getString("imgfile", "");
    	is_lss = spref.getBoolean("is_lss", false);
    	

        lss_core_username = spref.getString("lss_core_username", "");
        lss_core_password = spref.getString("lss_core_password", "");
    	
		client = context.client;
        data = new HashMap<String, String>();	    		
	}
	public medi_person(medi_mouse_activity context,
			String stafflink,
			String myStafflink){
		//not me, don't save anything
		this.nosave=true;
		this.stafflink=stafflink;
		this.myStafflink=myStafflink;
		this.context=context;
		SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(context);
		this.is_lss = spref.getBoolean("is_lss", false);
    	
	}
	
	public void secondaryLoad(){
		data = new HashMap<String, String>();
		if(stafflink!=null){
			data.put("TYPE","ViewUser");
			data.put("view","Photo");	
			data.put("stafflink",stafflink);
		}
	}
	public void primaryLoad(){
		data = new HashMap<String, String>();
		data.put("TYPE","TraxFrame");
		data.put("User",username);
	}
	public boolean hasStafflink(){
		return stafflink!=null&&(stafflink.length()>9);
	}
	public void submit(Activity context){
		if (!nosave){
			SharedPreferences spref=
					PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = spref.edit();
			editor.putString("full_name", full_name);
			editor.putString("status", status);
			editor.putString("date", date);
			editor.putString("stafflink",stafflink);
			editor.putString("imglink",imglink);
			editor.commit();
			
	        data = new HashMap<String, String>();
			//actual important stuff
			data.put("loc", loc);
			data.put("bldg", bldg);
			data.put("out", out);
			data.put("date", date);
			data.put("text", "");
			data.put("TextEdit","false");
			data.put("stafflink",stafflink);
			data.put("mystafflink",stafflink);
			data.put("YYYYMMDDdate", YYYYmmdd);
			data.put("TYPE","Save");
			
			medi_post post = new medi_post(data,is_lss);
	    	
			post.execute(this);
		}
	}
	
	public static String parse(String htmlobject, String pre,String post){
		
		if (htmlobject!=null){
			int index = htmlobject.indexOf(pre, 0);
			int end = htmlobject.indexOf(post, index+pre.length());
			//System.out.println(index+":"+pre+":"+end+":"+htmlobject.length());
			System.out.println("---"+(index+pre.length())+":"+end);
			if (index!=-1 && index+pre.length()<end) {
				//System.out.println(index+","+end+":::"+htmlobject.substring(index,end));
				
				String test = htmlobject.substring(index+pre.length(), end);
				System.out.println(index+","+end+":"+test);
				return test;
			}
		}		return "";
		
	}
	private String fix_stafflink(String stafflink) {
		int bad = stafflink.indexOf("\\",1);
		while (bad != -1){
			stafflink = stafflink.substring(0, bad)+ stafflink.substring(bad+1, stafflink.length());;
			bad = stafflink.indexOf("\\",1);
		}
		return stafflink;
	}
	public void parseresponse(String output, String type) {
		if(output.length()>0){
			if(type=="ViewUser"){
				full_name = parse(output, "<td valign=middle align=center>","<br>");
				imglink = "Photos/"+medi_person.parse(output,"Photos\\","\"");
				String prePhone ="Phone&nbsp;</td><td bgcolor=\"#e8ffe8\">&nbsp;";
				phone_ext = medi_person.parse(output,"Phone&nbsp;</td><td bgcolor=\"#e8ffe8\">&nbsp;","</td>");
				System.out.println("Phone: "+phone_ext);
				date = medi_person.parse(output, "date = '","';");
				out = medi_person.parse(output, "out = '","';");
				loc = medi_person.parse(output, "loc = '","';");
				bldg = medi_person.parse(output, "bldg = '","';");
				status = loc.length()>0?loc+", "+bldg:out;
			} else if (type=="Save"){
				
			} else if (type=="Lookup") {
				found_people = new HashMap<String, String>();
				String SLpre = "parent.changeUser('stafflink\\";
				String SLpost = "')";
				String Npre = ">";
				String Npost = "</a>";
				int index = output.indexOf(SLpre, 0);
				int end;
				String name,stafflink;
				while (index!=-1) {
					end = output.indexOf(SLpost, index+SLpre.length());
					System.out.println(index+":_:"+end);
					stafflink = "stafflink"+fix_stafflink(output.substring(index+SLpre.length(), end));
					
					
					index = output.indexOf(Npre,end);
					end = output.indexOf(Npost, index);
					System.out.println(index+":"+end);
					if(index==-1||end==-1){
						//stafflink & no name, only one result found
						index=-1;
						found_stafflink = stafflink;
						found_people = null;
					}else {
						name = output.substring(index+Npre.length(), end);
						System.out.println(name+": "+stafflink);
						
						found_people.put(name, stafflink);
						
						index = output.indexOf(SLpre,end);
					}
				}
			} else if(type=="TraxView") {
				date = medi_person.parse(output, "date = '","';");
				out = medi_person.parse(output, "out = '","';");
				loc = medi_person.parse(output, "loc = '","';");
				bldg = medi_person.parse(output, "bldg = '","';");
				status = loc.length()>0?loc+", "+bldg:out;
			}
			
			if (!hasStafflink()){
				String stafflink;
				stafflink = medi_person.parse(output, "stafflink = 'stafflink\\","';");
				stafflink = fix_stafflink(stafflink);

				System.out.println(stafflink);
				this.stafflink = "stafflink"+stafflink;
			}
			
			
			
			System.out.println("full name: "+full_name);
		}
	}
	public void loadauth() {
		SharedPreferences spref=PreferenceManager.getDefaultSharedPreferences(this);
    	
    	username = spref.getString("user_name", "");
    	password = spref.getString("user_password","");
    	
		
	}
}
