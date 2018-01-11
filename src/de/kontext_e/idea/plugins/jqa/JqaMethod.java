package de.kontext_e.idea.plugins.jqa;

import org.neo4j.graphdb.Label;

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
    private String name;
    private String classFqn;

    public JqaMethod(final String name, final String signature) {
        this.name = name;
        this.classFqn = classFqnOfMethod(signature);
    }

    public String getClassFqn() {
        return classFqn;
    }

    public int calculateOffset(final PsiClass psiClass) {
        if(psiClass == null) {
            return 0;
        }

        PsiMethod[] allMethods = psiClass.getMethods();
        if(allMethods == null)
        {
            // Fallback: jump to class name
            return psiClass.getNameIdentifier().getTextRange().getStartOffset();
        }

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

    public static String classFqnOfMethod(final String signature) {
        int indexOf = signature.lastIndexOf("(");
        return signature.substring(0, signature.substring(0, indexOf).lastIndexOf(" "));
    }

    @Override
    public String toString() {
        return "JqaMethod{" +
               "name='" + name + '\'' +
               ", classFqn='" + classFqn + '\'' +
               '}';
    }
}
