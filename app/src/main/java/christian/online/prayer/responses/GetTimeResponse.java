package christian.online.prayer.responses;

/**
 * Created by ppobd_six on 5/25/2016.
 */
public class GetTimeResponse extends CommonResponse {
    private String curr_day;
    private String curr_time;
    private String next_day;
    private String next_time;

    public String getCurr_day() {
        return curr_day;
    }

    public void setCurr_day(String curr_day) {
        this.curr_day = curr_day;
    }

    public String getCurr_time() {
        return curr_time;
    }

    public void setCurr_time(String curr_time) {
        this.curr_time = curr_time;
    }

    public String getNext_day() {
        return next_day;
    }

    public void setNext_day(String next_day) {
        this.next_day = next_day;
    }

    public String getNext_time() {
        return next_time;
    }

    public void setNext_time(String next_time) {
        this.next_time = next_time;
    }
}
