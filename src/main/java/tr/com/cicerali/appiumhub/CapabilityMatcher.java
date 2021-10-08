package tr.com.cicerali.appiumhub;

import java.util.Map;

public interface CapabilityMatcher {

    /**
     * @param providedCapabilities node`s own capabilities
     * @param requestedCapability  capabilities requested by client
     * @return {@code true} if providedCapabilities matches requestedCapability
     */
    boolean matches(Map<String, Object> providedCapabilities, Map<String, Object> requestedCapability);
}
