package org.jenkinsci.plugins.deployment;

import hudson.model.Fingerprint;
import jenkins.model.FingerprintFacet;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Kohsuke Kawaguchi
 */
public class DeploymentFacet extends FingerprintFacet {
    public final CopyOnWriteArrayList<HostRecord> records = new CopyOnWriteArrayList<HostRecord>();

    public DeploymentFacet(Fingerprint fingerprint, long timestamp) {
        super(fingerprint, timestamp);
    }

    public void add(HostRecord r) throws IOException {
        records.add(r);
        getFingerprint().save();
        for (DeploymentFacetListener l : DeploymentFacetListener.all()) {
            l.onChange(this,r);
        }
    }
}
