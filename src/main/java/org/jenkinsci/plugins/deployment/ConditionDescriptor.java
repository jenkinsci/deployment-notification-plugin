package org.jenkinsci.plugins.deployment;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class ConditionDescriptor extends Descriptor<Condition> {
    public static DescriptorExtensionList<Condition,ConditionDescriptor> all() {
        return Jenkins.get().getDescriptorList(Condition.class);
    }
}
