package org.jenkinsci.plugins.deployment.promoted_builds

import hudson.plugins.promoted_builds.JobPropertyImpl
import org.jenkinsci.plugins.deployment.conditions.ThresholdCondition
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

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

        def promo = new JobPropertyImpl(p)
        p.addProperty(promo)

        def pp = promo.addProcess("process1")

        def before = new DeploymentPromotionCondition(new ThresholdCondition("test", 3))
        pp.conditions.add(before)

        j.configRoundtrip(p)

        def after = p.getProperty(JobPropertyImpl.class).getItem("process1").conditions.get(DeploymentPromotionCondition.class)

        j.assertEqualDataBoundBeans(before, after)
    }
}
