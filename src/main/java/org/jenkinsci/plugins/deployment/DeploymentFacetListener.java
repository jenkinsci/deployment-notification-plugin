package org.jenkinsci.plugins.deployment;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

/**
 * Listens to the addition of new {@link HostRecord}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class DeploymentFacetListener implements ExtensionPoint {
    /**
     * Called when {@link DeploymentFacet} gets a new {@link HostRecord}.
     */
    public abstract void onChange(DeploymentFacet facet, HostRecord newRecord);

    public static ExtensionList<DeploymentFacetListener> all() {
        return Jenkins.get().getExtensionList(DeploymentFacetListener.class);
    }
}
