package org.jenkinsci.plugins.puppet.track.report

import org.junit.Test

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class PuppetEventTest {
    @Test
    void md5new() {
        def ev = new PuppetEvent(message:"defined content as '{md5}e4a57ad2a0bc444804d53916ee23770f'")
        assert ev.newChecksum=="e4a57ad2a0bc444804d53916ee23770f"
        assert ev.oldChecksum==null
    }

    @Test
    void md5change() {
        def ev = new PuppetEvent(message:"content changed '{md5}261ed8dadd135f991a611b8ebb88a9fd' to '{md5}e4a57ad2a0bc444804d53916ee23770f'")
        assert ev.newChecksum=="e4a57ad2a0bc444804d53916ee23770f"
        assert ev.oldChecksum=="261ed8dadd135f991a611b8ebb88a9fd"
    }

    @Test
    void md5del() {
        def ev = new PuppetEvent(message:"undefined content from '{md5}261ed8dadd135f991a611b8ebb88a9fd'")
        assert ev.newChecksum==null
        assert ev.oldChecksum=="261ed8dadd135f991a611b8ebb88a9fd"
    }
}
