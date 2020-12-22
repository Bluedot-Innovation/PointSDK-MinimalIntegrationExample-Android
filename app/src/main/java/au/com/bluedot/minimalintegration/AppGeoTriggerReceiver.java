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
    /**
     * This method is invoked whenever the set of zones is updated. There are a number of situations
     * when zone updates can happen, such as initialising the SDK, periodic update, significant location
     * change or zone sync event from Canvas.
     * @param zones List of zones associated with the projectId
     */
    @Override public void onZoneInfoUpdate(@NotNull List<ZoneInfo> zones, @NotNull Context context) {
        Toast.makeText(context, "Rules Updated",
                                  Toast.LENGTH_LONG).show();
    }

    /**
     * This method is invoked when the SDK registers an entry event into a geofeature.
     * There can be only one entry event per zone. However, after the minimum retrigger time lapses,
     * or a corresponding exit event occurs, the entry event may occur again.
     * @param entryEvent Provides details of the entry event.
     */
    @Override
    public void onZoneEntryEvent(@NotNull ZoneEntryEvent entryEvent, @NotNull Context context) {
        String entryDetails = "Entered zone "+entryEvent.getZoneInfo().getZoneName()+" via fence "+ entryEvent.getFenceInfo().getName();
        String customData = "";
        if(entryEvent.getZoneInfo().getCustomData() != null)
            customData = entryEvent.getZoneInfo().getCustomData().toString();
        Toast.makeText(context, entryDetails + customData,
                       Toast.LENGTH_LONG).show();
    }

    /**
     * This method is invoked when the SDK registers an exit event. An exit event can be triggered if
     * the geofeature is configured to trigger on exit. The option to enable exit events can be found
     * under project and zone configuration on Canvas. An exit event is a pending event and might occur
     * hours later after an entry event. Currently there is timeout for an exit of 24 hours. If an
     * exit wasn't triggered by that time, an automatic exit event will be registered.
     * @param exitEvent Provides details of the exit event.
     */
    @Override
    public void onZoneExitEvent(@NotNull ZoneExitEvent exitEvent, @NotNull Context context) {
        String exitDetails = "Exited zone" + exitEvent.getZoneInfo().getZoneName();
        String dwellT = "Dwell time: " + exitEvent.getDwellTime()+ " minutes";

        Toast.makeText(context, exitDetails + dwellT,
                                  Toast.LENGTH_LONG).show();
    }
}
