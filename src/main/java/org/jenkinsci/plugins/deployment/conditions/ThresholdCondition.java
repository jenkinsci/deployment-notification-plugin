package org.jenkinsci.plugins.deployment.conditions;

import hudson.Extension;
import org.jenkinsci.plugins.deployment.Condition;
import org.jenkinsci.plugins.deployment.ConditionDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Kohsuke Kawaguchi
 */
public class ThresholdCondition extends Condition {
    private final String upstreamJob;
    private final String env;
    private final int threshold;

    @DataBoundConstructor
    public ThresholdCondition(String upstreamJob, String env, int threshold) {
        this.upstreamJob = upstreamJob;
        this.env = env;
        this.threshold = threshold;
    }

    @Extension
    public static class DescritorImpl extends ConditionDescriptor {
        @Override
        public String getDisplayName() {
            return null;
        }
    }
}
