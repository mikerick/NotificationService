package zdmk.micro.mailservice.components;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zdmk.micro.mailservice.interfaces.GRPCServiceBean;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class NotificationCollectorServer implements DisposableBean, Runnable {
    private final Logger logger = Logger.getLogger(NotificationCollectorServer.class.getName());
    private Server server;

    private final List<GRPCServiceBean> serviceInstances;

    @Autowired
    private NotificationCollectorServer(List<GRPCServiceBean> serviceInstances) {
        this.serviceInstances = serviceInstances;
    }

    public void initServer() throws IOException {
        logger.info("Server starting...");
        int zdmkMsServerPort;
        try {
            zdmkMsServerPort = Integer.parseInt(System.getenv("ZDMK_MS_SERVER_PORT"));
        } catch (NumberFormatException e) {
            zdmkMsServerPort = 32000;
        }
        ServerBuilder<?> serverBuilder = ServerBuilder
                .forPort(zdmkMsServerPort);
        this.serviceInstances.forEach(service -> serverBuilder.addService((BindableService) service));
        server = serverBuilder.build();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                NotificationCollectorServer.this.stop();
            } catch (InterruptedException e) {
                logger.info("Error during stopping");
            }
        }));
        logger.info("Server started. Port: " + zdmkMsServerPort);
    }

    public void stop() throws InterruptedException {
        logger.info("Server stopping... 5sec");
        server.awaitTermination(5, TimeUnit.SECONDS);
        logger.info("Server stopped.");
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public void destroy() {
        logger.info("Notification processor stopped.");
        if(this.server != null)
            this.server.shutdown();
    }

    @Override
    public void run() {
        logger.info("Notification collector started.");
        try {
            this.initServer();
            this.blockUntilShutdown();  // should be executed in a separate thread
        } catch (IOException | InterruptedException e) {
            logger.info("Cannot start the notification collector server: " + e.getMessage());
        }
    }

    @PostConstruct
    private void init() {
        ExecutorService tpe = Executors.newSingleThreadExecutor();
        tpe.submit(this);
    }

}
