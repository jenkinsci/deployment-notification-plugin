package org.jenkinsci.plugins.puppet.track;

import hudson.model.Fingerprint;
import org.jenkinsci.plugins.deployment.DeploymentFacet;

/**
 * {@link DeploymentFacet} for Puppet.
 *
 * @author Kohsuke Kawaguchi
 */
public class PuppetDeploymentFacet extends DeploymentFacet {
    public PuppetDeploymentFacet(Fingerprint fingerprint, long timestamp) {
        super(fingerprint, timestamp);
    }
}
