package tr.com.cicerali.appiumhub;

import java.util.Map;

public interface CapabilityMatcher {
    boolean matches(Map<String, Object> providedCapabilities, Map<String, Object> requestedCapability);
}
