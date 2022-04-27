package zdmk.micro.notificationservice.components.senders;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import zdmk.micro.notificationservice.interfaces.NotificationSender;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class EmailNotificationSender implements NotificationSender {

    private final Logger logger;

    private final LinkedBlockingQueue<zdmk.micro.notificationservice.protos.NotificationData> queue;

    private volatile boolean stop;

    {
        logger = Logger.getLogger(EmailNotificationSender.class.getName());
        queue = new LinkedBlockingQueue<>();
        stop = false;
    }

    @Override
    public void run() {
        logger.info("Sender instance started");
        while (!stop) {
            zdmk.micro.notificationservice.protos.NotificationData task;
            try {
                task = queue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                break;
            }
            if (task == null) {
                continue;
            }
            JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
            zdmk.micro.notificationservice.protos.ConnectionInfo connectionInfo = task.getConnectionInfo();

            javaMailSender.setProtocol(connectionInfo.getProtocol());
            javaMailSender.setHost(connectionInfo.getAddress());
            javaMailSender.setPort(connectionInfo.getPort());
            javaMailSender.setUsername(connectionInfo.getLogin());
            javaMailSender.setPassword(connectionInfo.getPassword());

            Properties props = javaMailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", connectionInfo.getProtocol());
            if (connectionInfo.getProtocol().equals("smtp") || connectionInfo.getProtocol().equals("smtps")) {
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
            }
            props.put("mail.debug", "false");


            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setSubject(task.getSubject());
            simpleMailMessage.setText(task.getContent());
            simpleMailMessage.setFrom(connectionInfo.getLogin());

            task.getReceiverList().forEach(receiver -> {
                simpleMailMessage.setTo(receiver);
                javaMailSender.send(simpleMailMessage);
                logger.info(String.format("Sent message to %s from %s.", receiver, javaMailSender.getUsername()));
            });
        }
    }

    @Override
    public List<String> getAvailableProtocols() {
        return List.of("smtp", "smtps", "imap", "imaps");
    }

    @Override
    public void processMessage(zdmk.micro.notificationservice.protos.NotificationData mailData) {
        queue.offer(mailData);
    }

    @Override
    public void destroy() {
        logger.info(String.format("Stopping sender instance (%s)", this.getClass().getName()));
        stop = true;
    }
}