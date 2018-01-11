package de.kontext_e.idea.plugins.jqa;

import java.util.List;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FindInNeo4jDatabaseActionIT {

    private FindInNeo4jDatabaseAction findInNeo4jDatabaseAction;

    private JTextArea textArea = new JTextArea();
    private JTextField textField = new JTextField();

//    @BeforeClass
    public static void prepareTestDatabase() {
        CreateTestDatabase.createTestDatabase();
    }

    @Before
    public void setUp() {
        findInNeo4jDatabaseAction = new FindInNeo4jDatabaseAction(textArea, textField, null);
    }

    @Test
    public void queryNeo4jForClasses() throws Exception {
        List<JqaClassFqnResult> fqns = findInNeo4jDatabaseAction.queryNeo4j("test/store", "match (n:Class) return n");

        assertThat(fqns.size(), is(1));
    }

    @Test
    public void queryNeo4jForMethods() throws Exception {
        List<JqaClassFqnResult> fqns = findInNeo4jDatabaseAction.queryNeo4j("test/store", "match (n:Method) return n");

        assertThat(fqns.size(), is(1));
        assertThat(((JqaMethod)fqns.get(0)).getClassFqn(), is(CreateTestDatabase.class.getName()));
    }

    @Test
    public void queryNeo4jForClassesAndMethods() throws Exception {
        List<JqaClassFqnResult> fqns = findInNeo4jDatabaseAction.queryNeo4j("test/store", "match (n:Class), (m:Method) return n,m LIMIT 10");

        assertThat(fqns.size(), is(2));
        assertThat(((JqaClass)fqns.get(0)).getClassFqn(), is(CreateTestDatabase.class.getName()));
        assertThat(((JqaMethod)fqns.get(1)).getClassFqn(), is(CreateTestDatabase.class.getName()));
    }

}
