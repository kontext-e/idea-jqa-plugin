package de.kontext_e.idea.plugins.jqa;

import org.neo4j.graphdb.Node;

public class JqaClass implements JqaClassFqnResult {

    private String fqn;

    public JqaClass(final Node node) {
        this.fqn = (String) node.getProperty("fqn");
    }

    @Override
    public String getClassFqn() {
        return fqn;
    }

    public static boolean isResponsibleFor(final Node node) {
        return node.hasLabel(FindInNeo4jDatabaseAction.LABEL_CLASS);
    }
}
