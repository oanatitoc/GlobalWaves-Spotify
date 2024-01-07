package app.user.Entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public class NotificationListManager implements NotificationObserver {
    private final List<Notification> notifications = new ArrayList<>();
    public void update(Notification notification) {
        notifications.add(notification);
    }
    public ArrayNode displayNotifications() {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (Notification notification : notifications) {
            ObjectNode notificationNode = objectMapper.createObjectNode();
            notificationNode.put("name", notification.getName());
            notificationNode.put("description", notification.getDescription());
            arrayNode.add(notificationNode);
        }

        notifications.clear();
        return arrayNode;
    }
}