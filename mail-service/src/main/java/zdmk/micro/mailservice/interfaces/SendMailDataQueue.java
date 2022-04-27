package zdmk.micro.mailservice.interfaces;

import zdmk.micro.mailservice.protos.MailData;

public interface SendMailDataQueue {
    void addTask(MailData mailData);
    MailData getTask();
}
