package tr.com.cicerali.appiumhub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableConfigurationProperties(HubProperties.class)
public class HubAutoConfiguration {

    @Autowired
    private HubProperties hubProperties;

    @Bean
    @ConditionalOnMissingBean
    public HubConfig hubConfig() {
        return new HubConfig(hubProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public HubCore hubCore(HubConfig hubConfig) {
        return new HubCore(hubConfig);
    }
}
