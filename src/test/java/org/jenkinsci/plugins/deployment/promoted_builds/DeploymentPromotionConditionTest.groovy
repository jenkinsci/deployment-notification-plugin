package org.jenkinsci.plugins.deployment.promoted_builds

import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.BuildListener
import hudson.model.FreeStyleProject
import hudson.plugins.promoted_builds.JobPropertyImpl
import hudson.plugins.promoted_builds.PromotionProcess
import hudson.tasks.Fingerprinter
import org.jenkinsci.plugins.deployment.DeploymentFacet
import org.jenkinsci.plugins.deployment.HostRecord
import org.jenkinsci.plugins.deployment.conditions.ThresholdCondition
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.TestBuilder

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class DeploymentPromotionConditionTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    void configRoundtrip() {
        def p = j.createFreeStyleProject()

        def pp = definePromotion(p)

        def before = new DeploymentPromotionCondition(new ThresholdCondition("test", 3))
        pp.conditions.add(before)

        j.configRoundtrip(p)

        def after = p.getProperty(JobPropertyImpl.class).getItem("process1").conditions.get(DeploymentPromotionCondition.class)

        j.assertEqualDataBoundBeans(before, after)
    }

    PromotionProcess definePromotion(FreeStyleProject p) {
        def promo = new JobPropertyImpl(p)
        p.addProperty(promo)
        return promo.addProcess("process1")
    }

    @Test
    void wholeSequence() {
        def fm = j.jenkins.fingerprintMap
        String[] fingerprints = ["e4a57ad2a0bc444804d53916ee23770f","a5656c064e4f94e6e586cf6ca030993f"]

        def p = j.createFreeStyleProject();
        def pp = definePromotion(p);

        def dpc = new DeploymentPromotionCondition(new ThresholdCondition("test", 2))
        pp.conditions.add(dpc)

        // record fingerprint
        p.buildersList.add(new TestBuilder() {
            @Override
            boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                def md5 = fingerprints[build.number - 1]
                def fp = fm.getOrCreate(build, "foo.war", md5);
                fp.add(build)
                build.addAction(new Fingerprinter.FingerprintAction(build,["foo.war":md5]))
                return true;
            }
        })

        def b1 = j.assertBuildStatusSuccess(p.scheduleBuild2(0));
        def b2 = j.assertBuildStatusSuccess(p.scheduleBuild2(0));

        def f = fm.get(fingerprints[0])
        def df = new DeploymentFacet(f,System.currentTimeMillis())
        f.facets.add(df)

        df.add(new HostRecord("box1","test","foo.war",null))
        assert pp.getBuildByNumber(1)==null : "it takes two deployment records to promote"

        df.add(new HostRecord("box2","test","foo.war",null))
        flushQueue()
        assert pp.getBuildByNumber(1)!=null : "now the build should have been promoted"
    }

    private void flushQueue() {
        while (j.jenkins.queue.items.length > 0) // wait until queued builds are started
            Thread.sleep(100);
    }
}
