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


    public HostRecord(long timestamp, String host, String env, String path, String replaces) {
        this.timestamp = timestamp;
        this.host = host;
        this.env = env;
        this.path = path;
        this.replaces = replaces;
    }

    public HostRecord(String host, String env, String path, String replaces) {
        this(System.currentTimeMillis(),host,env,path,replaces);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostRecord that = (HostRecord) o;

        if (timestamp != that.timestamp) return false;
        if (env != null ? !env.equals(that.env) : that.env != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (replaces != null ? !replaces.equals(that.replaces) : that.replaces != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (env != null ? env.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (replaces != null ? replaces.hashCode() : 0);
        return result;
    }
}
