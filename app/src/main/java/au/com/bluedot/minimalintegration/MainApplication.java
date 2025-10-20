package au.com.bluedot.minimalintegration;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.GeoTriggeringService;
import au.com.bluedot.point.net.engine.InitializationResultListener;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.TempoService;
import au.com.bluedot.point.net.engine.TempoServiceStatusListener;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bluedot Innovation
 * Copyright (c) 2025 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
public class MainApplication extends Application implements TempoServiceStatusListener {
    ServiceManager mServiceManager;
    private final static String projectId = "20c0d63a-7a06-4ed2-88e4-c275aa722cf5";   //ProjectId from Canvas
    private final static String destinationId = "eta123"; //destinationId to start Tempo

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
                    else {
                        text = text + "Success ";
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
                    Log.d("MinApp"," Initialized with projectID"+projectId);
                };
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
            else {
                text = text + "Success ";
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
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
        Map<String,String> eventMetadata = new HashMap<>();
        eventMetadata.put("hs_orderId", randomString());
        eventMetadata.put("hs_Customer Name", "Customer");
        mServiceManager.setCustomEventMetaData(eventMetadata);
        TempoService.builder()
                .notification(createNotification())
                .destinationId(destinationId)
                .start(getApplicationContext(), this);
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    private String randomString() {
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    /**
     * Creates notification channel and notification, required for foreground service notification.
     * @return notification
     */

    private Notification createNotification() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "Bluedot" + getString(R.string.app_name);

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
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.mipmap.ic_launcher);

        return notification.build();
    }

    @Override public void onTempoResult(@Nullable BDError bdError) {
        String text = "Tempo start ";
        if (bdError != null) {
            text = text + bdError.getReason();
        } else {
            text = text + "Success";
        }
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}
