package de.kontext_e.idea.plugins.jqa;

import com.intellij.psi.PsiJavaFile;

public interface JqaClassFqnResult {
    String getClassFqn();

    int calculateOffset(PsiJavaFile psiJavaFile);
}
