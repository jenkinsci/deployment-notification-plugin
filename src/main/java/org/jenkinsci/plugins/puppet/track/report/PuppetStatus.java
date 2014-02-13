package org.jenkinsci.plugins.puppet.track.report;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class PuppetStatus {
    public String resource;
    public boolean changed;
    public String resource_type;
    public String title;
    public boolean skipped;
    public boolean failed;

    public List<PuppetEvent> events = new ArrayList<PuppetEvent>();
}
