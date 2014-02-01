package org.jenkinsci.plugins.deploymentnotification;

import hudson.model.Fingerprint;
import jenkins.model.FingerprintFacet;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Kohsuke Kawaguchi
 */
public class DeploymentFacet extends FingerprintFacet {
    public final CopyOnWriteArrayList<ServerDeploymentRecord> records = new CopyOnWriteArrayList<ServerDeploymentRecord>();

    public DeploymentFacet(Fingerprint fingerprint, long timestamp) {
        super(fingerprint, timestamp);
    }


}
