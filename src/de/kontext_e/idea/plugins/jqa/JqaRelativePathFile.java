package de.kontext_e.idea.plugins.jqa;

import org.neo4j.graphdb.Node;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.usageView.UsageInfo;

public class JqaRelativePathFile implements JqaClassFqnResult {
    private final String relativePath;

    public JqaRelativePathFile(final Node node) {
        this.relativePath = (String)node.getProperty("relativePath");
    }

    @Override
    public String getClassFqn() {
        return null;
    }

    @Override
    public int calculateOffset(final PsiJavaFile psiJavaFile) {
        return 0;
    }

    @Override
    public UsageInfo toUsageInfo(final Project project) {
        VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(relativePath);
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        return new UsageInfo(psiFile, 0, 0);
    }

    public static boolean isResponsibleFor(final Node node) {
        return node.hasProperty("relativePath");
    }

}
