package christian.online.prayer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.facebook.AccessToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import christian.online.prayer.R;
import christian.online.prayer.data.StaticData;

/**
 * Created by User on 29-Mar-16.
 */
public class ApplicationUtility {

    public static Bundle getFacebookData(JSONObject object, AccessToken accessToken) {

        try {
            Bundle bundle = new Bundle();
            String id = object.getString("id");
            String accesstokenUrl = StaticData.FACEBOOK_ACCESSTOKEN_URL + accessToken.getToken() + "&format=json";
            bundle.putString(StaticData.USER_ID, id);
            if (object.has(StaticData.USER_FIRST_NAME))
                bundle.putString(StaticData.USER_FIRST_NAME, object.getString(StaticData.USER_FIRST_NAME));
            if (object.has(StaticData.USER_MIDDLE_NAME))
                bundle.putString(StaticData.USER_MIDDLE_NAME, object.getString(StaticData.USER_MIDDLE_NAME));
            if (object.has(StaticData.USER_LAST_NAME))
                bundle.putString(StaticData.USER_LAST_NAME, object.getString(StaticData.USER_LAST_NAME));
            if (object.has(StaticData.USER_EMAIL))
                bundle.putString(StaticData.USER_EMAIL, object.getString(StaticData.USER_EMAIL));
            bundle.putString(StaticData.ACCESSTOKEN_URL, accesstokenUrl);
            return bundle;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRegId() {
        return "6669866845616";
    }


    public static boolean checkInternet(Context context) {
        ConnectivityManager check = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = check.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static void openNetworkDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(activity.getString(R.string.con_message))
                .setTitle(activity.getString(R.string.con_title)).setCancelable(false).setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        }).setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        }).create();
        builder.show();
    }

    public static void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(android.util.Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

    /*public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy"); // Date and
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        String currentDate = sdf.format(calendar.getTime());
        return currentDate;
    }*/

    public static String getCurrentDay() {
        SimpleDateFormat sdf_ = new SimpleDateFormat("EEEE");
        sdf_.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        Date date = new Date();
        String dayName = sdf_.format(date);
        return dayName;
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf_ = new SimpleDateFormat("hh:mm a");
        sdf_.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        Date date = new Date();
        String currentTime = sdf_.format(date);
        return currentTime;
    }

    public static Date formatDate(String inputDate, SimpleDateFormat sdFormat) {
        Date dt = null;
        try {
            dt = sdFormat.parse(inputDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dt;
    }

    public static String getNextTime(String time) {
        SimpleDateFormat sf = new SimpleDateFormat("hh:mm");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        Date nextDate = formatDate(time.substring(0, 5), sf);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        sdf.setTimeZone(TimeZone.getDefault());
        String nextTime = sdf.format(nextDate);
        return nextTime;
    }

    public static long getCurrentPrayTime(String currTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        String dateString = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + " " + currTime.substring(0, 5);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        Date nextDate = formatDate(dateString, sf);
        calendar.setTime(nextDate);
        Log.e("GMT 0 Time", calendar.getTime().toString());
        return calendar.getTimeInMillis();
    }

    public static Typeface getTimeTypeface(Context context) {
        Typeface timeTypeface =
                Typeface.createFromAsset(context.getAssets(), "fonts/time.ttf");
        return timeTypeface;
    }

    public static Typeface getTextTypeface(Context context) {
        Typeface timeTypeface =
                Typeface.createFromAsset(context.getAssets(), "fonts/text.ttf");
        return timeTypeface;
    }

    public static Typeface getDetailTypeface(Context context) {
        Typeface timeTypeface =
                Typeface.createFromAsset(context.getAssets(), "fonts/time_date_details.ttf");
        return timeTypeface;
    }

    public static void storeCurrentPrayerTime(long time, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(StaticData.APP_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(StaticData.CURRENT_PRAYER, time);
        editor.commit();
    }

    public static boolean checkTime(Context context) {
        Calendar calendar = Calendar.getInstance();
        long time = calendar.getTimeInMillis();
        long currTime = context.getSharedPreferences(StaticData.APP_PREFERENCE, Context.MODE_PRIVATE).getLong(StaticData.CURRENT_PRAYER, 0);
        if (time >= currTime) {
            return true;
        } else {
            return false;
        }
    }

    public static void alarmIsSet(Context context, boolean isAlarm) {
        SharedPreferences preferences = context.getSharedPreferences(StaticData.APP_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(StaticData.ALARM, isAlarm);
        editor.commit();
    }

    public static boolean isAlarmed(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(StaticData.APP_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean(StaticData.ALARM, false);
    }

}
