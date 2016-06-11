package christian.online.prayer.utils;

import android.content.Context;
import android.os.CountDownTimer;

import java.util.concurrent.TimeUnit;

/**
 * Created by ppobd_six on 5/18/2016.
 */
public class CounterClass extends CountDownTimer {
    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */

    OnTickedListener onTickedListener;
    OnFinishedListener onFinishedListener;
    long countDowntill;
    Context context;

    public CounterClass(long millisInFuture, long countDownInterval, Context context) {
        super(millisInFuture, countDownInterval);
        this.context = context;
        this.countDowntill = millisInFuture;
        try {
            onTickedListener = (OnTickedListener) context;
            onFinishedListener = (OnFinishedListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement all interfaces");
        }
    }

    @Override
    public void onTick(long millisUntilFinished) {
        long millis = millisUntilFinished;
        String hms = String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                        .toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                        .toMinutes(millis)));
        onTickedListener.onTicked(hms);
    }

    @Override
    public void onFinish() {
        onFinishedListener.onFinished();
    }

    public interface OnTickedListener {
        public void onTicked(String hms);
    }

    public interface OnFinishedListener{
        public void onFinished();
    }

}
