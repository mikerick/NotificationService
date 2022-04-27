package zdmk.micro.mailservice.components;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zdmk.micro.mailservice.interfaces.NotificationSender;
import zdmk.micro.mailservice.interfaces.SendMailDataQueue;

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
    private SendMailDataQueue queue;

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
    public void setQueue(SendMailDataQueue queue) {
        this.queue = queue;
    }

    private void processNotifications() {
        while (sendNotifications) {
            zdmk.micro.mailservice.protos.MailData task = queue.getTask();
            if(task == null) continue;
            String messageProcessorBeanName = protocolProcessors.get(task.getConnectionInfo().getProtocol());
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
        senders.values().forEach(v -> { try { v.destroy(); } catch (Exception ignored) {}});
    }

    @Override
    public void run() {
        logger.info("Notification processor started.");
        processNotifications();
    }

}
