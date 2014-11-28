package de.kontext_e.idea.plugins.jqa;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
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

    public String getClassFqn() {
        return fqn;
    }

    @Override
    public UsageInfo toUsageInfo(final Project project) {
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(fqn, GlobalSearchScope.projectScope(project));
        return new UsageInfo(psiClass, psiClass.getNameIdentifier().getStartOffsetInParent(), psiClass.getNameIdentifier().getStartOffsetInParent());
    }

    public static boolean isResponsibleFor(final Node node) {
        return node.hasProperty("fqn");
    }
}
