package tr.com.cicerali.appiumhub;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.cicerali.appiumhub.exception.HubRegisterException;
import tr.com.cicerali.appiumhub.exception.HubSessionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;


@RestController
@RequestMapping("${appium-hub.pathPrefix:}")
public class HubController {

    private final HubCore hubCore;

    @Autowired
    public HubController(HubCore hubCore) {
        this.hubCore = hubCore;
    }

    @PostMapping("/grid/register")
    public void registration(@RequestBody @Valid RegistrationRequest request) throws HubRegisterException {
        hubCore.register(request);
    }

    @GetMapping("/grid/api/proxy")
    public Map<String, Object> proxyStatus(@RequestParam("id") String id) {
        return hubCore.getNodeStatus(id);
    }

    @GetMapping("/wd/hub/sessions")
    public SessionData sessions() {
        return hubCore.processGetSessions();
    }

    @RequestMapping(value = "/wd/hub/session/{sessionKey}/**", method = {GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE})
    public ResponseEntity<byte[]> regular(HttpServletRequest request, @PathVariable("sessionKey") String sessionKey) throws HubSessionException {
        return hubCore.processRegularSession(request, sessionKey);
    }

    @PostMapping("/wd/hub/session")
    public ResponseEntity<byte[]> start(HttpServletRequest request) throws HubSessionException, InterruptedException {
        return hubCore.processStartSession(request);
    }

    @DeleteMapping("/wd/hub/session/{sessionKey}")
    public ResponseEntity<byte[]> delete(HttpServletRequest request, @PathVariable("sessionKey") String sessionKey) throws HubSessionException {
        return hubCore.processDeleteSession(request, sessionKey);
    }

    @DeleteMapping("/wd/hub/session")
    public void delete() {
        /* make appium desktop happy */
    }
}
