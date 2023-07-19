package au.com.bluedot.minimalintegration;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.TempoTrackingReceiver;
import au.com.bluedot.point.net.engine.event.TempoTrackingUpdate;

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

    @Override
    public void onTempoTrackingUpdate(@NonNull TempoTrackingUpdate tempoTrackingUpdate, @NonNull Context context) {
        Toast.makeText(context, "Tempo update " + tempoTrackingUpdate,
                Toast.LENGTH_LONG).show();
    }
}