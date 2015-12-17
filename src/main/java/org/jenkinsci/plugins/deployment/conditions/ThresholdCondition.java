package org.jenkinsci.plugins.deployment.conditions;

import hudson.Extension;
import hudson.Util;
import hudson.model.Fingerprint;
import hudson.model.Fingerprint.RangeSet;
import hudson.model.Job;
import org.jenkinsci.plugins.deployment.Condition;
import org.jenkinsci.plugins.deployment.ConditionDescriptor;
import org.jenkinsci.plugins.deployment.DeploymentFacet;
import org.jenkinsci.plugins.deployment.HostRecord;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public class ThresholdCondition extends Condition {
    private final String env;
    private final int threshold;

    @DataBoundConstructor
    public ThresholdCondition(String env, int threshold) {
        this.env = Util.fixEmpty(env);
        this.threshold = threshold;
    }

    public String getEnv() {
        return env;
    }

    public int getThreshold() {
        return threshold;
    }


    @Override
    public RangeSet calcMatchingBuildNumberOf(Job upstream, DeploymentFacet<?> facet) {
        Fingerprint f = facet.getFingerprint();
        RangeSet r;

        if (upstream==null)     return new RangeSet();

        r = f.getRangeSet(upstream);
        if (r.isEmpty())        return new RangeSet();


        // at this point, we verified that the fingerprint touches the project we care about

        // count the deployment
        Set<String> hosts = new HashSet<String>();
        for (HostRecord hr : facet.records) {
            if (env==null || env.equals(hr.getEnv()))
                hosts.add(hr.getHost());
            if (hosts.size()>=threshold)
                return r;
        }

        // not enough deployments have happened yet
        return new RangeSet();
    }

    @Extension
    public static class DescritorImpl extends ConditionDescriptor {
        @Override
        public String getDisplayName() {
            return "When deployed on N servers";
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ThresholdCondition.class.getName());
    public static final int NO_MATCH = Integer.MIN_VALUE;
}
