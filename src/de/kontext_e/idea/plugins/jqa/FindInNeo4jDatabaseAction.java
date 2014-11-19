package de.kontext_e.idea.plugins.jqa;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
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
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewManager;
import com.intellij.usages.UsageViewPresentation;

import static com.intellij.notification.NotificationType.INFORMATION;
import static liveplugin.PluginUtil.show;

class FindInNeo4jDatabaseAction extends AbstractAction {
    private Project myProject;
    JTextArea textArea;
    JTextField neo4jPath;

    public FindInNeo4jDatabaseAction(JTextArea textArea, JTextField neo4jPath, final Project project) {
        this.textArea = textArea;
        this.neo4jPath = neo4jPath;
        this.myProject = project;
    }

    public void actionPerformed(ActionEvent e) {
        openFindTool(resolvePsiElements(queryNeo4j()));
    }

    Usage[] resolvePsiElements(List<String> usagesList) {
        List<Usage> usages = new ArrayList<Usage>(usagesList.size());
        for (String classFqn : usagesList) {
            PsiClass psiClass = JavaPsiFacade.getInstance(myProject).findClass(classFqn, GlobalSearchScope.projectScope(myProject));
            if(psiClass.getContainingFile() instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiClass.getContainingFile();
                UsageInfo info = new UsageInfo(psiJavaFile, psiJavaFile.getClasses()[0].getNameIdentifier().getTextRange().getStartOffset(), psiJavaFile.getClasses()[0].getNameIdentifier().getTextRange().getStartOffset());
                usages.add(new UsageInfo2UsageAdapter(info));
            }
        }


        return usages.toArray(new Usage[usages.size()]);
    }

    List<String> queryNeo4j() {
        GraphDatabaseService graphDb = null;
        String path = myProject.getBasePath() + "/" + neo4jPath.getText();
        try {
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path)
                    .setConfig(GraphDatabaseSettings.read_only, "true")
                    .newGraphDatabase();
        } catch (Exception e) {
            String message = "Could not open Neo4j database at "+path;
            NotificationType notificationType = INFORMATION;
            Notification notification = new Notification("", "", message, notificationType);
            ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);

            return Collections.emptyList();
        }

        List<String> usages = new ArrayList<String>();
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
