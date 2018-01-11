package de.kontext_e.idea.plugins.jqa;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageViewManager;
import com.intellij.usages.UsageViewPresentation;

import static com.intellij.notification.NotificationType.ERROR;

class FindInNeo4jDatabaseAction extends AbstractAction {

    private Project myProject;
    private JTextArea textArea;
    private JTextField neo4jPath;

    public FindInNeo4jDatabaseAction(JTextArea textArea, JTextField neo4jPath, final Project project) {
        this.textArea = textArea;
        this.neo4jPath = neo4jPath;
        this.myProject = project;
    }

    public void actionPerformed(ActionEvent e) {
        openFindTool(resolvePsiElements(queryNeo4j(neo4jPath.getText(), textArea.getText())));
    }

    void openFindTool(Usage[] theUsages) {
        try {
            UsageTarget[] usageTargets = new UsageTarget[] {
                    new PsiElement2UsageTargetAdapter(PsiManager.getInstance(myProject).findDirectory(myProject.getBaseDir()))
            };

            UsageViewManager.getInstance(myProject).showUsages(usageTargets, theUsages, createPresentation());
        } catch (Exception e) {
            String message = "Exception occured on opening Find Tool Window: "+ e;
            showErrorBubble(message);
        }
    }

    Usage[] resolvePsiElements(List<JqaClassFqnResult> usagesList) {
        try {
            List<Usage> usages = new ArrayList<>(usagesList.size());
            for (JqaClassFqnResult classFqnResult : usagesList) {
                UsageInfo info = classFqnResult.toUsageInfo(myProject);
                if(info != null) {
                    usages.add(new UsageInfo2UsageAdapter(info));
                }
            }

            return usages.toArray(new Usage[usages.size()]);
        } catch (Exception e) {
            String message = "Exception occured while resolving PSI elements: "+ e;
            showErrorBubble(message);
            return new Usage[0];
        }
    }

    List<JqaClassFqnResult> queryNeo4j(final String path, final String queryString) {
        final List<JqaClassFqnResult> jqaResults = new ArrayList<>();
        try {
            String serverUri = path;
            if(!serverUri.endsWith("/")) serverUri += "/";

            final String txUri = serverUri + "transaction/commit";
            WebResource resource = Client.create().resource(txUri );

            String payload = "{\"statements\" : [ {\"statement\" : \"" + queryString + "\"} ]}";
            ClientResponse response = resource
                    .accept(MediaType.APPLICATION_JSON )
                    .type(MediaType.APPLICATION_JSON )
                    .entity( payload )
                    .post( ClientResponse.class );

            final String responseEntity = response.getEntity(String.class);

            final String message = String.format(
                    "POST [%s] to [%s], status code [%d], returned data: "
                    + System.lineSeparator() + "%s",
                    payload, txUri, response.getStatus(),
                    responseEntity);
            showErrorBubble(message);
            response.close();

            /////////////////////////////

            final JsonFactory jsonFactory = new JsonFactory();
            jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
            jsonFactory.disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            JsonParser p = jsonFactory.createParser(responseEntity);
            p.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

            boolean isFqn = false;
            final Collection<String> fqns = new ArrayList<>();
            while(!p.isClosed()){
                JsonToken jsonToken = p.nextToken();
                if(jsonToken == JsonToken.FIELD_NAME) {
                    final String value = p.getCurrentName();
                    if("fqn".equalsIgnoreCase(value)) {
                        isFqn = true;
                    }
                }
                if(isFqn && jsonToken == JsonToken.VALUE_STRING) {
                    fqns.add(p.getValueAsString());
                    isFqn = false;
                }
            }
            System.out.println("FQNs: "+fqns);

            readFqnsFromResult(fqns, jqaResults);
        } catch (Exception e) {
            String message = "Exception occured for database with path "+path+":  "+ e.toString();
            showErrorBubble(message);
        }

        return jqaResults;
    }

    private void readFqnsFromResult(final Collection<String> result, final List<JqaClassFqnResult> jqaResults) {
        for ( String row : result )
        {
            jqaResults.add(new JqaClass(row));
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

            if(JqaRelativePathFile.isResponsibleFor(node)) {
                jqaResults.add(new JqaRelativePathFile(node));
            }
        } catch(Exception e) {
            // TODO find out how to write a message into messages tool window
        }
    }

    public static void showErrorBubble(final String message) {
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
