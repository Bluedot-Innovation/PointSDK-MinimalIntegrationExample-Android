package bluedot.com.au.minimalintegration;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import au.com.bluedot.application.model.Proximity;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.BDAuthenticationError;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.BeaconInfo;
import au.com.bluedot.point.net.engine.FenceInfo;
import au.com.bluedot.point.net.engine.LocationInfo;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.ZoneInfo;

import static android.app.Notification.PRIORITY_MAX;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
public class MainApplication extends Application implements ServiceStatusListener, ApplicationNotificationListener {


    ServiceManager mServiceManager;
    private NetworkChangeReceiver networkChangeReceiver;

    String packageName = "";   //Package name for the App
    String apiKey = ""; //API key for the App
    String emailId = ""; //Registration email Id
    // set this to true if you want to start the SDK with service sticky and auto-start mode on boot complete.
    // Please refer to Bluedot Developer documentation for further information.
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

    public void initPointSDK() {

        int checkPermissionCoarse = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int checkPermissionFine = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        if(checkPermissionCoarse == PackageManager.PERMISSION_GRANTED && checkPermissionFine == PackageManager.PERMISSION_GRANTED) {
            mServiceManager = ServiceManager.getInstance(this);

            if(!mServiceManager.isBlueDotPointServiceRunning()) {
                // Setting Notification for foreground service, required for Android Oreo and above.
                // Setting targetAllAPIs to TRUE will display foreground notification for Android versions lower than Oreo
                mServiceManager.setForegroundServiceNotification(createNotification(), false);
                mServiceManager.sendAuthenticationRequest(packageName,apiKey,emailId,this, restartMode);
            }
        }
        else
        {
            requestPermissions();
        }

    }

    private void requestPermissions() {

        Intent intent = new Intent(getApplicationContext(), RequestPermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

        //Internet Connectivity may not be available on boot complete
        //This is a retry strategy to make an attempt for authentication of SDK once internet connectivity is available.
        if(bdError instanceof BDAuthenticationError) {
            if(bdError.getReason().contains("Network is not available")) {
                if(mServiceManager != null && !mServiceManager.isBlueDotPointServiceRunning()) {
                    //SDK auth failed due to no network
                    networkChangeReceiver = new NetworkChangeReceiver();
                    registerReceiver(
                            networkChangeReceiver,
                            new IntentFilter(
                                    ConnectivityManager.CONNECTIVITY_ACTION));
                }
            }
        }
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
     * @param fenceInfo      - Fence triggered
     * @param zoneInfo   - Zone information Fence belongs to
     * @param location   - geographical coordinate where trigger happened
     * @param customData - custom data associated with this Custom Action
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Fence
     */
    @Override
    public void onCheckIntoFence(final FenceInfo fenceInfo, ZoneInfo zoneInfo, LocationInfo location, Map<String, String> customData, boolean isCheckOut) {
        //Using handler to pass Runnable into UI thread to interact with UI Elements
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Checked into fence: " + fenceInfo.getName(),Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from fence under that Zone
     * @param fenceInfo     - Fence user is checked out from
     * @param zoneInfo  - Zone information Fence belongs to
     * @param dwellTime - time spent inside the Fence; in minutes
     * @param customData - custom data associated with this Custom Action
     */
    @Override
    public void onCheckedOutFromFence(final FenceInfo fenceInfo, ZoneInfo zoneInfo, final int dwellTime, Map<String, String> customData) {
        //Using handler to pass Runnable into UI thread to interact with UI Elements
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Left: " + fenceInfo.getName() + " dwellTime,min=" + dwellTime, Toast.LENGTH_LONG)
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

    /**
     * Creates notification channel and notification, required for foreground service notification.
     * @return notification
     */

    private Notification createNotification() {

        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "Bluedot";
            String channelName = "Bluedot Service";
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notification = new Notification.Builder(getApplicationContext(), channelId)
                    .setContentText(getString(R.string.foreground_notification_title))
                    .setContentTitle(getString(R.string.foreground_notification_text))
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.mipmap.ic_launcher);

            return notification.build();
        } else {

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentText(getString(R.string.foreground_notification_title))
                    .setContentTitle(getString(R.string.foreground_notification_text))
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setPriority(PRIORITY_MAX)
                    .setSmallIcon(R.mipmap.ic_launcher);

            return notification.build();
        }
    }

    /**
     * Custom broadcast receiver to check for connectivity
     */
    class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(isInternetAvailable()) {
                initPointSDK();
                if (networkChangeReceiver != null) {
                    unregisterReceiver(networkChangeReceiver);
                }
            }
        }
    }

    /**
     * Check whether internet is available
     * @return
     */
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobile = false, isWifi = false;

        NetworkInfo[] infoAvailableNetworks = cm.getAllNetworkInfo();

        if (infoAvailableNetworks != null) {
            for (NetworkInfo network : infoAvailableNetworks) {

                if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (network.isConnected() && network.isAvailable())
                        isWifi = true;
                }
                if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (network.isConnected() && network.isAvailable())
                        isMobile = true;
                }
            }
        }

        return isMobile || isWifi;
    }
}
