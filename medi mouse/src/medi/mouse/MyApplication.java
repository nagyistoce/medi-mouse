package medi.mouse;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import android.annotation.SuppressLint;
import android.app.Application;
//import android.os.Application;
/*
 * ACRA crash reporting
 * 
 * formkey is for a google doc that stores the crash output
 */
@SuppressLint("ParserError")
@ReportsCrashes(formKey = "", 
	mode = ReportingInteractionMode.TOAST,
	customReportContent = {ReportField.APP_VERSION_NAME, ReportField.PACKAGE_NAME,
		ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,  ReportField.LOGCAT, 
		ReportField.STACK_TRACE }, 
	resToastText = R.string.crashing,
	mailTo = "chewnoill@gmail.com") 
public class MyApplication extends Application {
	@Override
    public void onCreate() {
		
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        super.onCreate();
    }
}