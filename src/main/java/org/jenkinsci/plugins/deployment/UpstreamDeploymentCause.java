package org.jenkinsci.plugins.deployment;

import hudson.model.Cause.UpstreamCause;
import hudson.model.Run;

/**
 * @author Kohsuke Kawaguchi
 */
public class UpstreamDeploymentCause extends UpstreamCause {
    public UpstreamDeploymentCause(Run<?, ?> up) {
        super(up);
    }
}
