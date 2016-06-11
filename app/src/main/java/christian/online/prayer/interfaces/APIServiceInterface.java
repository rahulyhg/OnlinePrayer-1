package christian.online.prayer.interfaces;

import christian.online.prayer.responses.GetTimeResponse;
import christian.online.prayer.responses.LoginResponse;
import christian.online.prayer.responses.SignUpResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIServiceInterface {
    @FormUrlEncoded
    @POST("index.php")
    Call<SignUpResponse> signUpUser(@Field("tag") String tag, @Field("name") String name, @Field("email") String email,
                                    @Field("password") String password, @Field("regId") String regId);

    @FormUrlEncoded
    @POST("index.php")
    Call<SignUpResponse> facebookSignUpUser(@Field("tag") String tag, @Field("name") String name,
                                            @Field("email") String email, @Field("regId") String regId);

    @FormUrlEncoded
    @POST("index.php")
    Call<LoginResponse> loginUser(@Field("tag") String tag, @Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("index.php")
    Call<GetTimeResponse> getTime(@Field("tag") String tag);

}
