package christian.online.prayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

import christian.online.prayer.data.StaticData;
import christian.online.prayer.interfaces.APIServiceInterface;
import christian.online.prayer.reciever.AlarmReceiver;
import christian.online.prayer.responses.GetTimeResponse;
import christian.online.prayer.utils.ApplicationUtility;
import christian.online.prayer.utils.CounterClass;
import christian.online.prayer.utils.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity implements CounterClass.OnTickedListener, CounterClass.OnFinishedListener {

    public static String TAG = HomeActivity.class.getSimpleName();
    private TextView tvTimeRemaining, tvDay, tvTime, tvNPTR, tvJoinPrayer, tvInstall;
    private Button ivGotoMeeting;
    private Button ivPrayerResources,ivPrayerDays;
    private Toolbar mytoolbar;
    private SessionManager session;
    long notificationTIme;

    private static String URL1 = "http://onlineprayers.org/online-library-resources/";
    private static String URL2 = "http://onlineprayers.org/30-days-fasting-and-prayers/";
    public static String WEB_URL = "web_url";

    public AlarmManager alarmManager;
    PendingIntent pendingIntent;

    Context context;

    //Web Service
    Retrofit retrofit;
    APIServiceInterface apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;

        initWebService();
        initActionBar();
        initUI();
        setTypeFaces();
        addClickListeners();
        if (ApplicationUtility.checkInternet(context)) {
            getNextPrayerTimeFromServer();
        } else {
            ApplicationUtility.openNetworkDialog(this);
        }
        session = new SessionManager(getApplicationContext());
        setDataInUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareApp();
//            Toast.makeText(this,"RATE MY APP PLEASE",Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_logout) {
//            showPopup(MainActivity.this);
            logoutUser();
//            Toast.makeText(this,"THIS IS CALENDAR",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        mytoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mytoolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initUI() {
        tvTimeRemaining = (TextView) findViewById(R.id.tvTimeRemaining);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvDay = (TextView) findViewById(R.id.tvDay);
        tvNPTR = (TextView) findViewById(R.id.tvNPTR);
        tvJoinPrayer = (TextView) findViewById(R.id.tvJoinPrayer);
        tvInstall = (TextView) findViewById(R.id.tvInstall);
        ivGotoMeeting = (Button) findViewById(R.id.ivGotoMeeting);
        ivPrayerResources = (Button) findViewById(R.id.ivPrayerResources);
        ivPrayerDays = (Button) findViewById(R.id.ivPrayerDays);
    }

    private void setTypeFaces() {
        tvTimeRemaining.setTypeface(ApplicationUtility.getTimeTypeface(context));
        tvTime.setTypeface(ApplicationUtility.getTextTypeface(context));
        tvDay.setTypeface(ApplicationUtility.getTextTypeface(context));
        tvNPTR.setTypeface(ApplicationUtility.getTextTypeface(context));
        tvJoinPrayer.setTypeface(ApplicationUtility.getDetailTypeface(context));
        tvInstall.setTypeface(ApplicationUtility.getDetailTypeface(context));
    }

    private void initWebService() {
        retrofit = new Retrofit.Builder()
                .baseUrl(StaticData.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(APIServiceInterface.class);
    }

    private void shareApp(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=christian.online.prayer");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void logoutUser() {
        session.setLogin(false);
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void setDataInUI() {
//        tvDay.setText(ApplicationUtility.getCurrentDay());
//        tvTime.setText(ApplicationUtility.getCurrentTime());
    }

    private void addClickListeners() {
        ivGotoMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMeeting();
            }
        });

        ivPrayerResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,WebViewActivity.class);
                i.putExtra(WEB_URL,URL1);
              //  Log.e(WEB_URL,URL1);
                startActivity(i);
            }
        });

        ivPrayerDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,WebViewActivity.class);
                i.putExtra(WEB_URL,URL2);
               // Log.e(WEB_URL,URL2);
                startActivity(i);
            }
        });
    }

    private void gotoMeeting() {
        final String appPackageName = "com.citrixonline.android.gotomeeting"; // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }


    private void showMessage(String msg) {
        Toast toast = Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void getNextPrayerTimeFromServer() {
        String tag = "gettime";
        Call<GetTimeResponse> call = apiService.getTime(tag);
        call.enqueue(getTimeResponseCallback);
    }

    private void timeResponseAction(GetTimeResponse getTimeResponse) {
        String nextTime = ApplicationUtility.getNextTime(getTimeResponse.getNext_time());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        if (!getTimeResponse.getCurr_time().equalsIgnoreCase("Complete")) {
            long currentTime = ApplicationUtility.getCurrentPrayTime(getTimeResponse.getCurr_time());
            Log.e("PRAY TIME", currentTime+"");
            long timeRemaining = currentTime - calendar.getTimeInMillis();
            Log.e("CURRENT TIME", calendar.getTimeInMillis()+"");
            Log.e("REMAINING TIME", timeRemaining+"");
            ApplicationUtility.storeCurrentPrayerTime(currentTime, context);
            notificationTIme = currentTime - (60 * 60 * 1000);
            if (!ApplicationUtility.isAlarmed(context)) {
                setAlarm();
            }
            tvNPTR.setText("Today's prayer count down");
            CounterClass counterClass = new CounterClass(timeRemaining, 1000, context);
            counterClass.start();
        } else {
            ApplicationUtility.alarmIsSet(context, false);
            tvTimeRemaining.setText("WELCOME");
            tvNPTR.setText("TO ONLINE PRAYER");
        }
        tvTime.setText(nextTime);
        tvDay.setText(getTimeResponse.getNext_day());
    }

    Callback<GetTimeResponse> getTimeResponseCallback = new Callback<GetTimeResponse>() {
        @Override
        public void onResponse(Call<GetTimeResponse> call, Response<GetTimeResponse> response) {
            timeResponseAction(response.body());
        }

        @Override
        public void onFailure(Call<GetTimeResponse> call, Throwable t) {
            showMessage(t.getMessage());
        }
    };

    @Override
    public void onTicked(String hms) {
        tvTimeRemaining.setText(hms);
    }

    @Override
    public void onFinished() {
        if (ApplicationUtility.checkInternet(context)) {
            if (ApplicationUtility.checkTime(context)) {
                getNextPrayerTimeFromServer();
            }
        } else {
            ApplicationUtility.openNetworkDialog(this);
        }
    }

    public void setAlarm() {
        ApplicationUtility.alarmIsSet(context, true);
        Log.i(TAG, "Alarms set at "+notificationTIme);
        Intent myIntent = new Intent(HomeActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(HomeActivity.this, 0, myIntent, 0);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, notificationTIme, pendingIntent);

    }
}
