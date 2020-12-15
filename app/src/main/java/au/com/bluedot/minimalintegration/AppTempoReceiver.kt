package au.com.bluedot.minimalintegration

import android.content.Context
import android.os.Looper
import android.widget.Toast
import au.com.bluedot.point.net.engine.BDError
import au.com.bluedot.point.net.engine.TempoTrackingReceiver

class AppTempoReceiver : TempoTrackingReceiver() {
  private val handler = android.os.Handler(Looper.getMainLooper())

  /**
   * Called when there is an error that has caused Tempo to stop.
   *
   * @param error: can be a [TempoInvalidDestinationIdError][au.com.bluedot.point.TempoInvalidDestinationIdError]
   * or a [BDTempoError][au.com.bluedot.point.BDTempoError]
   * @param context: Android context
   * @since 15.3.0
   */
  override fun tempoStoppedWithError(error: BDError, context: Context) {
    handler.post(Runnable {
      Toast.makeText(context, "Error during Tempo ${error.reason}", Toast.LENGTH_LONG).show()
    })
  }
}