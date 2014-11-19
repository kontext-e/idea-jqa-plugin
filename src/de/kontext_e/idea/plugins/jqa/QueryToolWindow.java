package de.kontext_e.idea.plugins.jqa;

import javax.swing.JPanel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class QueryToolWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(final Project project, final ToolWindow toolWindow) {
        final JPanel jqaPanel = ToolWindowPanelBuilder.buildToolWindowPanel(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(jqaPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

}
