package org.jenkinsci.plugins.deployment.workflowsteps;

import com.google.common.base.Function;
import hudson.Extension;
import org.jenkinsci.plugins.deployment.DeploymentFacet;
import org.jenkinsci.plugins.deployment.DeploymentFacetListener;
import org.jenkinsci.plugins.deployment.DeploymentTrigger;
import org.jenkinsci.plugins.deployment.HostRecord;

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
        POOL.submit(() -> {
        AwaitDeploymentStepExecution.applyAll(AwaitDeploymentStepExecution.class, awaitDeploymentStepExecution -> {
                awaitDeploymentStepExecution.proceed(facet, newRecord);
            return null;
        });
        });
    }

    public final ExecutorService POOL = new ThreadPoolExecutor(0, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    private static final Logger LOGGER = Logger.getLogger(DeploymentTrigger.class.getName());

}
