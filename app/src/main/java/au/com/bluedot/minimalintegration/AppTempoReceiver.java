package au.com.bluedot.minimalintegration;

import android.content.Context;
import android.widget.Toast;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.TempoTrackingReceiver;
import org.jetbrains.annotations.NotNull;

public class AppTempoReceiver extends TempoTrackingReceiver {
    /**
     * Called when there is an error that has caused Tempo to stop.
     *
     * @param bdError: can be a [TempoInvalidDestinationIdError][au.com.bluedot.point.TempoInvalidDestinationIdError]
     * or a [BDTempoError][au.com.bluedot.point.BDTempoError]
     * @param context: Android context
     * @since 15.3.0
     */
    @Override
    public void tempoStoppedWithError(@NotNull BDError bdError, @NotNull Context context) {
        Toast.makeText(context, "Error during Tempo " + bdError.getReason(),
                                  Toast.LENGTH_LONG).show();
    }
}