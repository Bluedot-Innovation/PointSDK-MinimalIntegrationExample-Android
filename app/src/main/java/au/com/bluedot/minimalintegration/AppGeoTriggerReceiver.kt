package au.com.bluedot.minimalintegration

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver
import au.com.bluedot.point.net.engine.ZoneEntryEvent
import au.com.bluedot.point.net.engine.ZoneExitEvent
import au.com.bluedot.point.net.engine.ZoneInfo

class AppGeoTriggerReceiver : GeoTriggeringEventReceiver() {

  /**
   * This method is invoked when the SDK registers an entry event into a geofeature.
   * There can be only one entry event per zone. However, after the minimum retrigger time lapses,
   * or a corresponding exit event occurs, the entry event may occur again.
   * @param entryEvent Provides details of the entry event.
   */
  override fun onZoneEntryEvent(entryEvent: ZoneEntryEvent, context: Context) {

    val entryDetails = "Entered zone ${entryEvent.zoneInfo.zoneName} via fence ${entryEvent.fenceInfo.name}"
    val customData =
        if (entryEvent.zoneInfo.getCustomData().isNullOrEmpty()) ""
        else "Data: ${entryEvent.zoneInfo.getCustomData()}"
     val handler = Handler(Looper.getMainLooper())
    //Using handler to pass Runnable into UI thread to interact with UI Elements
    handler.post(Runnable {
      Toast.makeText(context, entryDetails + customData,
                     Toast.LENGTH_LONG).show()
    })
  }

  override fun onZoneExitEvent(exitEvent: ZoneExitEvent, context: Context) {
    val exitDetails = "Exited zone ${exitEvent.zoneInfo.zoneName}"
    val dwellT = "Dwell time: ${exitEvent.dwellTime} minutes"
     val handler = Handler(Looper.getMainLooper())
    //Using handler to pass Runnable into UI thread to interact with UI Elements
    handler.post(Runnable {
      Toast.makeText(context, exitDetails + dwellT,
                     Toast.LENGTH_LONG).show()
    })

  }

  override fun onZoneInfoUpdate(zones: List<ZoneInfo>, context: Context) {
     val handler = Handler(Looper.getMainLooper())
    //Using handler to pass Runnable into UI thread to interact with UI Elements
    handler.post(Runnable {
      Toast.makeText(context, "Rules Updated",
                     Toast.LENGTH_LONG).show()
    })
  }
}