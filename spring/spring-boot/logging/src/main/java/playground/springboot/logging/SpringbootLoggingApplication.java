package playground.springboot.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SpringbootLoggingApplication implements CommandLineRunner {

    public static void main(String[] args) {

        SpringApplication.run(SpringbootLoggingApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("this is in info {}", "blue");
        log.debug("this is in debug {}", "red");
    }
}
