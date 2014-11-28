package de.kontext_e.idea.plugins.jqa;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.usageView.UsageInfo;

public class JqaClass implements JqaClassFqnResult {

    public static final Label LABEL_CLASS = new Label() {
        @Override
        public String name() {
            return "Class";
        }
    };
    private String fqn;

    public JqaClass(final Node node) {
        this.fqn = (String) node.getProperty("fqn");
    }

    @Override
    public String getClassFqn() {
        return fqn;
    }

    @Override
    public int calculateOffset(final PsiJavaFile psiJavaFile) {
        return psiJavaFile.getClasses()[0].getNameIdentifier().getTextRange().getStartOffset();
    }

    @Override
    public UsageInfo toUsageInfo(final Project project) {
        return null;
    }

    public static boolean isResponsibleFor(final Node node) {
        return node.hasProperty("fqn");
    }
}
