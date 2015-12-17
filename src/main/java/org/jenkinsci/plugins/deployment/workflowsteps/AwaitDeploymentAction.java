package org.jenkinsci.plugins.deployment.workflowsteps;

import hudson.model.Run;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AwaitDeployment action
 */
public class AwaitDeploymentAction implements RunAction2 {

    private final String message;
    private final List<AwaitDeploymentStepExecution> executions = new ArrayList<AwaitDeploymentStepExecution>();

    @DataBoundConstructor
    public AwaitDeploymentAction(String message) {
        this.message = message;
    }

    private transient Run<?,?> run;
    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
        assert executions != null && !executions.contains(null) : executions;
        for (AwaitDeploymentStepExecution step : executions) {
            step.run = run;
        }
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    /**
     * Called when {@link AwaitDeploymentAction} is completed to remove it from the active input list.
     */
    public synchronized void remove(AwaitDeploymentStepExecution exec) throws IOException {
        executions.remove(exec);
        run.save();
    }

    public synchronized void add(@Nonnull AwaitDeploymentStepExecution step) throws IOException {
        this.executions.add(step);
        run.save();
    }

}
