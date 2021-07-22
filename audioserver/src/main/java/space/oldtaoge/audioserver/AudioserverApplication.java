package space.oldtaoge.audioserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AudioserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioserverApplication.class, args);
    }

}
