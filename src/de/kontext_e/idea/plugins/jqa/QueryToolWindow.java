package de.kontext_e.idea.plugins.jqa;

import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
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

        JTextField tmpStoreTextField;
        JTextArea matchNCLASSReturnTextArea;
        JButton button1;

        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setAlignmentX(0.5f);
        final JLabel label1 = new JLabel();
        label1.setText("Database Location");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        tmpStoreTextField = new JTextField();
        tmpStoreTextField.setText("jqassistant/store");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(tmpStoreTextField, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Query");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel1.add(label2, gbc);
        matchNCLASSReturnTextArea = new JTextArea();
        matchNCLASSReturnTextArea.setText("match (n:Class) return n LIMIT 10");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(matchNCLASSReturnTextArea, gbc);
        button1 = new JButton();
        button1.setLabel("Find");
        button1.setText("Find");
        button1.addActionListener(new MyFindAction(matchNCLASSReturnTextArea, tmpStoreTextField, project));

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(button1, gbc);

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
