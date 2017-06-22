package bluedot.com.au.minimalintegration;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import au.com.bluedot.application.model.Proximity;
import au.com.bluedot.application.model.geo.Fence;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.BeaconInfo;
import au.com.bluedot.point.net.engine.LocationInfo;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.ZoneInfo;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2016 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
public class MainApplication extends Application implements ServiceStatusListener, ApplicationNotificationListener {


    ServiceManager mServiceManager;

    String packageName = "";   //Package name for the App
    String apiKey = ""; //API key for the App
    String emailId = ""; //Registration email Id
    boolean restartMode = true;
    private Handler handler;


    @Override
    public void onCreate() {
        super.onCreate();

        //Initializing Handler bind to UI Thread
        handler = new Handler(Looper.getMainLooper());
        // initialize point sdk
        initPointSDK();
    }

    private void initPointSDK() {
        mServiceManager = ServiceManager.getInstance(this);

        // Android O handling - Set the foreground Service Notification which will fire only if running on Android O and above
        Intent actionIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT );
        mServiceManager.setForegroundServiceNotification(R.mipmap.ic_launcher, getString(R.string.foreground_notification_title), getString(R.string.foreground_notification_text), pendingIntent);

        if(!mServiceManager.isBlueDotPointServiceRunning()){
            mServiceManager.sendAuthenticationRequest(packageName,apiKey,emailId,this,restartMode);
        }
    }


    /**
     * <p>It is called when BlueDotPointService started successful, your app logic code using the Bluedot service could start from here.</p>
     * <p>This method is off the UI thread.</p>
     */
    @Override
    public void onBlueDotPointServiceStartedSuccess() {
        mServiceManager.subscribeForApplicationNotification(this);

    }

    /**
     * <p>This method notifies the client application that BlueDotPointService is stopped. Your app could release the resources related to Bluedot service from here.</p>
     * <p>It is called off the UI thread.</p>
     */
    @Override
    public void onBlueDotPointServiceStop() {
        mServiceManager.unsubscribeForApplicationNotification(this);
    }

    /**
     * <p>The method delivers the error from BlueDotPointService by a generic BDError. There are several types of error such as
     * - BDAuthenticationError (fatal)
     * - BDNetworkError (fatal / non fatal)
     * - LocationServiceNotEnabledError (fatal / non fatal)
     * - RuleDownloadError (non fatal)
     * - BLENotAvailableError (non fatal)
     * - BluetoothNotEnabledError (non fatal)
     * <p> The BDError.isFatal() indicates if error is fatal and service is not operable.
     * Followed by onBlueDotPointServiceStop() indicating service is stopped.
     * <p> The BDError.getReason() is useful to analyse error cause.
     * @param bdError
     */
    @Override
    public void onBlueDotPointServiceError(BDError bdError) {

    }

    /**
     * <p>The method deliveries the ZoneInfo list when the rules are updated. Your app is able to get the latest ZoneInfo when the rules are updated.</p>
     * @param list
     */
    @Override
    public void onRuleUpdate(List<ZoneInfo> list) {

    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and check into any fence under that Zone
     * @param fence      - Fence triggered
     * @param zoneInfo   - Zone information Fence belongs to
     * @param location   - geographical coordinate where trigger happened
     * @param customData - custom data associated with this Custom Action
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Fence
     */
    @Override
    public void onCheckIntoFence(final Fence fence, ZoneInfo zoneInfo, LocationInfo location, Map<String, String> customData, boolean isCheckOut) {
        //Using handler to pass Runnable into UI thread to interact with UI Elements
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Checked into fence: " + fence.getName(),Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from fence under that Zone
     * @param fence     - Fence user is checked out from
     * @param zoneInfo  - Zone information Fence belongs to
     * @param dwellTime - time spent inside the Fence; in minutes
     * @param customData - custom data associated with this Custom Action
     */
    @Override
    public void onCheckedOutFromFence(final Fence fence, ZoneInfo zoneInfo, final int dwellTime, Map<String, String> customData) {
        //Using handler to pass Runnable into UI thread to interact with UI Elements
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Left: " + fence.getName() + " dwellTime,min=" + dwellTime, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and check into any beacon under that Zone
     * @param beaconInfo - Beacon triggered
     * @param zoneInfo   - Zone information Beacon belongs to
     * @param location   - geographical coordinate where trigger happened
     * @param proximity  - the proximity at which the trigger occurred
     * @param customData - custom data associated with this Custom Action
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Beacon advertisement range
     */
    @Override
    public void onCheckIntoBeacon(final BeaconInfo beaconInfo, ZoneInfo zoneInfo, LocationInfo location, Proximity proximity, Map<String, String> customData, boolean isCheckOut) {
        //Using handler to pass Runnable into UI thread to interact with UI Elements
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Entered: " + beaconInfo.getName(),Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from beacon under that Zone
     * @param beaconInfo - Beacon is checked out from
     * @param zoneInfo   - Zone information Beacon belongs to
     * @param dwellTime  - time spent inside the Beacon area; in minutes
     * @param customData - custom data associated with this Custom Action
     */
    @Override
    public void onCheckedOutFromBeacon(final BeaconInfo beaconInfo, ZoneInfo zoneInfo, final int dwellTime, Map<String, String> customData) {
        //Using handler to pass Runnable into UI thread to interact with UI Elements
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Left: " + beaconInfo.getName() + " dwellTime,min=" + dwellTime, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}
