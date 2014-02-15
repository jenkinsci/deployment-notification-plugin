package org.jenkinsci.plugins.deployment;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.BuildableItem;
import hudson.model.Fingerprint;
import hudson.model.Fingerprint.RangeSet;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.FingerprintFacet;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

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
 * {@link Trigger} that fires when artifacts are deployed
 * @author Kohsuke Kawaguchi
 */
public class DeploymentTrigger extends Trigger<AbstractProject> {
    private final String upstreamJob;
    private final String env;
    private final int threshold;

    private transient Job upstream;

    @DataBoundConstructor
    public DeploymentTrigger(String upstreamJob, String env, int threshold) {
        this.upstreamJob = upstreamJob;
        this.env = Util.fixEmpty(env);
        this.threshold = threshold;
    }

    public String getUpstreamJob() {
        return upstreamJob;
    }

    public String getEnv() {
        return env;
    }

    public int getThreshold() {
        return threshold;
    }

    @Override
    public void start(AbstractProject project, boolean newInstance) {
        super.start(project, newInstance);
        upstream = Jenkins.getInstance().getItem(upstreamJob, job, Job.class);
    }

    public void checkAndFire(DeploymentFacet facet) {
        try {
            int n = firesWith(facet);
            if (n>0) {
                if (findTriggeredRecord(facet.getFingerprint()).add(this)) {
                    Run b = upstream.getBuildByNumber(n);
                    if (b!=null) {
                        // pass all the current parameters if we can
                        ParametersAction action = b.getAction(ParametersAction.class);
                        job.scheduleBuild(job.getQuietPeriod(),new UpstreamDeploymentCause(b),action);
                    } else
                        job.scheduleBuild();    // TODO: expose a version that takes name and build number
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

    private int firesWith(DeploymentFacet facet) {
        Fingerprint f = facet.getFingerprint();

        if (upstream==null)     return 0;
        RangeSet r = f.getRangeSet(upstream);
        if (r.isEmpty())        return 0;

        // at this point, we verified that the fingerprint touches the project we care about

        // count the deployment
        int cnt = 0;
        for (HostRecord hr : facet.records) {
            if (env==null || env.equals(hr.getEnv()))
                cnt++;
            if (cnt>=threshold)
                return r.min();
        }

        // not enough deployments have happened yet
        return 0;
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
                    for (AbstractProject<?,?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                        DeploymentTrigger t = p.getTrigger(DeploymentTrigger.class);
                        if (t!=null) {
                            t.checkAndFire(facet);
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
    }

    private static final Logger LOGGER = Logger.getLogger(DeploymentTrigger.class.getName());
}
