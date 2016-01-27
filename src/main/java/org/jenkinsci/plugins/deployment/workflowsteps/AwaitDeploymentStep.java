package org.jenkinsci.plugins.deployment.workflowsteps;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * Workflow Step for awaiting for a deployment
 */
public class AwaitDeploymentStep extends AbstractStepImpl implements Serializable {

    private final int threshold;
    private final String env;

    @DataBoundConstructor
    public AwaitDeploymentStep(int threshold, String env) {
        this.threshold = threshold;
        this.env = env;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getEnv() {
        return env;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(AwaitDeploymentStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "awaitDeployment";
        }

        @Override
        public String getDisplayName() {
            return "Awaiting for deployment";
        }
    }

}
