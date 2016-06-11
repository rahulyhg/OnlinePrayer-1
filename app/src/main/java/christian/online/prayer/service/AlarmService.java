package christian.online.prayer.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import christian.online.prayer.HomeActivity;
import christian.online.prayer.R;
import christian.online.prayer.data.StaticData;
import christian.online.prayer.interfaces.APIServiceInterface;
import christian.online.prayer.reciever.AlarmReceiver;
import christian.online.prayer.responses.GetTimeResponse;
import christian.online.prayer.utils.ApplicationUtility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlarmService extends IntentService {
    public AlarmManager alarmManager;
    Intent alarmIntent;
    //   PendingIntent pendingIntent;
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "BANANEALARM";
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent, pendingIntent2;
    long time_next;
    Retrofit retrofit;
    APIServiceInterface apiService;

    public AlarmService() {
        super("AlarmService");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // don't notify if they've played in last 24 hr
        Log.i(TAG, "Alarm Service has started.");
        Context context = this.getApplicationContext();

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent mIntent = new Intent(this, HomeActivity.class);
        pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Resources res = this.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setTicker(res.getString(R.string.notification_title))
                .setAutoCancel(true)
                .setContentTitle(res.getString(R.string.notification_title))
                .setContentText(res.getString(R.string.notification_subject))
                .setSound(soundUri);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Log.i(TAG, "POPO");

        initWebService();
        getTimeFromAPI();

    }

    private void initWebService() {
        retrofit = new Retrofit.Builder()
                .baseUrl(StaticData.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(APIServiceInterface.class);
    }

    protected void getTimeFromAPI() {
        String tag = "gettime";
        Call<GetTimeResponse> call = apiService.getTime(tag);
        call.enqueue(getTimeResponseCallback);
    }

    Callback<GetTimeResponse> getTimeResponseCallback = new Callback<GetTimeResponse>() {
        @Override
        public void onResponse(Call<GetTimeResponse> call, Response<GetTimeResponse> response) {
            setTimeActions(response.body());
        }

        @Override
        public void onFailure(Call<GetTimeResponse> call, Throwable t) {
            Log.e(TAG, t.getMessage());
        }
    };

    private void setTimeActions(GetTimeResponse getTimeResponse) {
        if (!getTimeResponse.isError()) {
            time_next = ApplicationUtility.getCurrentPrayTime(getTimeResponse.getNext_time());
            Log.d(TAG, "alarmserViCE ::" + " next " + time_next);

            time_next = time_next - (60 * 60 * 1000);
            Log.d(TAG, "alarmserViCE ::" + " next alarm time " + time_next);

            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            alarmIntent = new Intent(AlarmService.this, AlarmReceiver.class);
            pendingIntent2 = PendingIntent.getBroadcast(AlarmService.this, 0, alarmIntent, 0);

            Log.d(TAG, "alarmserViCE ::" + " just time " + time_next);
            alarmManager.setRepeating(AlarmManager.RTC, time_next, 0, pendingIntent2);
        } else {
            String errorMsg = getTimeResponse.getTag();
            Log.e(TAG, errorMsg);
        }
    }


}