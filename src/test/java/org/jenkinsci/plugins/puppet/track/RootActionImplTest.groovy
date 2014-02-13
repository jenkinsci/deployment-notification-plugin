package org.jenkinsci.plugins.puppet.track

import com.gargoylesoftware.htmlunit.HttpMethod
import com.gargoylesoftware.htmlunit.WebRequestSettings
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
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
        p.buildersList.add(new TestBuilder() {
            @Override
            boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                j.jenkins.fingerprintMap.getOrCreate(build, "foo.war","e4a57ad2a0bc444804d53916ee23770f");
            }
        })
        j.assertBuildStatusSuccess(p.scheduleBuild2(0));

        def req = new WebRequestSettings(new URL(j.getURL(),"puppet/report"), HttpMethod.POST);
        req.requestBody = this.class.getResourceAsStream("report/http-reporter.yaml").text;
        def rsp = j.createWebClient().loadWebResponse(req);
        assert rsp.statusCode==200;

        def f = j.jenkins.fingerprintMap.get("e4a57ad2a0bc444804d53916ee23770f");

        assert f.fileName=="foo.war"
        DeploymentFacet df = f.facets.find { it instanceof DeploymentFacet }
        assert df.records.size()==1
        def rec = df.records[0]
        assert rec.env=="production"
        assert rec.host=="dragon.opendns.com"
        assert rec.path=="/tmp/foo.war"

//        j.interactiveBreak(); // at this point we should have a good data
    }
}
