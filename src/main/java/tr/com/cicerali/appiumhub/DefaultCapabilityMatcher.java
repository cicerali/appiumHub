package tr.com.cicerali.appiumhub;

import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class DefaultCapabilityMatcher implements CapabilityMatcher {

    interface Validator extends BiFunction<Map<String, Object>, Map<String, Object>, Boolean> {
    }

    private final List<Validator> validators = new ArrayList<>();

    private boolean anything(Object requested) {
        return requested == null ||
                ImmutableSet.of("any", "", "*").contains(requested.toString().toLowerCase());
    }

    DefaultCapabilityMatcher(boolean addDefault) {
        if (!addDefault) {
            return;
        }
        validators.add(new AliasedStringPropertyValidator(true, "platformName", "platform"));
        validators.add(new AliasedPropertyValidator("platformVersion", "version"));
        validators.add(new SimplePropertyValidator("udid"));
    }

    @Override
    public boolean matches(Map<String, Object> providedCapabilities, Map<String, Object> requestedCapabilities) {
        return providedCapabilities != null && requestedCapabilities != null
                && validators.stream().allMatch(v -> v.apply(providedCapabilities, requestedCapabilities));
    }

    public void addValidator(Validator validator) {
        validators.add(validator);
    }

    /* compare properties as object
     * can compare multiple properties
     */
    public class SimplePropertyValidator implements Validator {
        private final List<String> toConsider;

        SimplePropertyValidator(String... toConsider) {
            this.toConsider = Arrays.asList(toConsider);
        }

        @Override
        public Boolean apply(Map<String, Object> providedCapabilities, Map<String, Object> requestedCapabilities) {
            return requestedCapabilities.entrySet().stream()
                    .filter(entry -> toConsider.contains(entry.getKey()))
                    .filter(entry -> !anything(entry.getValue()))
                    .allMatch(entry -> entry.getValue().equals(providedCapabilities.get(entry.getKey())));
        }
    }

    /* compare properties as object
     * Properties can be defined with aliases
     */
    public class AliasedPropertyValidator implements Validator {
        private final String[] propertyAliases;

        AliasedPropertyValidator(String... propertyAliases) {
            this.propertyAliases = propertyAliases;
        }

        @Override
        public Boolean apply(Map<String, Object> providedCapabilities, Map<String, Object> requestedCapabilities) {
            Object requested = Stream.of(propertyAliases)
                    .map(requestedCapabilities::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            if (anything(requested)) {
                return true;
            }

            Object provided = Stream.of(propertyAliases)
                    .map(providedCapabilities::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            return Objects.equals(requested, provided);
        }
    }

    /* compare properties as string
     * Properties can be defined with aliases
     */
    public class AliasedStringPropertyValidator implements Validator {

        private final boolean ignoreCase;
        private final String[] propertyAliases;

        public AliasedStringPropertyValidator(boolean ignoreCase, String... propertyAliases) {
            this.ignoreCase = ignoreCase;
            this.propertyAliases = propertyAliases;
        }

        @Override
        public Boolean apply(Map<String, Object> providedCapabilities, Map<String, Object> requestedCapabilities) {

            Optional<Object> requested = Stream.of(propertyAliases)
                    .map(requestedCapabilities::get)
                    .filter(Objects::nonNull)
                    .findFirst();

            if (anything(requested.orElse(null))) {
                return true;
            }

            Optional<Object> provided = Stream.of(propertyAliases)
                    .map(providedCapabilities::get)
                    .filter(Objects::nonNull)
                    .findFirst();

            return provided.map(o -> {
                String p = o.toString();
                String r = requested.get().toString();
                return ignoreCase ? p.equalsIgnoreCase(r) : p.equals(r);
            }).orElse(false);
        }
    }
}
