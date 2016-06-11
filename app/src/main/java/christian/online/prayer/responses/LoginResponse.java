package christian.online.prayer.responses;

import christian.online.prayer.entity.User;

/**
 * Created by ppobd_six on 5/22/2016.
 */
public class LoginResponse extends CommonResponse {
    private String uid;
    private User user;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
