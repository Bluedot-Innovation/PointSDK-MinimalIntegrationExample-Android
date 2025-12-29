package au.com.bluedot.minimalintegration;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.event.GeoTriggerEvent;

public class AppGeoTriggerReceiver extends GeoTriggeringEventReceiver {
    /**
     * This method is invoked whenever the set of zones is updated. There are a number of situations
     * when zone updates can happen, such as initialising the SDK, periodic update, significant location
     * change or zone sync event from Canvas.
     */
    @Override public void onZoneInfoUpdate(@NotNull Context context) {
        Toast.makeText(context, "Rules Updated",
                                  Toast.LENGTH_LONG).show();
        //Access the Zone details from ServiceManager.getInstance(context).getZonesAndFences()
        Log.i("MinApp", " Zone size "+ ServiceManager.getInstance(context).getZonesAndFences().size());
    }

    /**
     * This method is invoked when the SDK registers an entry event into a geo-feature.
     * There can be only one entry event per zone. However, after the minimum trigger time lapses,
     * or a corresponding exit event occurs, the entry event may occur again.
     * @param geoTriggerEvent Provides details of the entry event.
     */
    @Override
    public void onZoneEntryEvent(@NotNull GeoTriggerEvent geoTriggerEvent, @NotNull Context context) {
        String entryDetails = "Entered zone "+geoTriggerEvent.getZoneInfo().getName()+" via fence "+ Objects.requireNonNull(geoTriggerEvent.entryEvent()).getFenceName();
        String customData = geoTriggerEvent.getZoneInfo().getCustomData().toString();
        Log.i("MinApp", "Entry is "+ geoTriggerEvent);
        Toast.makeText(context, entryDetails + customData,
                       Toast.LENGTH_LONG).show();
    }

    /**
     * This method is invoked when the SDK registers an exit event. An exit event can be triggered if
     * the geo-feature is configured to trigger on exit. The option to enable exit events can be found
     * under project and zone configuration on Canvas. An exit event is a pending event and might occur
     * hours later after an entry event. Currently there is timeout for an exit of 24 hours. If an
     * exit wasn't triggered by that time, an automatic exit event will be registered.
     * @param geoTriggerEvent Provides details of the exit event.
     */
    @Override
    public void onZoneExitEvent(@NotNull GeoTriggerEvent geoTriggerEvent, @NotNull Context context) {
        String exitDetails = "Exited zone" + geoTriggerEvent.getZoneInfo().getName();
        String dwellT = "Dwell time: " + Objects.requireNonNull(geoTriggerEvent.exitEvent()).getDwellTime()+ " ms";

        Toast.makeText(context, exitDetails + dwellT,
                                  Toast.LENGTH_LONG).show();
    }

    @Override
    public void onZoneDwellEvent(@NonNull GeoTriggerEvent geoTriggerEvent, @NonNull Context context) {
        String dwellDetails = "Dwelled in zone " + geoTriggerEvent.getZoneInfo().getName();
        String dwellT = "Dwell time: " + Objects.requireNonNull(geoTriggerEvent.dwellEvent()).getCalculatedDwellTime()+ " ms";
        Log.i("MinApp", "Dwell is "+ geoTriggerEvent);
        Toast.makeText(context, "Dwell: " + dwellDetails + " " + dwellT,
                Toast.LENGTH_LONG).show();
    }
}
