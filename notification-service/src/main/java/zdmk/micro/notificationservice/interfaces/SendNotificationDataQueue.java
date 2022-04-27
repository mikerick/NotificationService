package zdmk.micro.notificationservice.interfaces;

public interface SendNotificationDataQueue {
    void addTask(zdmk.micro.notificationservice.protos.NotificationData mailData);
    zdmk.micro.notificationservice.protos.NotificationData getTask();
}
