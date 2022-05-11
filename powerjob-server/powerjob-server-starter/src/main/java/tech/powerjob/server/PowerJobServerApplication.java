package tech.powerjob.server;

import tech.powerjob.server.common.utils.PropertyUtils;
import tech.powerjob.server.remote.transport.starter.AkkaStarter;
import tech.powerjob.server.remote.transport.starter.SocketStarter;
import tech.powerjob.server.remote.transport.starter.VertXStarter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * powerjob-server entry
 *
 * @author tjq
 * @since 2020/3/29
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class PowerJobServerApplication {


    public static void main(String[] args) {

        pre();

        AkkaStarter.init();
        VertXStarter.init();
        SocketStarter.init();
        // Start SpringBoot application.
        try {
            SpringApplication.run(PowerJobServerApplication.class, args);
        } catch (Throwable t) {
            throw t;
        }
    }

    private static void pre() {
        PropertyUtils.initByConfig();
    }

}
