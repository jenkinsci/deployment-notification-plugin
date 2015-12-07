package org.jenkinsci.plugins.deployment.workflowsteps;

import com.google.inject.Inject;
import hudson.model.Fingerprint;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.deployment.DeploymentFacet;
import org.jenkinsci.plugins.deployment.HostRecord;
import org.jenkinsci.plugins.deployment.conditions.ThresholdCondition;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import java.util.concurrent.ScheduledFuture;

/**
 * The Execution for AwaitDeploymentStep
 */
public class AwaitDeploymentStepExecution extends AbstractStepExecutionImpl {

    @Inject(optional=true) private transient AwaitDeploymentStep awaitDeploymentStep;
    @StepContextParameter private transient FlowNode node;
    @StepContextParameter transient Run run;
    @StepContextParameter private transient Run build;
    @StepContextParameter private transient Job job;
    private transient volatile ScheduledFuture<?> task;

    @Override
    public boolean start() throws Exception {
        node.addAction(new AwaitDeploymentAction("Await for deployment"));

        return false;
    }

    @Override public void stop(Throwable cause) throws Exception {
        if (task != null) {
            task.cancel(false);
        }
        getContext().onFailure(cause);
    }

    public void proceed(DeploymentFacet<?> facet, HostRecord newRecord) {
        ThresholdCondition thresholdCondition = new ThresholdCondition(awaitDeploymentStep.getEnv(), awaitDeploymentStep.getThreshold());
        Fingerprint.RangeSet r = thresholdCondition.calcMatchingBuildNumberOf(job, facet.getFingerprint());

        if (!r.isEmpty()) {
            for (Integer n : r.listNumbers()) {
                Run b = job.getBuildByNumber(n);
                if (b!=null) {
                    if (b.getId().equals(build.getId())) {
                        getContext().onSuccess(null);
                    }
                }
            }
        }
    }

    @Override public void onResume() {
        super.onResume();
    }

}
