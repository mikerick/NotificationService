package zdmk.micro.notificationservice.components.queues;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import zdmk.micro.notificationservice.interfaces.SendNotificationDataQueue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Qualifier("sendMailDataInMemoryQueue")
public class SendMailDataInMemoryQueue implements SendNotificationDataQueue {
    private final LinkedBlockingQueue<zdmk.micro.notificationservice.protos.NotificationData> mailToSend;

    {
        mailToSend = new LinkedBlockingQueue<>(256);
    }

    @Override
    public void addTask(zdmk.micro.notificationservice.protos.NotificationData mailData) {
        mailToSend.offer(mailData);
    }

    @Override
    public zdmk.micro.notificationservice.protos.NotificationData getTask() {
        try {
            return mailToSend.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {return null;}
    }
}
