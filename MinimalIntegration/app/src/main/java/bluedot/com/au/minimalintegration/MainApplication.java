package bluedot.com.au.minimalintegration;

import android.app.Application;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.util.List;

import au.com.bluedot.point.ApplicationNotification;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.ZoneInfo;

/**
 * @author Bluedot Innovation
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

        // initialize point sdk
        initPointSDK();
    }

    private void initPointSDK() {


        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            Toast.makeText(this, "GooglePlayServicesRepairableException happened while updating Security Provider", Toast.LENGTH_LONG).show();
            return;
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "GooglePlayServicesNotAvailableException happened while updating Security Provider", Toast.LENGTH_LONG).show();
            return;
        }

        mServiceManager = ServiceManager.getInstance(this);
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
     *
     * @param applicationNotification
     */
    @Override
    public void onCheckIntoFence(ApplicationNotification applicationNotification) {

    }

    /**
     * This callback happens when user is subscribed to Application Notification
     * and check into any beacon under that Zone
     *
     * @param applicationNotification
     */
    @Override
    public void onCheckIntoBeacon(ApplicationNotification applicationNotification) {

    }

}
