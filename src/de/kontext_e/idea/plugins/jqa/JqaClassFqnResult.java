package de.kontext_e.idea.plugins.jqa;

import com.intellij.openapi.project.Project;
import com.intellij.usageView.UsageInfo;

public interface JqaClassFqnResult {
    UsageInfo toUsageInfo(Project project);
}
