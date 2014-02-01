package org.jenkinsci.plugins.deploymentnotification;

/**
 * Record of the deployment.
 *
 * @author Kohsuke Kawaguchi
 */
public class ServerDeploymentRecord {
    private final String host;
    private final String env;
    private final long timestamp;


    public ServerDeploymentRecord(String host, String env) {
        this.timestamp = System.currentTimeMillis();
        this.host = host;
        this.env = env;
    }

    public String getHost() {
        return host;
    }

    public String getEnv() {
        return env;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
