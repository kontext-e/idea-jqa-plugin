package de.kontext_e.idea.plugins.jqa;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.notification.Notification;
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
import static com.intellij.notification.NotificationType.INFORMATION;

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

            String normalizedQueryString = queryString;
            normalizedQueryString = normalizedQueryString
                    .replaceAll("\n","")
                    .replaceAll("\t","")
            ;

            final String txUri = serverUri + "transaction/commit";
            WebResource resource = Client.create().resource(txUri );

            String payload = "{\"statements\" : [ {\"statement\" : \"" + normalizedQueryString + "\"} ]}";
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
            showInfoBubble(message);
            response.close();

            /////////////////////////////

            final JsonFactory jsonFactory = new JsonFactory();
            jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
            jsonFactory.disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            JsonParser p = jsonFactory.createParser(responseEntity);
            p.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

            boolean isFqn = false;
            boolean isName = false;
            boolean isSignature = false;
            boolean isRelativePath = false;
            String name = "";
            while(!p.isClosed()){
                JsonToken jsonToken = p.nextToken();
                if(jsonToken == JsonToken.FIELD_NAME) {
                    final String value = p.getCurrentName();
                    if("fqn".equalsIgnoreCase(value)) {
                        isFqn = true;
                    }
                    if("relativePath".equalsIgnoreCase(value)) {
                        isRelativePath = true;
                    }
                    if("signature".equalsIgnoreCase(value)) {
                        isSignature = true;
                    }
                    if("name".equalsIgnoreCase(value)) {
                        isName = true;
                    }
                }
                if(isFqn && jsonToken == JsonToken.VALUE_STRING) {
                    jqaResults.add(new JqaClass(p.getValueAsString()));
                    isFqn = false;
                }
                if(isRelativePath && jsonToken == JsonToken.VALUE_STRING) {
                    jqaResults.add(new JqaRelativePathFile(p.getValueAsString()));
                    isRelativePath = false;
                }
                if(isSignature && jsonToken == JsonToken.VALUE_STRING) {
/*
deactivated because I currently dont know how to get all classes of the project
to look for methods with matching signature
any hints welcome
                    final JqaMethod jqaMethod = new JqaMethod(name, p.getValueAsString());
                    jqaResults.add(jqaMethod);
*/
                    isSignature = false;
                    name = "";
                }
                if(isName && jsonToken == JsonToken.VALUE_STRING) {
                    name = p.getValueAsString();
                    isName = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Exception occured for database with path "+path+":  "+ e.toString();
            showErrorBubble(message);
        }

        return jqaResults;
    }

    private static void showErrorBubble(final String message) {
        Notification notification = new Notification("", "", message, ERROR);
        if(ApplicationManager.getApplication() != null) {
            ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
        }
    }

    private static void showInfoBubble(final String message) {
        Notification notification = new Notification("", "", message, INFORMATION);
        if(ApplicationManager.getApplication() != null) {
            ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
        }
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
