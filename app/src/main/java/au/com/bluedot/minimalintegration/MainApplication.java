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
import android.os.Build;
import android.util.Log;
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

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
 * @author Bluedot Innovation
 * Copyright (c) 2018 Bluedot Innovation. All rights reserved.
 * MainApplication demonstrates the implementation Bluedot Point SDK and related callbacks.
 */
public class MainApplication extends Application implements TempoServiceStatusListener {
    ServiceManager mServiceManager;
    private final static String projectId = "57eece2b-0989-4adc-8dc7-bcf3cf44cdd4";   //ProjectId from Canvas
    private final static String destinationId = "<TEMPO-DESTINATION-ID>"; //destinationId to start Tempo

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
        Map<String,String> eventMetadata = new HashMap<String, String>();
        eventMetadata.put("hs_orderId", randomString(5));
        eventMetadata.put("hs_Customer Name", "Customer");
        mServiceManager.setCustomEventMetaData(eventMetadata);
        TempoService.builder()
                .notification(createNotification())
                .destinationId(destinationId)
                .start(getApplicationContext(), this);
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    private String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
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

        String channelId  = "Bluedot" + getString(R.string.app_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        } else {

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setContentTitle(getString(R.string.foreground_notification_title))
                    .setContentText(getString(R.string.foreground_notification_text))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(PRIORITY_MAX)
                    .setSmallIcon(R.mipmap.ic_launcher);
            return notification.build();
        }
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
