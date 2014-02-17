package org.jenkinsci.plugins.deployment;

import hudson.init.Initializer;
import hudson.model.Fingerprint;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import jenkins.model.FingerprintFacet;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Records a collection of {@link HostRecord} to a {@link Fingerprint} object.
 *
 * @author Kohsuke Kawaguchi
 */
public class DeploymentFacet<T extends HostRecord> extends FingerprintFacet {
    public final CopyOnWriteArrayList<T> records = new CopyOnWriteArrayList<T>();

    public DeploymentFacet(Fingerprint fingerprint, long timestamp) {
        super(fingerprint, timestamp);
    }

    public void add(T r) throws IOException {
        for (T e : records) {
            if (e.equals(r))
                return;
        }
        records.add(r);
        getFingerprint().save();
        for (DeploymentFacetListener l : DeploymentFacetListener.all()) {
            l.onChange(this,r);
        }
    }

    public static final PermissionGroup PERMISSIONS = new PermissionGroup(DeploymentFacet.class, Messages._DeploymentFacet_Permissions_Title());

    public static final Permission RECORD = new Permission(PERMISSIONS,"Record", Messages._DeploymentFacet_RecordPermission_Description(), Permission.UPDATE, PermissionScope.JENKINS);

    @Initializer
    public static void recordPermission() {
        PERMISSIONS.toString(); // force initialization
    }
}
