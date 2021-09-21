package tr.com.cicerali.appiumhub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collection;
import java.util.Map;

@TestConfiguration
class AppiumGridApplicationTests {

    @Test
    void contextLoads() throws JsonProcessingException {
        String test = "{\"desiredCapabilites\": {\"platformName\": \"iOS\", \"platformVersion\": \"13.3\", \"browserName\": \"Safari\", \"deviceName\": \"iPhone 11\"}}";
        String test2 = "{\"capabilities\": {\"alwaysMatch\": {\"platformName\": \"iOS\", \"platformVersion\": \"13.3\", \"browserName\": \"Safari\", \"deviceName\": \"iPhone 11\"}}}";
       String test23 = "{\"value\":{\"capabilities\":{\"webStorageEnabled\":false,\"locationContextEnabled\":false,\"browserName\":\"Safari\",\"platform\":\"MAC\",\"javascriptEnabled\":true,\"databaseEnabled\":false,\"takesScreenshot\":true,\"networkConnectionEnabled\":false,\"platformName\":\"iOS\",\"platformVersion\":\"13.3\",\"deviceName\":\"iPhone 11\",\"udid\":\"140472E9-8733-44FD-B8A1-CDCFF51BD071\"},\"sessionId\":\"ac3dbaf9-3b4e-43a2-9416-1a207cdf52da\"}}\n";
        Map<String, Object> result =
                new ObjectMapper().readValue(test, Map.class);
    }

}
