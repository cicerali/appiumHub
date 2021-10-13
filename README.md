# Appium Hub

Appium Hub is an alternative to Selenium Grid. Designed to work together with spring boot, also it can be used without
spring boot.

It uses same configuration parameters with selenium grid for reduce complexity and facilitate the migration.

## Configuration

With spring boot, application properties can be used for configuration. With default configuration it acts as selenium
grid. More info: [Using Selenium Grid with Appium](http://appium.io/docs/en/advanced-concepts/grid/)

All configuration parameters grouped under `appium-hub`

* `path-prefix` { String } : Path prefix for hub endpoints. Default: "/"
* `keepAuthorizationHeaders` { Boolean } true or false: If true, hub will keep client's authorization headers. Default:
  false
* `stopOnProxyError` { Boolean } true or false : If true, hub will terminate session if any proxy error occurs. If set
  to false, the session will remain open until it is stopped by the client, or it times out. Default: true
* `browserTimeout` { Integer }: same as selenium grid. Default: 0
* `newSessionWaitTimeout` { Integer }: same as selenium grid. Default: 60000
* `cleanUpCycle` { Integer }: same as selenium grid. Default: 5000
* `timeout` { Integer }: same as selenium grid. Default: 300
* `nodePolling` { Integer }: same as selenium grid. Default: 5000
* `downPollingLimit` { Integer }: same as selenium grid. Default: 2
* `unregisterIfStillDownAfter` { Integer }: same as selenium grid. Default: 60000
* `nodeStatusCheckTimeout` { Integer }: same as selenium grid. Default: 5000
* `throwOnCapabilityNotPresent` { Boolean} true or false : If true, the hub will reject all test requests.If no
  compatible proxy is currently registered. If set to false, the request will queue until a node supporting the
  capability is registered with the grid. Default: true
* `enableController` {Boolean} true or false : If true, the hub controller will be enabled. Default: true
### Example config
```shell
appium-hub.path-prefix=/api
appium-hub.keep-authorization-headers=true
appium-hub.stop-on-proxy-error=false
appium-hub.browser-timeout=0
appium-hub.new-session-wait-timeout=60000
appium-hub.clean-up-cycle=5000
appium-hub.timeout=1800
appium-hub.node-polling=5000
appium-hub.down-polling-limit=2
appium-hub.unregister-if-still-down-after=60000
appium-hub.node-status-check-timeout=5000
appium-hub.throw-on-capability-not-present=true
appium-hub.enable-controller=true
```

## Advanced Configuration

For advance configuration, `HubConfig` bean can be created manually. For this purpose there is a builder class named
as `HubConfigBuilder`
Advanced configuration parameters are:

* `capabilityMatcher` used for matching new session request to remote appium nodes.
* `httpRequestInterceptors` used for manipulating request which forwarded to remote appium node. For example, it can be
  used for header manipulation especially authorization headers
* `testSessionInterceptors` used for interaction with session events start and terminate.

### capabilityMatcher

There are two methods `getCapabilityExtractor` and `matches`

* `getCapabilityExtractor` it can be used for manipulating new session requested capabilities.
* `matches` used for match requested capabilities with remote appium nodes

It also has a default implementation `DefaultCapabilityMatcher`

### httpRequestInterceptors
It is a `ClientHttpRequestInterceptor` list. Used for manipulation session request which forwarded to remote appium node.\
Working same as `RestTemplate` because, requests forwarded via `RestTemplate`

### testSessionInterceptors
It is a `TestSessionInterceptor` list. Used for interact some session events like `start` and `terminate`\
There are two methods `afterTestSessionTerminate` and `beforeTestSessionStart`

* `afterTestSessionTerminate` called after test session termination.
* `beforeTestSessionStart` called before forwarding the request to the remote node


# LICENSE

Source code is made available under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.txt).
