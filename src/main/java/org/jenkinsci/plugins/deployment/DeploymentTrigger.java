package org.jenkinsci.plugins.deployment;

import hudson.Extension;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildableItem;
import hudson.model.Fingerprint;
import hudson.model.Fingerprint.RangeSet;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.FingerprintFacet;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Trigger} that fires when artifacts are deployed.
 *
 * @author Kohsuke Kawaguchi
 */
public class DeploymentTrigger extends Trigger<Job> {
    private final String upstreamJob;
    private final Condition cond;

    private transient volatile Job upstream;

    @DataBoundConstructor
    public DeploymentTrigger(String upstreamJob, Condition cond) {
        this.upstreamJob = upstreamJob;
        this.cond = cond;
    }

    public String getUpstreamJob() {
        return upstreamJob;
    }

    public Condition getCond() {
        return cond;
    }

    public void checkAndFire(DeploymentFacet facet) {
        try {
            if (upstream==null)
                upstream = Jenkins.getInstance().getItem(upstreamJob, job, Job.class);

            ParameterizedJobMixIn parameterizedJobMixIn = new ParameterizedJobMixIn() {
                @Override protected Job asJob() {
                    return job;
                }
            };

            RangeSet r = cond.calcMatchingBuildNumberOf(upstream, facet);
            if (!r.isEmpty()) {
                if (findTriggeredRecord(facet.getFingerprint()).add(this)) {
                    for (Integer n : r.listNumbers()) {
                        Run b = upstream.getBuildByNumber(n);
                        if (b!=null) {
                            // pass all the current parameters if we can
                            ParametersAction action = b.getAction(ParametersAction.class);
                            parameterizedJobMixIn.scheduleBuild2(5, action);
                            return;
                        }
                    }

                    // didn't find any matching build, so just trigger it but without the cause to link to the upstream
                    parameterizedJobMixIn.scheduleBuild();    // TODO: expose a version that takes name and build number
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to trigger "+job, e);
        }
    }

    private Triggered findTriggeredRecord(Fingerprint f) {
        for (FingerprintFacet ff : f.getFacets()) {
            if (ff instanceof Triggered) {
                return (Triggered) ff;
            }
        }
        Triggered t = new Triggered(f, System.currentTimeMillis());
        f.getFacets().add(t);
        return t;
    }

    /**
     * Whenever a new deployment record arrives, check if we need to trigger any jobs.
     */
    @Extension
    public static class ListenerImpl extends DeploymentFacetListener {
        @Override
        public void onChange(final DeploymentFacet facet, HostRecord newRecord) {
            POOL.submit(new Runnable() {
                public void run() {
                    for (Job<?,?> job : Jenkins.getInstance().getAllItems(Job.class)) {
                        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
                            ParameterizedJobMixIn.ParameterizedJob pJob = (ParameterizedJobMixIn.ParameterizedJob) job;
                            for (Trigger trigger : pJob.getTriggers().values()) {
                                if (trigger instanceof DeploymentTrigger) {
                                    DeploymentTrigger deploymentTrigger = (DeploymentTrigger) trigger;
                                    deploymentTrigger.checkAndFire(facet);
                                }
                            }
                        }
                    }
                }
            });
        }

        /**
         * Waits until all the pending deployment facets are processed.
         */
        public void sync() throws InterruptedException {
            try {
                POOL.submit(new Runnable() {
                    public void run() {
                        // no-op
                    }
                }).get();
            } catch (ExecutionException e) {
                throw (InterruptedException)new InterruptedException().initCause(e);
            }
        }

        public final ExecutorService POOL = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public static void sync() throws InterruptedException {
        DeploymentFacetListener.all().get(ListenerImpl.class).sync();
    }

    /**
     * Marks the jobs that have already been triggered as a result of {@link DeploymentTrigger}
     * to avoid multi-firing when additional hosts check in.
     */
    public static class Triggered extends FingerprintFacet {
        private final HashSet<String> jobs = new HashSet<String>();
        public Triggered(Fingerprint fingerprint, long timestamp) {
            super(fingerprint, timestamp);
        }

        boolean add(DeploymentTrigger t) throws IOException {
            synchronized (jobs) {
                boolean b = jobs.add(t.job.getFullName());
                if (!b)     return false;   // already in the set
            }
            getFingerprint().save();
            return true;
        }
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {
        @Override
        public boolean isApplicable(Item item) {
            return item instanceof BuildableItem;
        }

        @Override
        public String getDisplayName() {
            return "When configuration management tools finish deploying artifacts to server";
        }

        public AutoCompletionCandidates doAutoCompleteUpstreamJob(@QueryParameter String value, @AncestorInPath Item self, @AncestorInPath ItemGroup container) {
            return AutoCompletionCandidates.ofJobNames(Job.class, value, self,  container);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(DeploymentTrigger.class.getName());
}
