package christian.online.prayer.responses;

/**
 * Created by ppobd_six on 5/17/2016.
 */
public class SignUpResponse extends CommonResponse {

    private String error_msg;

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }
}
