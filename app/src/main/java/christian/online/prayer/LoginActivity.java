package christian.online.prayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

import christian.online.prayer.customcontrol.FloatingHintEditText;
import christian.online.prayer.data.StaticData;
import christian.online.prayer.entity.User;
import christian.online.prayer.interfaces.APIServiceInterface;
import christian.online.prayer.responses.LoginResponse;
import christian.online.prayer.responses.SignUpResponse;
import christian.online.prayer.utils.ApplicationUtility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    FloatingHintEditText fhetUserEmail, fhetPassword;
    TextView tvLogin, tvCreateAccount, tvLoginWithFacebook;
    RelativeLayout rlProgress;

    //Facebok
    CallbackManager callbackManager;
    LoginManager loginManager;
    //    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    Bundle userBundle;

    //Web Service
    Retrofit retrofit;
    APIServiceInterface apiService;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        initUI();
        addClickListeners();
        checkInternet();
        initSharePref();
        initWebService();

        FacebookSdk.sdkInitialize(context);
        loginManager = LoginManager.getInstance();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initUI(){
        fhetUserEmail = (FloatingHintEditText) findViewById(R.id.fhetUserEmail);
        fhetPassword = (FloatingHintEditText) findViewById(R.id.fhetPassword);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvCreateAccount = (TextView) findViewById(R.id.tvCreateAccount);
        tvLoginWithFacebook = (TextView) findViewById(R.id.tvLoginWithFacebook);
        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.GONE);
    }

    private void addClickListeners(){
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inAppLogin();
            }
        });

        tvLoginWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApplicationUtility.checkInternet(context)) {
                    loginWithFacebook();
                } else {
                    ApplicationUtility.openNetworkDialog(LoginActivity.this);
                }
            }
        });

        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegistrationPage();
            }
        });

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
    }

    private void initWebService() {
        retrofit = new Retrofit.Builder()
                .baseUrl(StaticData.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(APIServiceInterface.class);
    }

    private void inAppLogin(){
        if(ApplicationUtility.checkInternet(context)) {
            if (checkData()) {
                rlProgress.setVisibility(View.VISIBLE);
                loginUser();
            } else {
                Toast.makeText(context, "Need to set all information properly before login", Toast.LENGTH_SHORT).show();
            }
        }else{
            ApplicationUtility.openNetworkDialog(this);
        }
    }

    private boolean checkData(){
        String email = fhetUserEmail.getText().toString();
        String password = fhetPassword.getText().toString();
        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
            if(TextUtils.isEmpty(email)){
                fhetUserEmail.setError("Email is needed");
            }else if (TextUtils.isEmpty(password)){
                fhetPassword.setError("Password is needed");
            }
            return false;
        }else{
            return true;
        }

    }

    private void loginWithFacebook(){
        rlProgress.setVisibility(View.GONE);
        callbackManager = CallbackManager.Factory.create();
        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add("email");
        loginManager.logInWithReadPermissions(this, permissions);
        loginManager.registerCallback(callbackManager, facebookCallbackManager);
    }

    private void openRegistrationPage(){
        Intent rIntent = new Intent(context, RegistrationActivity.class);
        startActivity(rIntent);
    }

    // Associate methods for Facebook login
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
                    loginUserWithFacebook(userBundle);
                }
            });
            requestToExecuteAPI(request);
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {
            Log.e("FACEBOOK",e.getMessage());
        }
    };

    private void requestToExecuteAPI(GraphRequest request) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, middle_name, last_name, email, gender, birthday, location"); // Facebook permission parameters
        request.setParameters(parameters);
        request.executeAsync();
    }
    // Facebook Signin ended

    private void loginUserWithFacebook(Bundle bundleString) {
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

    //User login
    private void loginUser(){
        String email = fhetUserEmail.getText().toString();
        String password = fhetPassword.getText().toString();
        String tag = "login";
        Call<LoginResponse> call = apiService.loginUser(tag, email, password);
        call.enqueue(loginResponseCallback);
    }

    Callback<LoginResponse> loginResponseCallback = new Callback<LoginResponse>() {
        @Override
        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
            if(!response.body().isError()){
                rlProgress.setVisibility(View.GONE);
                saveUserData(response.body());
                Intent hIntent = new Intent(context, HomeActivity.class);
                startActivity(hIntent);
                finish();
            }else{
                Toast.makeText(context,"Couldn't connect to the server. Try again",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<LoginResponse> call, Throwable t) {

        }
    };

    private void saveUserData(LoginResponse loginResponse) {
        editor.putString(StaticData.USER_ID, loginResponse.getUid());
        editor.putString(StaticData.USER_NAME, loginResponse.getUser().getName());
        editor.putString(StaticData.USER_EMAIL, loginResponse.getUser().getEmail());
        editor.putBoolean(StaticData.SESSION, true);
        editor.commit();
    }

}
