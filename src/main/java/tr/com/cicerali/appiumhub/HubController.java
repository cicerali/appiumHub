package tr.com.cicerali.appiumhub;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;


@RestController
@RequestMapping()
public class HubController {

    private HubCore hubCore;

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

    @RequestMapping(value = "/wd/hub/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<?> webDriver(HttpServletRequest request) throws HubSessionException, InterruptedException {
        return hubCore.process(request);
    }
}
