package christian.online.prayer.responses;

/**
 * Created by ppobd_six on 5/18/2016.
 */
public class CommonResponse {
    private String tag;
    private boolean error;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
