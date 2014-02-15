package org.jenkinsci.plugins.deployment;

import hudson.Util;
import hudson.model.Fingerprint;
import jenkins.model.Jenkins;

import java.io.IOException;

/**
 * Record of the deployment per host.
 *
 * @author Kohsuke Kawaguchi
 */
public class HostRecord {
    private final String host;
    private final String env;
    private final long timestamp;
    private final String path;
    /**
     * Fingerprint of the previous file that this deployment has replaced.
     * Can be null.
     */
    private final String replaces;


    public HostRecord(String host, String env, String path, String replaces) {
        this.timestamp = System.currentTimeMillis();
        this.host = host;
        this.env = env;
        this.path = path;
        this.replaces = replaces;
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

    public String getReplaces() {
        return replaces;
    }

    public Fingerprint getReplacesFingerprint() throws IOException {
        return replaces!=null ? Jenkins.getInstance().getFingerprintMap().get(replaces) : null;
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
