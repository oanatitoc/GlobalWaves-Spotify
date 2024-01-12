package app.user.Entities.Notifications;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public final class Notification {
    private final List<NotificationObserver> observers = new ArrayList<>();
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String description;

    public Notification(final String name, final String description) {
        this.name = name;
        this.description = description;
    }
    /**
     * Adds a NotificationObserver to the list of observers.
     *
     * @param observer The NotificationObserver to be added.
     */
    public void addObserver(final NotificationObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes a NotificationObserver from the list of observers.
     *
     * @param observer The NotificationObserver to be removed.
     */
    public void removeObserver(final NotificationObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all registered observers about a change in the subject.
     * Calls the update method on each observer, passing the current object as an argument.
     */
    public void notifyObservers() {
        for (NotificationObserver observer : observers) {
            observer.update(this);
        }
    }

}
