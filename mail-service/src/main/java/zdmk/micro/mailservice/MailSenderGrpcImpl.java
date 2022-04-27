package zdmk.micro.mailservice;

import com.google.protobuf.ProtocolStringList;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import zdmk.micro.mailservice.interfaces.GRPCServiceBean;
import zdmk.micro.mailservice.interfaces.SendMailDataQueue;
import zdmk.micro.mailservice.protos.ConnectionInfo;
import zdmk.micro.mailservice.protos.SendEventInfo;

import java.lang.reflect.Field;
import java.util.logging.Logger;

@Component
public class MailSenderGrpcImpl
        extends zdmk.micro.mailservice.protos.MailSenderGrpc.MailSenderImplBase
        implements GRPCServiceBean {

    Logger logger;

    {
        logger = Logger.getLogger(this.getClass().getName());
    }

    private SendMailDataQueue queue;

    private final zdmk.micro.mailservice.protos.ConnectionInfo defaultConnectionInfo;

    public MailSenderGrpcImpl(@Qualifier("defaultEmailConnectionInfo") ConnectionInfo defaultConnectionInfo) {
        this.defaultConnectionInfo = defaultConnectionInfo;
    }

    @Autowired
    private void setQueue(SendMailDataQueue queue) {
        this.queue = queue;
    }

    private boolean isMailDataInitializedCorrectly(zdmk.micro.mailservice.protos.MailData mailData) {
        // if the encoded message __does not contain__ a particular singular element,
        // the corresponding field in the parsed object **is set to the default value** for that field
        // Boolean checking way

        ProtocolStringList receiverList = mailData.getReceiverList();

        String content = mailData.getContent();
        if (content.equals("") || receiverList.isEmpty()) return false;

        boolean patchConnectionInfo = false;

        if (!mailData.hasConnectionInfo()) {
            Field connectionInfoField;
            try {
                connectionInfoField = mailData.getClass().getDeclaredField("connectionInfo_");
                connectionInfoField.setAccessible(true);
                ReflectionUtils.setField(connectionInfoField, mailData, defaultConnectionInfo);
            } catch (NoSuchFieldException ignored) {
                logger.info("Default connection info data has NOT been injected as field is missing.");
            }
        }
        return true;

    }

    @Override
    public void sendMail(zdmk.micro.mailservice.protos.MailData request,
             StreamObserver<zdmk.micro.mailservice.protos.SendEventInfo> responseObserver) {

        queue.addTask(request);

        SendEventInfo.Builder builder = SendEventInfo
                .newBuilder()
                .setMessage("The operation has been added to the sending queue")
                .setStatus(200);

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
