package de.kontext_e.idea.plugins.jqa;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.usageView.UsageInfo;

public class JqaRelativePathFile implements JqaClassFqnResult {
    private final String relativePath;

    JqaRelativePathFile(final String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public UsageInfo toUsageInfo(final Project project) {
        VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(relativePath);
        if(virtualFile == null) return null;
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        return new UsageInfo(psiFile, 0, 0);
    }

}
