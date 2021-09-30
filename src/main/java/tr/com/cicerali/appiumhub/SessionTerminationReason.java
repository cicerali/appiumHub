package tr.com.cicerali.appiumhub;

public enum SessionTerminationReason {
    /**
     * triggered for client inactivity timout
     */
    TIMEOUT,
    /**
     * triggered by client for stop the session
     */
    CLIENT_STOPPED_SESSION,
    /**
     * triggered by an exception during forward request to the node
     */
    FORWARDING_TO_NODE_FAILED,
    /**
     * triggered for exception during new session create
     */
    CREATION_FAILED,
    /**
     * triggered by re-registration the node
     */
    NODE_REREGISTRATION,
    /**
     * triggered by node unreachable
     */
    NODE_UNREACHABLE,
    /**
     * triggered for socket timeout
     */
    SO_TIMEOUT
}