package org.jenkinsci.plugins.puppet.track.report;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Node;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class PuppetReport {
    public String host;
    public String environment;
    public String time;
    public String configuration_version;

    public Map<String,PuppetStatus> resource_statuses = new HashMap<String, PuppetStatus>();

    public static PuppetReport load(InputStream in) {
        return (PuppetReport)PARSER.load(in);
    }

    public static PuppetReport load(Reader r) {
        return (PuppetReport)PARSER.load(r);
    }

    public static final Yaml PARSER = buildParser();

    private static Yaml buildParser() {
        Constructor c = new Constructor(PuppetReport.class) {
            {
                // ignore missing properties in YAML that we don't care
                PropertyUtils p = new PropertyUtils();
                p.setSkipMissingProperties(true);
                setPropertyUtils(p);

                // map symbol to String
                addTypeDescription(new TypeDescription(String.class,"!ruby/sym"));
            }

            /**
             * If we encounter unknown tags, ignore them.
             */
            @Override
            protected Class<?> getClassForNode(Node node) {
                try {
                    return super.getClassForNode(node);
                } catch (Exception e) {
                    return node.getType();
                }
            }
        };

        return new Yaml(c);
    }
}
