package tr.com.cicerali.appiumhub;


import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class RegistrationRequest {

    @NotNull
    private List <Map<String, Object>> capabilities;

    @NotNull
    private NodeConfiguration configuration;

    public List<Map<String, Object>> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Map<String, Object>> capabilities) {
        this.capabilities = capabilities;
    }

    public NodeConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(NodeConfiguration configuration) {
        this.configuration = configuration;
    }
}
