package de.kontext_e.idea.plugins.jqa;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class QueryToolWindow implements ToolWindowFactory {
    private ToolWindow myToolWindow;
    private JPanel myToolWindowContent;
    private JButton refreshToolWindowButton;
    private JButton hideToolWindowButton;
    private Project project;

    @Override
    public void createToolWindowContent(final Project project, final ToolWindow toolWindow) {
        this.myToolWindow = toolWindow;
        this.project = project;

        final JPanel panel1 = ToolWindowPanelBuilder.buildToolWindowPanel(project);

        myToolWindowContent = panel1;

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(panel1, "", false);
        toolWindow.getContentManager().addContent(content);
    }

}
