package zdmk.micro.mailservice.components.queues;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import zdmk.micro.mailservice.interfaces.SendMailDataQueue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Qualifier("sendMailDataInMemoryQueue")
public class SendMailDataInMemoryQueue implements SendMailDataQueue {
    private final LinkedBlockingQueue<zdmk.micro.mailservice.protos.MailData> mailToSend;

    {
        mailToSend = new LinkedBlockingQueue<>(256);
    }

    @Override
    public void addTask(zdmk.micro.mailservice.protos.MailData mailData) {
        mailToSend.offer(mailData);
    }

    @Override
    public zdmk.micro.mailservice.protos.MailData getTask() {
        try {
            return mailToSend.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {return null;}
    }
}
