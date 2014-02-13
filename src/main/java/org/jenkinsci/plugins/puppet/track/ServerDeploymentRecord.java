package org.jenkinsci.plugins.puppet.track;

/**
 * Record of the deployment.
 *
 * @author Kohsuke Kawaguchi
 */
public class ServerDeploymentRecord {
    private final String host;
    private final String env;
    private final long timestamp;
    private final String path;


    public ServerDeploymentRecord(String host, String env, String path) {
        this.timestamp = System.currentTimeMillis();
        this.host = host;
        this.env = env;
        this.path = path;
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

    public String getPath() {
        return path;
    }
}
