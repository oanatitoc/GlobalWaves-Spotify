package app.user;

import java.util.ArrayList;
import java.util.List;

public final class Subscribe {
    private List<String> names; // the names of the artists or hosts of which one user has subscribed
    private List<Notification> notifications; // the notifications list
    public Subscribe() {
        names = new ArrayList<>();
        notifications = new ArrayList<>();
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
}
