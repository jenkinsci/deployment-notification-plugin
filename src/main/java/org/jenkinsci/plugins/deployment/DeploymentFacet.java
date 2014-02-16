package org.jenkinsci.plugins.deployment;

import hudson.model.Fingerprint;
import jenkins.model.FingerprintFacet;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Records a collection of {@link HostRecord} to a {@link Fingerprint} object.
 *
 * @author Kohsuke Kawaguchi
 */
public class DeploymentFacet<T extends HostRecord> extends FingerprintFacet {
    public final CopyOnWriteArrayList<T> records = new CopyOnWriteArrayList<T>();

    public DeploymentFacet(Fingerprint fingerprint, long timestamp) {
        super(fingerprint, timestamp);
    }

    public void add(T r) throws IOException {
        records.add(r);
        getFingerprint().save();
        for (DeploymentFacetListener l : DeploymentFacetListener.all()) {
            l.onChange(this,r);
        }
    }
}
