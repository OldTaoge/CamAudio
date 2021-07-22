package space.oldtaoge.audioserver.entity;

import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class PlayerEntity {
    private static final PlayerEntity instance = new PlayerEntity();
    private Map<String, Client> CliRegister;

    @Data
    public static class Client {
        private boolean areConnect = false;
        private LocalDateTime lastKA;
    }

    @Scheduled(cron = "*/5 * * * * ?")
    public static void checkOnline()
    {
//        System.out.println("TimeTask");
        LocalDateTime ndt = LocalDateTime.now();
        PlayerEntity.getInstance().getCliRegister().forEach((k, v) ->
        {
            if (v.lastKA == null) {
                v.areConnect = false;
            }
            else {
              v.areConnect = v.lastKA.until(ndt, ChronoUnit.SECONDS) < 60;
//                v.areConnect = Duration.between(ndt, v.lastKA).toSeconds() < 60;
            }
        });
    }

    public static PlayerEntity getInstance() {
        if (instance.getCliRegister() == null) {
            var tmpM = new HashMap<String, Client>();
            tmpM.put("bd7dcd8c-c000-4967-bede-9fdd42e60cba", new Client());
            instance.setCliRegister(tmpM);
        }
        return instance;
    }

}
