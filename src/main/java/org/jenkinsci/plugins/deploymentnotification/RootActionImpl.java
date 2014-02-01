package org.jenkinsci.plugins.deploymentnotification;

import hudson.Extension;
import hudson.Util;
import hudson.model.Fingerprint;
import hudson.model.FingerprintMap;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import jenkins.model.FingerprintFacet;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Kohsuke Kawaguchi
 */
@Extension
public class RootActionImpl implements RootAction {
    @Inject
    private Jenkins jenkins;

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "deployment-notification";
    }

    @RequirePOST
    public HttpResponse doNotify(@QueryParameter String host, @QueryParameter String checksum, @QueryParameter String env) throws IOException {
        FingerprintMap fm = jenkins.getFingerprintMap();
        Fingerprint f = fm.get(checksum);

        Collection<FingerprintFacet> facets = f.getFacets();
        DeploymentFacet df = findDeploymentFacet(facets);
        if (df==null) {
            df = new DeploymentFacet(f,System.currentTimeMillis());
            facets.add(df);
        }

        df.records.add(new ServerDeploymentRecord(host, env));

        f.save();
        return HttpResponses.ok();
    }

    private DeploymentFacet findDeploymentFacet(Collection<FingerprintFacet> facets) {
        for (DeploymentFacet df : Util.filter(facets, DeploymentFacet.class)) {
            return df;
        }
        return null;
    }
}
