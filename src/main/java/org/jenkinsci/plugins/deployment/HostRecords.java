/*
 * The MIT License
 *
 * Copyright 2015 Felix Belzunce Arcos.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.deployment;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;
import hudson.model.InvisibleAction;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Injects the deployment environment variables into the build
 * @since May 8, 2015
 * @version TODO
 */
public class HostRecords extends InvisibleAction implements EnvironmentContributingAction {
   private final @Nonnull Collection<HostRecord> hostrecords;

    public HostRecords(@Nonnull Collection<HostRecord> hostRecords) {
        this.hostrecords = hostRecords;
    }

    public @Nonnull Collection<HostRecord> getHostrecords() {
        return hostrecords;
    }

    public void buildEnvVars(AbstractBuild<?, ?> abstractBuild, EnvVars envVars) {
        @Nonnull final List<String> deploymentEnv = new ArrayList();
        @Nonnull final List<String> deploymentHost = new ArrayList();
        @Nonnull final List<String> deploymentPath = new ArrayList();
        @Nonnull final List<String> deploymentTimeStamp = new ArrayList();

        Collection<HostRecord> hostRecords = getHostrecords();

        for (HostRecord hostRecord : hostRecords) {
            deploymentEnv.add(hostRecord.getEnv());
            deploymentHost.add(hostRecord.getHost());
            deploymentPath.add(hostRecord.getPath());
            deploymentTimeStamp.add(hostRecord.getTimestampString());
        }
        envVars.put("DEPLOYMENT_ENV", StringUtils.join(deploymentEnv, ","));
        envVars.put("DEPLOYMENT_HOST", StringUtils.join(deploymentHost, ","));
        envVars.put("DEPLOYMENT_PATH", StringUtils.join(deploymentPath, ","));
        envVars.put("DEPLOYMENT_TIMESTAMP", StringUtils.join(deploymentTimeStamp, ","));
    }
}
