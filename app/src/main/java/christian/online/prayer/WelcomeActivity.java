package christian.online.prayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import christian.online.prayer.adapters.WelcomePagerAdapter;
import christian.online.prayer.data.StaticData;
import christian.online.prayer.fragment.WelcomeScreen1;
import christian.online.prayer.fragment.WelcomeScreen2;
import christian.online.prayer.interfaces.APIServiceInterface;
import christian.online.prayer.responses.SignUpResponse;
import christian.online.prayer.utils.ApplicationUtility;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WelcomeActivity extends AppCompatActivity {

    TextView tvLogin, tvRegister, tvFacebook;
    ViewPager vpWelcome;
    CircleIndicator circleIndicator;
    RelativeLayout rlProgress;//, rlPagerContainer;

    //Facebok
    CallbackManager callbackManager;
    LoginManager loginManager;
    //    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    Bundle userBundle;

    //Google
    private static final String TAG = "WelcomeActivity";

    //Web Service
    Retrofit retrofit;
    APIServiceInterface apiService;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isFirstTime = true;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        context = this;

        checkInternet();
        FacebookSdk.sdkInitialize(context);

        initSharePref();
//        initGoogleSignIn();
        initWebService();
        initUI();
    }

    private void checkInternet() {
        if (!ApplicationUtility.checkInternet(context)) {
            //Open Network Settings
            ApplicationUtility.openNetworkDialog(this);
        }
    }

    private void initSharePref() {
        sharedPreferences = getSharedPreferences(StaticData.APP_PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isFirstTime = sharedPreferences.getBoolean(StaticData.FIRST_TIME, true);
    }

    private void initUI() {
        loginManager = LoginManager.getInstance();
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvRegister = (TextView) findViewById(R.id.tvRegistration);
        tvFacebook = (TextView) findViewById(R.id.tvFacebook);
        tvFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApplicationUtility.checkInternet(context)) {
                    loginByFacebook();
                } else {
                    ApplicationUtility.openNetworkDialog(WelcomeActivity.this);
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lIntent = new Intent(context, LoginActivity.class);
                startActivity(lIntent);
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lIntent = new Intent(context, RegistrationActivity.class);
                startActivity(lIntent);
            }
        });

        vpWelcome = (ViewPager) findViewById(R.id.viewPagerWelcome);
        circleIndicator = (CircleIndicator) findViewById(R.id.indicatorWelcome);

        vpWelcome.setAdapter(new WelcomePagerAdapter(getSupportFragmentManager(), getFragments()));
        circleIndicator.setViewPager(vpWelcome);

        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.GONE);
//        rlPagerContainer = (RelativeLayout) findViewById(R.id.rlPagerContainer);
//        initAccesssTokenTracker();
    }

    private void initWebService() {
        retrofit = new Retrofit.Builder()
                .baseUrl(StaticData.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(APIServiceInterface.class);
    }

    private List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new WelcomeScreen1());
        fragments.add(new WelcomeScreen2());
        return fragments;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //Integrating Facebook Sign In Starts >
    private void loginByFacebook() {
        rlProgress.setVisibility(View.GONE);
        callbackManager = CallbackManager.Factory.create();
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add("email");
        loginManager.logInWithReadPermissions(this, permissions);
        loginManager.registerCallback(callbackManager, facebookCallbackManager);

    }

    FacebookCallback<LoginResult> facebookCallbackManager = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            rlProgress.setVisibility(View.VISIBLE);
            accessToken = loginResult.getAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                    Log.d("FacebookData", jsonObject.toString());
                    userBundle = ApplicationUtility.getFacebookData(jsonObject, accessToken);
                    signUpUser(userBundle);
                }
            });
            requestToExecuteAPI(request);
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {

        }
    };

    private void requestToExecuteAPI(GraphRequest request) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, middle_name, last_name, email, gender, birthday, location"); // Facebook permission parameters
        request.setParameters(parameters);
        request.executeAsync();
    }
    // Facebook Signin ended

    private void signUpUser(Bundle bundleString) {
        String name = bundleString.getString(StaticData.USER_FIRST_NAME) + " " + bundleString.getString(StaticData.USER_LAST_NAME);
        String email = bundleString.getString(StaticData.USER_EMAIL);
        String tag = "register";
        String regId = ApplicationUtility.getRegId();
        Log.e("FacebookLogin",name+" "+email+" "+regId);
        Call<SignUpResponse> call = apiService.facebookSignUpUser(tag, name, email, regId);
        call.enqueue(signUpResponseCallback);
    }

    Callback<SignUpResponse> signUpResponseCallback = new Callback<SignUpResponse>() {
        @Override
        public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
            if (response.body().isError()) {
                Log.e("RESPONSE", response.body().getError_msg());
                processSignIn(response.body());
                rlProgress.setVisibility(View.GONE);
            } else {
                Log.e("RESPONSE", response.body().getError_msg());
            }
        }

        @Override
        public void onFailure(Call<SignUpResponse> call, Throwable t) {
            Toast.makeText(context, "Problem connecting server. Try again", Toast.LENGTH_SHORT).show();
            rlProgress.setVisibility(View.GONE);
        }
    };

    private void processSignIn(SignUpResponse response) {
        if (response.getError_msg().trim().equalsIgnoreCase(StaticData.SUCCESS_MSG.trim()) ||
                response.getError_msg().trim().equalsIgnoreCase(StaticData.EXIST_MSG.trim())) {
            saveUserSession();
            Intent hIntent = new Intent(context, HomeActivity.class);
            startActivity(hIntent);
            finish();
        } else {
            Toast.makeText(context, "Failed connecting to server. Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserSession() {
        editor.putString(StaticData.USER_ID, userBundle.getString(StaticData.USER_ID));
        editor.putString(StaticData.USER_FIRST_NAME, userBundle.getString(StaticData.USER_FIRST_NAME));
        editor.putString(StaticData.USER_LAST_NAME, userBundle.getString(StaticData.USER_LAST_NAME));
        editor.putString(StaticData.USER_EMAIL, userBundle.getString(StaticData.USER_EMAIL));
        editor.putBoolean(StaticData.SESSION, true);
        editor.putBoolean(StaticData.FIRST_TIME, false);
        editor.commit();
    }

}
