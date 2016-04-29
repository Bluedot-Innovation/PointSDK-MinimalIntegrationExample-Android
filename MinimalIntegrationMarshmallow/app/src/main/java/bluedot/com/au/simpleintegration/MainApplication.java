package bluedot.com.au.simpleintegration;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import java.util.List;

import au.com.bluedot.application.model.Proximity;
import au.com.bluedot.application.model.geo.Fence;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.BeaconInfo;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.ZoneInfo;

/**
 * Created by Bluedot Innovation on 20/01/16.
 */
public class MainApplication extends Application implements ServiceStatusListener, ApplicationNotificationListener {

    ServiceManager mServiceManager;

    String packageName = "";   //Package name for the App
    String apiKey = ""; //API key for the App
    String emailId = ""; //Registration email Id
    boolean restartMode = true;

    @Override
    public void onCreate() {
        super.onCreate();

        //init Point SDK
        initPointSDK();
    }

    public void initPointSDK() {

        int checkPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if(checkPermission == PackageManager.PERMISSION_GRANTED) {
            mServiceManager = ServiceManager.getInstance(this);
            if(!mServiceManager.isBlueDotPointServiceRunning()) {
                mServiceManager.sendAuthenticationRequest(packageName,apiKey,emailId,this,restartMode);
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
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Fence
     */
    @Override
    public void onCheckIntoFence(Fence fence, ZoneInfo zoneInfo, Location location, boolean isCheckOut) {

    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from fence under that Zone
     * @param fence     - Fence user is checked out from
     * @param zoneInfo  - Zone information Fence belongs to
     * @param dwellTime - time spent inside the Fence; in minutes
     */
    @Override
    public void onCheckedOutFromFence(Fence fence, ZoneInfo zoneInfo, int dwellTime) {

    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and check into any beacon under that Zone
     * @param beaconInfo - Beacon triggered
     * @param zoneInfo   - Zone information Beacon belongs to
     * @param location   - geographical coordinate where trigger happened
     * @param proximity  - the proximity at which the trigger occurred
     * @param isCheckOut - CheckOut will be tracked and delivered once device left the Beacon advertisement range
     */
    @Override
    public void onCheckIntoBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, Location location, Proximity proximity, boolean isCheckOut) {

    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and checked out from beacon under that Zone
     * @param beaconInfo - Beacon is checked out from
     * @param zoneInfo   - Zone information Beacon belongs to
     * @param dwellTime  - time spent inside the Beacon area; in minutes
     */
    @Override
    public void onCheckedOutFromBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, int dwellTime) {

    }
}
