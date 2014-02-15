package org.jenkinsci.plugins.deployment.promoted_builds;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Fingerprint;
import hudson.model.Fingerprint.RangeSet;
import hudson.model.Item;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotionBadge;
import hudson.plugins.promoted_builds.PromotionCondition;
import hudson.plugins.promoted_builds.PromotionConditionDescriptor;
import hudson.plugins.promoted_builds.PromotionProcess;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.deployment.Condition;
import org.jenkinsci.plugins.deployment.DeploymentFacet;
import org.jenkinsci.plugins.deployment.DeploymentFacetListener;
import org.jenkinsci.plugins.deployment.HostRecord;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link PromotionCondition} to test if artifacts have been deployed.
 *
 * @author Kohsuke Kawaguchi
 */
public class DeploymentPromotionCondition extends PromotionCondition {
    private final Condition cond;

    @DataBoundConstructor
    public DeploymentPromotionCondition(Condition cond) {
        this.cond = cond;
    }

    @Override
    public PromotionBadge isMet(PromotionProcess promotionProcess, AbstractBuild<?,?> build) {
        for (Fingerprint f : build.getBuildFingerprints()) {
            RangeSet r = cond.calcMatchingBuildNumberOf(build.getProject(), f);
            if (r.includes(build.getNumber())) {
                // promoted!
                return new Badge();
            }
        }

        // no deployment record match the criteria
        return null;
    }

    public static final class Badge extends PromotionBadge {
    }

    @Extension
    public static final class DescriptorImpl extends PromotionConditionDescriptor {
        public boolean isApplicable(AbstractProject<?,?> item) {
            return true;
        }

        public String getDisplayName() {
            return "When configuration management tools finish deploying artifacts to server";
        }
    }

    /**
     * Whenever a new deployment record arrives, check if we need to trigger any jobs.
     */
    @Extension
    public static class ListenerImpl extends DeploymentFacetListener {
        @Override
        public void onChange(final DeploymentFacet facet, HostRecord newRecord) {
            for (String jobFullName : facet.getFingerprint().getJobs()) {
                Item item = Jenkins.getInstance().getItemByFullName(jobFullName);
                if (!(item instanceof AbstractProject))     continue;

                AbstractProject<?,?> p = (AbstractProject) item;    // job that defines the promotion process

                JobPropertyImpl jp = p.getProperty(JobPropertyImpl.class);
                if (jp==null)   continue;

                for (PromotionProcess pp : jp.getItems()) {
                    // is it worth considering the promotion of a build of this process?
                    RangeSet range = new RangeSet();
                    for (PromotionCondition cond : pp.conditions) {
                        if (cond instanceof DeploymentPromotionCondition) {
                            DeploymentPromotionCondition dpcond = (DeploymentPromotionCondition) cond;
                            range = dpcond.cond.calcMatchingBuildNumberOf(p, facet);
                            if (!range.isEmpty()) {
                                break;
                            }
                        }
                    }
                    if (range.isEmpty())        continue;   // nope, DeploymentPromotionCondition still didn't match

                    for (Integer n : range.listNumbers()) {
                        AbstractBuild<?,?> b = p.getBuildByNumber(n);
                        if (b!=null) {
                            try {
                                pp.considerPromotion2(b);
                            } catch (IOException e) {
                                LOGGER.log(Level.WARNING, "Failed to consider promotion of " + b, e);
                            }
                            // for the expected use case, every build produces a different binary,
                            // so there should be only one number in the range. so whether or not promotion
                            // happens, cut the search.
                            return;
                        }
                    }
                }
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(DeploymentPromotionCondition.class.getName());
}