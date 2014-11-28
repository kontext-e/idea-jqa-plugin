package de.kontext_e.idea.plugins.jqa;

import org.neo4j.graphdb.Node;

public class JqaMethod implements JqaClassFqnResult {

    private String signature;
    private String name;
    private String visibility;
    private String isStatic;
    private String classFqn;

    public JqaMethod(final Node node) {
        signature = (String) node.getProperty("signature");
        name = (String) node.getProperty("name");
        visibility = (String) node.getProperty("visibility");
        isStatic = (String) node.getProperty("static");
        classFqn = classFqnOfMethod(signature);
    }

    @Override
    public String getClassFqn() {
        return classFqn;
    }

    public static boolean isResponsibleFor(final Node node) {
        return node.hasLabel(FindInNeo4jDatabaseAction.LABEL_METHOD);
    }

    public static String classFqnOfMethod(final String signature) {
        int indexOf = signature.lastIndexOf("(");
        return signature.substring(0, signature.substring(0, indexOf).lastIndexOf(" "));
    }
}
