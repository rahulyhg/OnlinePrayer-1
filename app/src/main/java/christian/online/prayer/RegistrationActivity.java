package christian.online.prayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import christian.online.prayer.customcontrol.FloatingHintEditText;
import christian.online.prayer.data.StaticData;
import christian.online.prayer.interfaces.APIServiceInterface;
import christian.online.prayer.responses.LoginResponse;
import christian.online.prayer.responses.SignUpResponse;
import christian.online.prayer.utils.ApplicationUtility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistrationActivity extends AppCompatActivity {

    FloatingHintEditText fhetRegUserName, fhetRegUserEmail, fhetRegPassword, fhetRegConfirmPassword;
    TextView tvCreateAccount, tvLogin;
    RelativeLayout rlProgress;

    //Web Service
    Retrofit retrofit;
    APIServiceInterface apiService;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String regId;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        context =this;

        initUI();
        addClickListeners();
        checkInternet();
        initSharePref();
        initWebService();
    }

    private void initUI(){
        fhetRegPassword = (FloatingHintEditText) findViewById(R.id.fhetRegPassword);
        fhetRegUserEmail = (FloatingHintEditText) findViewById(R.id.fhetRegUserEmail);
        fhetRegUserName = (FloatingHintEditText) findViewById(R.id.fhetRegUserName);
        fhetRegConfirmPassword = (FloatingHintEditText) findViewById(R.id.fhetRegConfirmPassword);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvCreateAccount = (TextView) findViewById(R.id.tvCreateAccount);
        rlProgress = (RelativeLayout) findViewById(R.id.rlProgress);
        rlProgress.setVisibility(View.GONE);
    }

    private void addClickListeners(){
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginPage();
            }
        });

        tvCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
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

    private void openLoginPage(){
        onBackPressed();
    }

    private void registerUser(){
        if(checkData()){
            rlProgress.setVisibility(View.VISIBLE);
            initRegistration();
        }else{
            Toast.makeText(context,"Please complete the form properly to create account",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkData(){
        if(TextUtils.isEmpty(fhetRegUserName.getText())){
            fhetRegUserName.setError("Name required");
            return false;
        }else if(TextUtils.isEmpty(fhetRegUserEmail.getText())){
            fhetRegUserEmail.setError("Email required");
            return false;
        }else if(TextUtils.isEmpty(fhetRegPassword.getText())){
            fhetRegPassword.setError("Password required");
            return false;
        }else if(TextUtils.isEmpty(fhetRegConfirmPassword.getText())){
            fhetRegConfirmPassword.setError("Type password again");
            return false;
        }else if(!fhetRegConfirmPassword.getText().toString().equalsIgnoreCase(fhetRegPassword.getText().toString())){
            fhetRegConfirmPassword.setError("Re-type the password");
            return false;
        }else{
            return true;
        }
    }

    private void initRegistration(){
        String name = fhetRegUserName.getText().toString();
        String email = fhetRegUserEmail.getText().toString();
        String password = fhetRegPassword.getText().toString();
        regId = Calendar.getInstance().getTimeInMillis()+"";
        String tag = "register";
        Call<SignUpResponse> call = apiService.signUpUser(tag, name, email, password, regId);
        call.enqueue(signUpResponseCallback);
    }

    Callback<SignUpResponse> signUpResponseCallback = new Callback<SignUpResponse>() {
        @Override
        public void onResponse(Call<SignUpResponse> call, Response<SignUpResponse> response) {
            if(response.body().isError()){
                processSignIn(response.body());
                rlProgress.setVisibility(View.GONE);
            }
        }

        @Override
        public void onFailure(Call<SignUpResponse> call, Throwable t) {

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
        editor.putString(StaticData.USER_ID, regId);
        editor.putString(StaticData.USER_NAME,fhetRegUserName.getText().toString());
        editor.putString(StaticData.USER_EMAIL, fhetRegUserEmail.getText().toString());
        editor.putBoolean(StaticData.SESSION, true);
        editor.putBoolean(StaticData.FIRST_TIME, false);
        editor.commit();
    }
}
