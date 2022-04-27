package zdmk.micro.notificationservice.components;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SenderThreadPool {

    @Getter
    private final ExecutorService pool = Executors.newFixedThreadPool(24);

    @PreDestroy
    private void destroy() {
        pool.shutdown();
    }
}
