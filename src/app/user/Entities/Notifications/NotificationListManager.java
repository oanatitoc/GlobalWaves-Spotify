package app.user.Entities.Notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public final class NotificationListManager implements NotificationObserver {
    private final List<Notification> notifications = new ArrayList<>();

    /**
     * Implements the update method as part of the Observer design pattern.
     * Adds the provided notification to the collection of notifications.
     *
     * @param notification The notification to be added to the collection.
     */
    public void update(final Notification notification) {
        notifications.add(notification);
    }


    /**
     * Creates and returns a JSON array node containing the details of notifications.
     * Each notification is represented by a JSON object with "name" and "description" fields.
     * After displaying the notifications, the internal notifications collection is cleared.
     *
     * @return the arrayNode.
     */
    public ArrayNode displayNotifications() {
        // Create an ObjectMapper to handle JSON processing.
        ObjectMapper objectMapper = new ObjectMapper();

        // Create an ArrayNode to store JSON objects representing notifications.
        ArrayNode arrayNode = objectMapper.createArrayNode();

        // Iterate through each notification and add it to the ArrayNode.
        for (Notification notification : notifications) {
            // Create a JSON object node for the current notification.
            ObjectNode notificationNode = objectMapper.createObjectNode();

            // Add "name" and "description" fields to the JSON object.
            notificationNode.put("name", notification.getName());
            notificationNode.put("description", notification.getDescription());

            // Add the JSON object to the ArrayNode.
            arrayNode.add(notificationNode);
        }

        // Clear the internal notifications collection after displaying them.
        notifications.clear();

        // Return the ArrayNode containing JSON representations of notifications.
        return arrayNode;
    }

}
