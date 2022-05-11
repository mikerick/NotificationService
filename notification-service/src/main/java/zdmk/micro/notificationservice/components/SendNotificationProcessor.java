package zdmk.micro.notificationservice.components;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zdmk.micro.notificationservice.interfaces.NotificationSender;
import zdmk.micro.notificationservice.interfaces.SendNotificationDataQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Component
public class SendNotificationProcessor implements Runnable, DisposableBean {
    private static Logger logger;

    private volatile boolean sendNotifications;

    private Map<String, NotificationSender> senders;

    private final Map<String, String> protocolProcessors;
    private SenderThreadPool pool;
    private SendNotificationDataQueue queue;

    {
        logger = Logger.getLogger(SendNotificationProcessor.class.getName());
        sendNotifications = true;
        protocolProcessors = new HashMap<>();
    }

    @Autowired
    private void setSenders(Map<String, NotificationSender> senders) {
        this.senders = senders;
    }

    @Autowired
    private void setPool(SenderThreadPool pool) {
        this.pool = pool;
    }

    @Autowired
    public void setQueue(SendNotificationDataQueue queue) {
        this.queue = queue;
    }

    private void processNotifications() {
        while (sendNotifications) {
            zdmk.micro.notificationservice.protos.NotificationData task = queue.getTask();
            if (task == null) continue;
            String messageProcessorBeanName = protocolProcessors.get(task.getConnectionInfo().getProtocol());
            if(messageProcessorBeanName == null) {
                logger.info("No such protocol: " + task.getConnectionInfo().getProtocol());
                continue;
            }
            senders.get(messageProcessorBeanName).processMessage(task);
        }
    }

    @PostConstruct
    private void init() {
        senders.forEach((name, notificationSender) ->
                notificationSender.getAvailableProtocols().forEach(protocol ->
                        protocolProcessors.put(protocol, name)));

        senders.values().forEach(v -> pool.getPool().submit(v));

        ExecutorService ste = Executors.newSingleThreadExecutor();
        ste.submit(this);
    }

    @PreDestroy
    @Override
    public void destroy() {
        logger.info("Notification processor stopped.");
        this.sendNotifications = false;
        senders.values().forEach(v -> {
            try {
                v.destroy();
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void run() {
        logger.info("Notification processor started.");
        processNotifications();
    }

}
