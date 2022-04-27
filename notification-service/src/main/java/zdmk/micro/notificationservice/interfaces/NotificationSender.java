package zdmk.micro.notificationservice.interfaces;

import org.springframework.beans.factory.DisposableBean;

import java.util.List;

public interface NotificationSender extends DisposableBean, Runnable {
    List<String> getAvailableProtocols();
    void processMessage(zdmk.micro.notificationservice.protos.NotificationData mailData);
}
