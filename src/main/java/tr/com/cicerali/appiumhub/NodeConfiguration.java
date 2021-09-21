package tr.com.cicerali.appiumhub;


import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.net.URL;

public class NodeConfiguration {

    @Nullable
    private String id;
    private Integer cleanUpCycle;
    private Integer timeout;

    @NotNull
    private URL url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCleanUpCycle() {
        return cleanUpCycle;
    }

    public void setCleanUpCycle(Integer cleanUpCycle) {
        this.cleanUpCycle = cleanUpCycle;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
