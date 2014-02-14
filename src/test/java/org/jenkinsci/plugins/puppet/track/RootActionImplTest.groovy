package org.jenkinsci.plugins.puppet.track

import com.gargoylesoftware.htmlunit.HttpMethod
import com.gargoylesoftware.htmlunit.WebRequestSettings
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.model.Fingerprint
import org.jenkinsci.plugins.deployment.DeploymentFacet
import org.jenkinsci.plugins.deployment.HostRecord
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.TestBuilder

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class RootActionImplTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    void submission() {
        j.jenkins.crumbIssuer=null;

        def p = j.createFreeStyleProject();
        String[] fingerprints = ["e4a57ad2a0bc444804d53916ee23770f","a5656c064e4f94e6e586cf6ca030993f"]
        p.buildersList.add(new TestBuilder() {
            @Override
            boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                j.jenkins.fingerprintMap.getOrCreate(build, "foo.war",fingerprints[build.number-1]);
            }
        })
        j.assertBuildStatusSuccess(p.scheduleBuild2(0));
        j.assertBuildStatusSuccess(p.scheduleBuild2(0));

        for (int n in [1,2]) {
            def req = new WebRequestSettings(new URL(j.getURL(),"puppet/report"), HttpMethod.POST);
            req.requestBody = this.class.getResourceAsStream("report/report${n}.yaml").text;
            def rsp = j.createWebClient().loadWebResponse(req);
            assert rsp.statusCode==200;
        }


        def f1 = j.jenkins.fingerprintMap.get(fingerprints[0]);
        def f2 = j.jenkins.fingerprintMap.get(fingerprints[1]);

        assert f1.fileName=="foo.war"
        HostRecord rec = findRecord(f1)
        assertBasics(rec)

        assert f2.fileName=="foo.war"
        rec = findRecord(f2)
        assertBasics(rec)

        assert rec.replaces=="e4a57ad2a0bc444804d53916ee23770f"

        org.kohsuke.stapler.MetaClass.NO_CACHE=true;
        j.interactiveBreak(); // at this point we should have a good data
    }

    private HostRecord findRecord(Fingerprint f1) {
        PuppetDeploymentFacet df = f1.facets.find { it instanceof PuppetDeploymentFacet }
        assert df.records.size() == 1
        def rec = df.records[0]
        return rec
    }

    private void assertBasics(HostRecord rec) {
        assert rec.env == "production"
        assert rec.host == "dragon"
        assert rec.path == "/tmp/foo.war"
    }
}
