package dk.magenta.datafordeler.core.user;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserSetup {
    @Bean
    @ConditionalOnMissingBean
    public DafoUserManager defaultDafoUserManager() {
        return new DafoUserManager();
    }
}
