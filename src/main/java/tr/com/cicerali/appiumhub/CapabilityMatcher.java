package tr.com.cicerali.appiumhub;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.function.Function;

public interface CapabilityMatcher {

    /**
     * It is used for determine desired capabilities from new session request.
     * Default implementation, will check first JSONWP `desiredCapabilities`,
     * if it found then other capabilities `W3C` ignored. If `desiredCapabilities`
     * not exist, then w3C capabilities will be used, if `alwaysMatch` and `firstMatch`
     * found same time they will be merged and `alwaysMatch` capabilities can override
     * `firstMatch` capabilities
     *
     * @return capability extractor function
     */
    @NotNull
    default Function<String, Map<String, Object>> getCapabilityExtractor() {
        return new DefaultCapabilityExtractor();
    }

    /**
     * @param providedCapabilities node`s own capabilities
     * @param requestedCapability  capabilities requested by client
     * @return {@code true} if providedCapabilities matches requestedCapability
     */
    boolean matches(Map<String, Object> providedCapabilities, Map<String, Object> requestedCapability);
}
