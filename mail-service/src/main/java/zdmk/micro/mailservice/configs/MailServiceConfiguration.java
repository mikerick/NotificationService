package zdmk.micro.mailservice.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.logging.Logger;

@Configuration
public class MailServiceConfiguration {

    @Bean
    public zdmk.micro.mailservice.protos.ConnectionInfo defaultEmailConnectionInfo() {
        Logger logger = Logger.getLogger(this.getClass().getName());

        Map<String, String> env = System.getenv();
        String defaultMail = env.getOrDefault("ZDMK_MS_DEFAULT_EMAIL", "joe@example.com");
        String defaultPassword = env.getOrDefault("ZDMK_MS_DEFAULT_PASSWORD", "Joe'sPassword");
        String defaultProtocol = env.getOrDefault("ZDMK_MS_DEFAULT_PROTOCOL", "smtps");
        int defaultPort = 465;
        try {
            defaultPort = Integer.parseInt(env.getOrDefault("ZDMK_MS_DEFAULT_PORT", "465"));  // String as its value in map
        } catch (NumberFormatException ignored) {}
        String defaultAddress = env.getOrDefault("ZDMK_MS_DEFAULT_ADDRESS", "localhost");  // To not connect anywhere accidentally

        zdmk.micro.mailservice.protos.ConnectionInfo info =
                zdmk.micro.mailservice.protos.ConnectionInfo
                        .newBuilder()
                        .setProtocol(defaultProtocol)
                        .setPort(defaultPort)
                        .setAddress(defaultAddress)
                        .setLogin(defaultMail)
                        .setPassword(defaultPassword)
                        .build();
        logger.info("Default mail sender set as " + info.getLogin());
        return info;
    }


}
