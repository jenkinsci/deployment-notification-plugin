package org.jenkinsci.plugins.deployment.workflowsteps;

import com.google.common.base.Function;
import hudson.Extension;
import org.jenkinsci.plugins.deployment.DeploymentFacet;
import org.jenkinsci.plugins.deployment.DeploymentFacetListener;
import org.jenkinsci.plugins.deployment.DeploymentTrigger;
import org.jenkinsci.plugins.deployment.HostRecord;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deployment Listener for Workflow jobs
 */
@Extension
public class WorkflowListenerImpl extends DeploymentFacetListener {

    @Override
    public void onChange(final DeploymentFacet facet, final HostRecord newRecord) {
        LOGGER.log(Level.FINE, "Deployment triggered");
        POOL.submit(new Runnable() {
            public void run() {
            AwaitDeploymentStepExecution.applyAll(AwaitDeploymentStepExecution.class, new Function<AwaitDeploymentStepExecution, Void>() {
                @Override public Void apply(@Nonnull AwaitDeploymentStepExecution awaitDeploymentStepExecution) {
                        awaitDeploymentStepExecution.proceed(facet, newRecord);
                    return null;
                }
            });
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
    private static final Logger LOGGER = Logger.getLogger(DeploymentTrigger.class.getName());

}