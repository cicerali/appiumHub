package tr.com.cicerali.appiumhub;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.cicerali.appiumhub.exception.HubRegisterException;
import tr.com.cicerali.appiumhub.exception.HubSessionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@ConditionalOnExpression("${appium-hub.enableController:true}")
@RestController
@RequestMapping("${appium-hub.pathPrefix:}")
public class HubController {

    private final HubCore hubCore;

    @Autowired
    public HubController(HubCore hubCore) {
        this.hubCore = hubCore;
    }

    /**
     * @param request node registration request
     * @throws HubRegisterException if the registration fail
     */
    @PostMapping("/grid/register")
    public void registration(@RequestBody @Valid RegistrationRequest request) throws HubRegisterException {
        hubCore.register(request);
    }

    /**
     * @param id node id
     * @return node status
     */
    @GetMapping("/grid/api/proxy")
    public Map<String, Object> proxyStatus(@RequestParam("id") String id) {
        return hubCore.getNodeStatus(id);
    }

    /**
     * @return all active sessions information
     */
    @GetMapping("/wd/hub/sessions")
    public SessionData sessions() {
        return hubCore.processGetSessions();
    }

    /**
     * @param request    session related request
     * @param sessionKey session id
     * @return session response from remote node
     * @throws HubSessionException if processing the request fail
     */
    @RequestMapping(value = "/wd/hub/session/{sessionKey}/**", method = {GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE})
    public ResponseEntity<byte[]> regular(HttpServletRequest request, @PathVariable("sessionKey") String sessionKey) throws HubSessionException {
        return hubCore.processRegularSession(request, sessionKey);
    }

    /**
     * @param request new session request
     * @return create new session response from remote
     * @throws HubSessionException  if fail to create new session
     * @throws InterruptedException if interrupted
     */
    @PostMapping("/wd/hub/session")
    public ResponseEntity<byte[]> start(HttpServletRequest request) throws HubSessionException, InterruptedException {
        return hubCore.processStartSession(request);
    }

    /**
     * @param request    delete session request
     * @param sessionKey session id
     * @return delete session response from remote
     * @throws HubSessionException if failed
     */
    @DeleteMapping("/wd/hub/session/{sessionKey}")
    public ResponseEntity<byte[]> delete(HttpServletRequest request, @PathVariable("sessionKey") String sessionKey) throws HubSessionException {
        return hubCore.processDeleteSession(request, sessionKey);
    }

    /**
     * appium desktop uses this endpoint in some conditions
     */
    @DeleteMapping("/wd/hub/session")
    public void delete() {
        /* make appium desktop happy */
    }
}
