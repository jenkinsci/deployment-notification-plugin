package org.jenkinsci.plugins.puppet.track.report

import org.junit.Assert
import org.junit.Test

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class LoaderTest extends Assert {
    /**
     * Basic data binding test.
     */
    @Test
    void loadReport() {
        def r = PuppetReport.load(this.class.getResourceAsStream("report1.yaml"))
        assert r.environment=="production"
        assert r.host=="dragon"

        def s = r.resource_statuses["File[/tmp/foo.war]"];
        assert s.changed
        assert s.title=="/tmp/foo.war"
        assert !s.skipped
        assert !s.failed
        assert s.resource_type=="File"
        assert s.resource=="File[/tmp/foo.war]"
        assert s.events.size()==1

        def e = s.events[0]
        assert e.property=="ensure"
        assert e.name=="file_created"
        assert e.previous_value=="absent"
        assert e.desired_value=="file"
        assert e.message=="defined content as '{md5}e4a57ad2a0bc444804d53916ee23770f'"
    }
}
