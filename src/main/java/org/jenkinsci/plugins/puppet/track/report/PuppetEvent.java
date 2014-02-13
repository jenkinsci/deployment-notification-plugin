package org.jenkinsci.plugins.puppet.track.report;

import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public class PuppetEvent {
    public String property;
    public String message;
    public String previous_value, desired_value;
    public String name;
    public String status;

    /**
     * Figure out MD5 checksum from {@link #message}
     */
    public String getNewChecksum() {
        /*
            three possible messages (can contain additional suffix):

            return "defined content as '#{newvalue}'"
            return "undefined content from '#{currentvalue}'"
            return "content changed '#{currentvalue}' to '#{newvalue}'"
         */
        if (message.startsWith("defined content as"))
            return extractChecksum(1);
        if (message.startsWith("undefined content from "))
            return null;
        if (message.startsWith("content changed"))
            return extractChecksum(3);

        LOGGER.fine("Unexpected message: "+message);
        return null;
    }

    public String getOldChecksum() {
        if (message.startsWith("defined content as"))
            return null;
        if (message.startsWith("undefined content from "))
            return extractChecksum(1);
        if (message.startsWith("content changed"))
            return extractChecksum(1);

        LOGGER.fine("Unexpected message: "+message);
        return null;
    }

    private String extractChecksum(int index) {
        String[] t = message.split("\'");
        String v = t[index];
        if (v.startsWith("{md5}")) {
            return v.substring(5);
        } else {
            LOGGER.fine("Expected to find {md5} but got "+v);
            return null;    // failed to parse
        }
    }

    private static final Logger LOGGER = Logger.getLogger(PuppetEvent.class.getName());
}
