package christian.online.prayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import christian.online.prayer.data.StaticData;

/**
 * Created by ppobd_six on 5/17/2016.
 */
public class SessionManager {

    public static String TAG = SessionManager.class.getSimpleName();

    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    public static boolean getSessionFromPref(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(StaticData.APP_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(StaticData.SESSION, false);
    }

    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(StaticData.APP_PREFERENCE, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn){
        editor.putBoolean(StaticData.SESSION
                , isLoggedIn);
        editor.commit();

        Log.d(TAG, "User login modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(StaticData.SESSION, false);
    }


}
