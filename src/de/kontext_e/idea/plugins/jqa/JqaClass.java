package de.kontext_e.idea.plugins.jqa;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.usageView.UsageInfo;

public class JqaClass implements JqaClassFqnResult {

    private String fqn;

    JqaClass(final String fqn) {
        this.fqn = fqn;
    }

    @Override
    public UsageInfo toUsageInfo(final Project project) {
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(fqn, GlobalSearchScope.projectScope(project));
        if(psiClass == null) return null;
        return new UsageInfo(psiClass, psiClass.getNameIdentifier().getStartOffsetInParent(), psiClass.getNameIdentifier().getStartOffsetInParent());
    }
}
