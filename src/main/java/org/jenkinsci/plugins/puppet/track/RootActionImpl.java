package org.jenkinsci.plugins.puppet.track;

import hudson.Util;
import hudson.model.Fingerprint;
import hudson.model.FingerprintMap;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import jenkins.model.FingerprintFacet;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.puppet.track.report.PuppetEvent;
import org.jenkinsci.plugins.puppet.track.report.PuppetReport;
import org.jenkinsci.plugins.puppet.track.report.PuppetStatus;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Kohsuke Kawaguchi
 */
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
        return "puppet";
    }

    /**
     * Receives the submission from HTTP reporter to track fingerprints.
     */
    @RequirePOST
    public HttpResponse doReport(StaplerRequest req) throws IOException {
        // TODO: permission check
        // TODO: stapler YAML support

        PuppetReport r = PuppetReport.load(req.getReader());

        String host = r.host;
        if (host==null)     host = "unknown";

        String env = r.environment;
        if (env==null)      env = "unknown";

        for (PuppetStatus st : r.resource_statuses.values()) {
            // TODO: pluggability for matching resources
            if (st.resource_type.equals("File")) {
                for (PuppetEvent ev : st.events) {
//                    ev.getChecksum();
                }
            }
        }

//        FingerprintMap fm = jenkins.getFingerprintMap();
//        Fingerprint f = fm.get(checksum);
//
//        Collection<FingerprintFacet> facets = f.getFacets();
//        DeploymentFacet df = findDeploymentFacet(facets);
//        if (df==null) {
//            df = new DeploymentFacet(f,System.currentTimeMillis());
//            facets.add(df);
//        }
//
//        df.records.add(new ServerDeploymentRecord(host, env));
//
//        f.save();
        return HttpResponses.ok();
    }

    private DeploymentFacet findDeploymentFacet(Collection<FingerprintFacet> facets) {
        for (DeploymentFacet df : Util.filter(facets, DeploymentFacet.class)) {
            return df;
        }
        return null;
    }
}
