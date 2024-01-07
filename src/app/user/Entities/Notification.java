package app.user.Entities;

import java.util.ArrayList;
import java.util.List;

public final class Notification {
    private final List<NotificationObserver> observers = new ArrayList<>();
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
    public Notification(final String name, final String description) {
        this.name = name;
        this.description = description;
    }
    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (NotificationObserver observer : observers) {
            observer.update(this);
        }
    }
}
