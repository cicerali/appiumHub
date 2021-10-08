package tr.com.cicerali.appiumhub;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableConfigurationProperties(HubProperties.class)
public class HubAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HubConfig hubConfig(HubProperties hubProperties) {
        return HubConfig.HubConfigBuilder.fromHubProperties(hubProperties).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public HubCore hubCore(HubConfig hubConfig) {
        return new HubCore(hubConfig);
    }
}
