package medi.mouse;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import android.app.Application;
//import android.os.Application;
/*
 * ACRA crash reporting
 * 
 * formkey is for a google doc that stores the crash output
 */
@ReportsCrashes(formKey = "dFhnRHR6Qy02aFZnSE9LU2NEVDRuY1E6MQ", 
mode = ReportingInteractionMode.TOAST,
resToastText = R.string.crashing) 
public class MyApplication extends Application {
	@Override
    public void onCreate() {
		
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        super.onCreate();
    }
}