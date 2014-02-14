package org.jenkinsci.plugins.deployment;

import hudson.model.Cause.UpstreamCause;
import hudson.model.Run;

/**
 * Indicates that a build was triggered because an artifact from a previous build was deployed to servers.
 *
 * <p>
 * This class extends from {@link UpstreamCause} to point to the build that previously touched the artifact in question.
 *
 * @author Kohsuke Kawaguchi
 */
public class UpstreamDeploymentCause extends UpstreamCause {
    public UpstreamDeploymentCause(Run<?, ?> up) {
        super(up);
    }
}
