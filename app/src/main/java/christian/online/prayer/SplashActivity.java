package christian.online.prayer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import christian.online.prayer.data.StaticData;
import christian.online.prayer.utils.ApplicationUtility;
import christian.online.prayer.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;

        ApplicationUtility.printKeyHash(this);
        final boolean isSessionExists = SessionManager.getSessionFromPref(context);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isSessionExists) {
                    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    finish();
                }
            }
        }, 1000);
    }
}
