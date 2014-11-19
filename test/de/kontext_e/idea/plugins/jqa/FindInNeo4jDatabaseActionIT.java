package de.kontext_e.idea.plugins.jqa;

import java.util.List;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public void queryNeo4j() throws Exception {
        List<String> fqns = findInNeo4jDatabaseAction.queryNeo4j("jqassistant/store", "match (n) return n");

        assertThat(fqns.size(), Matchers.is(1));
    }

}
