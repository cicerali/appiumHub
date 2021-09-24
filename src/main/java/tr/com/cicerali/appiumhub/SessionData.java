package tr.com.cicerali.appiumhub;

import java.util.List;
import java.util.Map;

public class SessionData {
    private List<Map<String, Object>> value;
    private int status;

    public SessionData(List<Map<String, Object>> value, int status) {
        this.value = value;
        this.status = status;
    }

    public List<Map<String, Object>> getValue() {
        return value;
    }

    public void setValue(List<Map<String, Object>> value) {
        this.value = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
