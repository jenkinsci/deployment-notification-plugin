package org.jenkinsci.plugins.deployment;

import hudson.Util;

/**
 * Record of the deployment.
 *
 * @author Kohsuke Kawaguchi
 */
public class HostRecord {
    private final String host;
    private final String env;
    private final long timestamp;
    private final String path;


    public HostRecord(String host, String env, String path) {
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

    /**
     * Gets the string that says how long since this build has scheduled.
     *
     * @return
     *      string like "3 minutes" "1 day" etc.
     */
    public String getTimestampString() {
        long duration = System.currentTimeMillis()-timestamp;
        return Util.getPastTimeString(duration);
    }
}
