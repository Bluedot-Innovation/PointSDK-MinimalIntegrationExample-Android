package au.com.bluedot.minimalintegration;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.GeoTriggeringService;
import au.com.bluedot.point.net.engine.InitializationResultListener;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.TempoService;
import au.com.bluedot.point.net.engine.TempoServiceStatusListener;
import org.jetbrains.annotations.Nullable;

import static android.app.Notification.PRIORITY_MAX;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
public class MainApplication extends Application implements TempoServiceStatusListener {


    ServiceManager mServiceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize point sdk
        initPointSDK();
    }

    public void initPointSDK() {

        boolean locationPermissionGranted =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (locationPermissionGranted) {
            mServiceManager = ServiceManager.getInstance(this);

            if (!mServiceManager.isBluedotServiceInitialized()) {

                InitializationResultListener resultListener = bdError -> {
                    String text = "Initialization Result ";
                    if(bdError != null)
                        text = text + bdError.getReason();
                    else
                        text = text + "Success ";
                    Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
                };
                //ProjectID for the App 
                String projectId = "1afc3ebb-bba7-404d-8d89-fa7539a1b7fa";
                mServiceManager.initialize(projectId, resultListener);
            }
        } else {
            requestPermissions();
        }
    }

    private void requestPermissions() {
        Intent intent = new Intent(getApplicationContext(), RequestPermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void reset(){
        mServiceManager.reset(bdError -> {
            String text = "Reset Finished ";
            if(bdError != null)
                text = text + bdError.getReason();
            else
                text = text + "Success ";
            Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
        });
    }

    void startGeoTrigger() {
        Notification notification = createNotification();

        GeoTriggeringService.builder()
                .notification(notification)
                .start(this, geoTriggerError -> {
                    if (geoTriggerError != null) {
                        Toast.makeText(getApplicationContext(),"Error in starting GeoTrigger"+geoTriggerError.getReason(),Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(),"GeoTrigger started successfully",Toast.LENGTH_LONG).show();

                });
    }

    void stopGeoTrigger() {
        GeoTriggeringService.stop(getApplicationContext(), bdError -> {
            String text = "GeoTrigger stop ";
            if (bdError != null) {
                text = text + bdError.getReason();
            } else {
                text = text + "Success ";
            }
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        });
    }

    void startTempo(){
        TempoService.builder()
                .notification(createNotification())
                .destinationId("newHome123")
                .start(getApplicationContext(), this);
    }

    /**
     * Creates notification channel and notification, required for foreground service notification.
     *
     * @return notification
     */

    private Notification createNotification() {

        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "Bluedot" + getString(R.string.app_name);
            String channelName = "Bluedot Service" + getString(R.string.app_name);
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notification = new Notification.Builder(getApplicationContext(), channelId)
                    .setContentTitle(getString(R.string.foreground_notification_title))
                    .setContentText(getString(R.string.foreground_notification_text))
                    .setStyle(new Notification.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.mipmap.ic_launcher);

            return notification.build();
        } else {

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.foreground_notification_title))
                    .setContentText(getString(R.string.foreground_notification_text))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setPriority(PRIORITY_MAX)
                    .setSmallIcon(R.mipmap.ic_launcher);

            return notification.build();
        }
    }

    @Override public void onTempoResult(@Nullable BDError bdError) {
        String text = "Tempo start";
        if (bdError != null) {
            text = text + bdError.getReason();
        } else {
            text = text + "Success";
        }
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}
