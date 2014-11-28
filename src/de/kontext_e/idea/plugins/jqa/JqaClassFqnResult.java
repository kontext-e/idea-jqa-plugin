package de.kontext_e.idea.plugins.jqa;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.usageView.UsageInfo;

public interface JqaClassFqnResult {
    String getClassFqn();

    int calculateOffset(PsiJavaFile psiJavaFile);

    UsageInfo toUsageInfo(Project project);
}
