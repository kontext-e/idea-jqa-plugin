package de.kontext_e.idea.plugins.jqa;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.usageView.UsageInfo;

public class JqaMethod implements JqaClassFqnResult {

    public static final Label LABEL_METHOD = new Label() {
        @Override
        public String name() {
            return "Method";
        }
    };
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

    public String getClassFqn() {
        return classFqn;
    }

    public int calculateOffset(final PsiClass psiClass) {
        PsiMethod[] allMethods = psiClass.getMethods();
        for (PsiMethod psiMethod : allMethods) {
            // todo: look for signature, not only name
            if(psiMethod.getName().equals(name)) {
                return psiMethod.getNameIdentifier().getTextRange().getStartOffset();
            }
        }

        // Fallback: jump to class name
        return psiClass.getNameIdentifier().getTextRange().getStartOffset();
    }

    @Override
    public UsageInfo toUsageInfo(final Project project) {
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(classFqn, GlobalSearchScope.projectScope(project));
        int classNameStartOffset = calculateOffset(psiClass);
        return new UsageInfo(psiClass.getContainingFile(), classNameStartOffset, classNameStartOffset);
    }

    public static boolean isResponsibleFor(final Node node) {
        return node.hasLabel(LABEL_METHOD);
    }

    public static String classFqnOfMethod(final String signature) {
        int indexOf = signature.lastIndexOf("(");
        return signature.substring(0, signature.substring(0, indexOf).lastIndexOf(" "));
    }
}
