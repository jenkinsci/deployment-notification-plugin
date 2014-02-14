package org.jenkinsci.plugins.deployment;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class Condition extends AbstractDescribableImpl<Condition> implements ExtensionPoint {
    @Override
    public ConditionDescriptor getDescriptor() {
        return (ConditionDescriptor)super.getDescriptor();
    }
}
