package de.kontext_e.idea.plugins.jqa;

import java.util.List;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FindInNeo4jDatabaseActionIT {

    private FindInNeo4jDatabaseAction findInNeo4jDatabaseAction;

    private JTextArea textArea = new JTextArea();
    private JTextField textField = new JTextField();

    @BeforeClass
    public static void prepareTestDatabase() {
        CreateTestDatabase.createTestDatabase();
    }

    @Before
    public void setUp() {
        findInNeo4jDatabaseAction = new FindInNeo4jDatabaseAction(textArea, textField, null);
    }

    @Test
    public void queryNeo4jForClasses() throws Exception {
        List<JqaClassFqnResult> fqns = findInNeo4jDatabaseAction.queryNeo4j("jqassistant/store", "match (n:Class) return n");

        assertThat(fqns.size(), is(1));
    }

    @Test
    public void queryNeo4jForMethods() throws Exception {
        List<JqaClassFqnResult> fqns = findInNeo4jDatabaseAction.queryNeo4j("jqassistant/store", "match (n:Method) return n");

        assertThat(fqns.size(), is(1));
        assertThat(fqns.get(0).getClassFqn(), is(CreateTestDatabase.class.getName()));
    }

}
