package de.kontext_e.idea.plugins.jqa;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.collection.IteratorUtil;

import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;
import com.intellij.usages.UsageViewPresentation;
import liveplugin.PluginUtil;

import static liveplugin.PluginUtil.show;

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

    class MyFindAction extends AbstractAction {
        private Project myProject;
        JTextArea textArea;
        JTextField neo4jPath;

        public MyFindAction(JTextArea textArea, JTextField neo4jPath, final Project project) {
            this.textArea = textArea;
            this.neo4jPath = neo4jPath;
            this.myProject = project;
        }

        public void actionPerformed(ActionEvent e) {
            openFindTool(resolvePsiElements(queryNeo4j()));
        }

        Usage[] resolvePsiElements(java.util.List<String> usagesList) {
            java.util.List<PsiJavaFile> resolvedPsiElements = new ArrayList<PsiJavaFile>(usagesList.size());
            Iterator<PsiFileSystemItem> psiFileSystemItemIterator = PluginUtil.allPsiItemsIn(myProject);
            while (psiFileSystemItemIterator.hasNext()) {
                PsiFileSystemItem psiItem = psiFileSystemItemIterator.next();
                if (!(psiItem instanceof PsiJavaFile)) continue;

                PsiJavaFile psiJavaFile = (PsiJavaFile) psiItem;
                String fileName = psiJavaFile.getName();
                fileName = fileName.substring(0, fileName.length() - 5);
                String fqn = psiJavaFile.getPackageName() + "." + fileName;
                if (usagesList.contains(fqn)) {
                    resolvedPsiElements.add(psiJavaFile);
                }
            }

            java.util.List<Usage> usages = new ArrayList<Usage>(usagesList.size());
            for (PsiJavaFile psiElement : resolvedPsiElements) {
                UsageInfo info = new UsageInfo(psiElement, psiElement.getClasses()[0].getNameIdentifier().getTextRange().getStartOffset(), psiElement.getClasses()[0].getNameIdentifier().getTextRange().getStartOffset());
                usages.add(new UsageInfo2UsageAdapter(info));
            }

            return usages.toArray(new Usage[usages.size()]);
        }

        java.util.List<String> queryNeo4j() {
            GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(myProject.getBasePath() + "/" + neo4jPath.getText())
                    .setConfig(GraphDatabaseSettings.read_only, "true")
                    .newGraphDatabase();

            java.util.List<String> usages = new ArrayList<String>();
            try {
                Transaction tx = graphDb.beginTx();
                ExecutionEngine engine = new ExecutionEngine(graphDb);
                ExecutionResult result = engine.execute(textArea.getText());
                try {
                    Iterator<Node> n_column = result.columnAs("n");
                    for (Node node : IteratorUtil.asIterable(n_column)) {
                        try {
                            if(node.hasLabel(new Label() {
                                @Override
                                public String name() {
                                    return "Class";
                                }
                            })) {
                                usages.add((String) node.getProperty("fqn"));
                            }
                        } catch(org.neo4j.graphdb.NotFoundException e) {
                        }
                    }
                } catch(org.neo4j.cypher.EntityNotFoundException e1) {
                    show("No column named 'n' found; you should return a node with label 'CLASS' and name 'n', e.g. match (c:CLASS) return c LIMIT 10");
                }

                tx.success();
            } finally {
                graphDb.shutdown();
            }

            return usages;
        }

        void openFindTool(Usage[] theUsages) {
            // may help: https://gist.github.com/dkandalov/7248184
            // custom search adapter. https://gist.github.com/dkandalov/5956923
            UsageTarget[] usageTargets = new UsageTarget[1];
            usageTargets[0] =
                    new PsiElement2UsageTargetAdapter(PsiManager.getInstance(myProject).findDirectory(myProject.getBaseDir()));

            final UsageView view = UsageViewManager.getInstance(myProject).showUsages(usageTargets, theUsages, createPresentation());
        }

        private UsageViewPresentation createPresentation() {
            final UsageViewPresentation presentation = new UsageViewPresentation();
            presentation.setScopeText("jqa scope");
            presentation.setTabText("Classes");
            presentation.setToolwindowTitle("Classes from jqa");
            presentation.setUsagesString("files");
            presentation.setOpenInNewTab(true);
            presentation.setCodeUsages(true);
            return presentation;
        }
    }

}
