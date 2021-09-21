package tr.com.cicerali.appiumhub;

import java.util.Map;

public interface CapabilityMatcher {
    boolean matches(Map<String, Object> currentCapability, Map<String, Object> requestedCapability);
}
