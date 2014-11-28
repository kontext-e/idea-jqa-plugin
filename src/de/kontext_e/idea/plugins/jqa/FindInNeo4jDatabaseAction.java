package de.kontext_e.idea.plugins.jqa;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
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
import com.intellij.usages.UsageViewManager;
import com.intellij.usages.UsageViewPresentation;

import static com.intellij.notification.NotificationType.ERROR;

class FindInNeo4jDatabaseAction extends AbstractAction {
    public static final Label LABEL_CLASS = new Label() {
        @Override
        public String name() {
            return "Class";
        }
    };
    public static final Label LABEL_METHOD = new Label() {
        @Override
        public String name() {
            return "Method";
        }
    };

    private Project myProject;
    private JTextArea textArea;
    private JTextField neo4jPath;

    public FindInNeo4jDatabaseAction(JTextArea textArea, JTextField neo4jPath, final Project project) {
        this.textArea = textArea;
        this.neo4jPath = neo4jPath;
        this.myProject = project;
    }

    public void actionPerformed(ActionEvent e) {
        String databasePath = myProject.getBasePath() + "/" + neo4jPath.getText();
        openFindTool(resolvePsiElements(queryNeo4j(databasePath, textArea.getText())));
    }

    void openFindTool(Usage[] theUsages) {
        UsageTarget[] usageTargets = new UsageTarget[] {
                new PsiElement2UsageTargetAdapter(PsiManager.getInstance(myProject).findDirectory(myProject.getBaseDir()))
        };

        UsageViewManager.getInstance(myProject).showUsages(usageTargets, theUsages, createPresentation());
    }

    Usage[] resolvePsiElements(List<JqaClassFqnResult> usagesList) {
        List<Usage> usages = new ArrayList<>(usagesList.size());
        for (JqaClassFqnResult classFqnResult : usagesList) {
            PsiClass psiClass = JavaPsiFacade.getInstance(myProject).findClass(classFqnResult.getClassFqn(), GlobalSearchScope.projectScope(myProject));
            if(psiClass.getContainingFile() instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiClass.getContainingFile();
                int classNameStartOffset = classFqnResult.calculateOffset(psiJavaFile);
                UsageInfo info = new UsageInfo(psiJavaFile, classNameStartOffset, classNameStartOffset);
                usages.add(new UsageInfo2UsageAdapter(info));
            }
        }


        return usages.toArray(new Usage[usages.size()]);
    }

    List<JqaClassFqnResult> queryNeo4j(final String path, final String queryString) {
        GraphDatabaseService graphDb = null;
        final List<JqaClassFqnResult> jqaResults = new ArrayList<>();
        try {
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path)
                    .setConfig(GraphDatabaseSettings.read_only, "true")
                    .newGraphDatabase();

            Transaction tx = graphDb.beginTx();
            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(queryString);
            readFqnsFromResult(result, jqaResults);
            tx.success();

        } catch (Exception e) {
            String message = "Exception occured for database with path "+path+":  "+ e.toString();
            showErrorBubble(message);
        } finally {
            if(graphDb != null) {
                graphDb.shutdown();
            }
        }

        return jqaResults;
    }

    private void readFqnsFromResult(final ExecutionResult result, final List<JqaClassFqnResult> jqaResults) {
        List<String> columnNames = result.columns();
        for (String columnName : columnNames) {
            Iterator<Node> column = result.columnAs(columnName);
            for (Node node : IteratorUtil.asIterable(column)) {
                ifNodeIsClassReadFqnProperty(node, jqaResults);
            }
        }
    }

    private void ifNodeIsClassReadFqnProperty(final Node node, final List<JqaClassFqnResult> jqaResults) {
        try {
            if(JqaMethod.isResponsibleFor(node)) {
                jqaResults.add(new JqaMethod(node));
            }

            if(JqaClass.isResponsibleFor(node)) {
                jqaResults.add(new JqaClass(node));
            }
        } catch(Exception e) {
            // TODO find out how to write a message into messages tool window
        }
    }

    private void showErrorBubble(final String message) {
        NotificationType notificationType = ERROR;
        Notification notification = new Notification("", "", message, notificationType);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
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
