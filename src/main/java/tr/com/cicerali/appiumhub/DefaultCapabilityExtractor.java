package tr.com.cicerali.appiumhub;

import org.json.JSONObject;
import tr.com.cicerali.appiumhub.exception.UncheckedHubException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DefaultCapabilityExtractor implements Function<String, Map<String, Object>> {

    @Override
    public Map<String, Object> apply(String s) {

        /* check JSONWP
         * requiredCapabilities deprecated and removed selenium source code
         * with 3.7.0 788936fa0597d0755185fa89566381d9555bf5a4 */

        JSONObject root = new JSONObject(s);
        if (root.has("desiredCapabilities")) {
            return root.getJSONObject("desiredCapabilities").toMap();
        }

        /* check W3C */
        if (root.has("capabilities")) {
            root = root.getJSONObject("capabilities");
            JSONObject aMatch = root.optJSONObject("alwaysMatch");
            JSONObject fMatch = root.optJSONObject("firstMatch");
            Map<String, Object> match;
            if (aMatch != null || fMatch != null) {
                match = new HashMap<>();
                if (fMatch != null) {
                    match.putAll(fMatch.toMap());
                }
                if (aMatch != null) {
                    match.putAll(aMatch.toMap());
                }
                return match;
            }
        }
        throw new UncheckedHubException("Could not found desired capabilities in request");
    }
}
