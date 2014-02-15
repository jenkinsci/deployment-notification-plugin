package org.jenkinsci.plugins.deployment

import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class DeploymentFacetTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    void configRoundtrip() {
        def p = j.createFreeStyleProject()

        def before = new DeploymentTrigger("abc","def",3)
        p.addTrigger(before)
        j.configRoundtrip(p)
        j.assertEqualDataBoundBeans(before, p.getTrigger(DeploymentTrigger.class))
    }
}
