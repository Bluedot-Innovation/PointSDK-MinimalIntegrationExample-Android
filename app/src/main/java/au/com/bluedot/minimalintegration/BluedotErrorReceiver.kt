package au.com.bluedot.minimalintegration

import android.content.Context
import android.os.Looper
import android.widget.Toast
import au.com.bluedot.point.net.engine.BDError
import au.com.bluedot.point.net.engine.BluedotServiceReceiver

class BluedotErrorReceiver : BluedotServiceReceiver() {
  private val handler = android.os.Handler(Looper.getMainLooper())

  /**
   * Called when the Bluedot Point SDK encounters errors. If the error is fatal, the SDK services
   * may need to be restarted after the cause of the error has been addressed.
   *
   * @param[error] The error, please see [documentation](https://docs.bluedot.io/android-sdk/android-error-handling/)
   * for possible subtypes and appropriate corrective actions.
   * @param[context] Android context.
   * @since 15.3.0
   */
  override fun onBluedotServiceError(error: BDError, context: Context) {
    handler.post(Runnable {
      Toast.makeText(context, "Bluedot Service Error ${error.reason}", Toast.LENGTH_LONG).show()
    })
  }
}