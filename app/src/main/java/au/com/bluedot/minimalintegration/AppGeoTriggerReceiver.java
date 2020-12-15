package au.com.bluedot.minimalintegration;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver;
import au.com.bluedot.point.net.engine.ZoneEntryEvent;
import au.com.bluedot.point.net.engine.ZoneExitEvent;
import au.com.bluedot.point.net.engine.ZoneInfo;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class AppGeoTriggerReceiver extends GeoTriggeringEventReceiver {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void onZoneInfoUpdate(@NotNull List<ZoneInfo> list, @NotNull Context context) {
        //Using handler to pass Runnable into UI thread to interact with UI Elements
        handler.post(() -> Toast.makeText(context, "Rules Updated",
                                  Toast.LENGTH_LONG).show());
    }

    @Override
    public void onZoneExitEvent(@NotNull ZoneExitEvent zoneExitEvent, @NotNull Context context) {
        String exitDetails = "Exited zone" + zoneExitEvent.getZoneInfo().getZoneName();
        String dwellT = "Dwell time: " + zoneExitEvent.getDwellTime()+ " minutes";

        //Using handler to pass Runnable into UI thread to interact with UI Elements
        handler.post(() -> Toast.makeText(context, exitDetails + dwellT,
                                  Toast.LENGTH_LONG).show());
    }

    @Override
    public void onZoneEntryEvent(@NotNull ZoneEntryEvent zoneEntryEvent, @NotNull Context context) {
        String entryDetails = "Entered zone "+zoneEntryEvent.getZoneInfo().getZoneName()+" via fence "+ zoneEntryEvent.getFenceInfo().getName();
        String customData = "";
        if(zoneEntryEvent.getZoneInfo().getCustomData() != null)
            customData = zoneEntryEvent.getZoneInfo().getCustomData().toString();

        //Using handler to pass Runnable into UI thread to interact with UI Elements
        String finalCustomData = customData;
        handler.post(() -> Toast.makeText(context, entryDetails + finalCustomData,
                                          Toast.LENGTH_LONG).show());
    }
}
