package zdmk.micro.mailservice.interfaces;

import org.springframework.beans.factory.DisposableBean;

import java.util.List;

public interface NotificationSender extends DisposableBean, Runnable {
    List<String> getAvailableProtocols();
    void processMessage(zdmk.micro.mailservice.protos.MailData mailData);
}
