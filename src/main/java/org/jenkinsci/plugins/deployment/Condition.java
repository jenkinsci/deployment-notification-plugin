package org.jenkinsci.plugins.deployment;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Fingerprint;
import hudson.model.Fingerprint.RangeSet;
import hudson.model.Job;
import jenkins.model.FingerprintFacet;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Condition extends AbstractDescribableImpl<Condition> implements ExtensionPoint {
    /**
     * Checks if the specified {@link DeploymentFacet} would meet the deployment condition
     * of the given {@link Job}. If so, return the build number of the job that matches,
     * otherwise return negative number to indicate no match.
     */
    @Deprecated
    public abstract RangeSet calcMatchingBuildNumberOf(Job upstream, DeploymentFacet<?> facet);

    public abstract RangeSet calcMatchingBuildNumberOf(Job upstream, DeploymentFacet<?> facet, Job workflowJob);

    @Deprecated
    public RangeSet calcMatchingBuildNumberOf(Job upstream, Fingerprint f) {
        for (FingerprintFacet ff : f.getFacets()) {
            if (ff instanceof DeploymentFacet) {
                return calcMatchingBuildNumberOf(upstream,(DeploymentFacet)ff);
            }
        }
        return new RangeSet();  // no match
    }

    public RangeSet calcMatchingBuildNumberOf(Job upstream, Fingerprint f, Job workflowJob) {
        for (FingerprintFacet ff : f.getFacets()) {
            if (ff instanceof DeploymentFacet) {
                return calcMatchingBuildNumberOf(upstream,(DeploymentFacet)ff, workflowJob);
            }
        }
        return new RangeSet();  // no match
    }

    @Override
    public ConditionDescriptor getDescriptor() {
        return (ConditionDescriptor)super.getDescriptor();
    }
}
