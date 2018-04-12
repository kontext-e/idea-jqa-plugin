package de.kontext_e.idea.plugins.jqa;

import java.util.List;
import javax.swing.JComboBox;
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
	private JComboBox queryHistory = new JComboBox();

	@Before
    public void setUp() {
        findInNeo4jDatabaseAction = new FindInNeo4jDatabaseAction(textArea, textField, queryHistory, null);
    }

    @Test
    public void queryNeo4jForClasses() throws Exception {
        List<JqaClassFqnResult> fqns = findInNeo4jDatabaseAction.queryNeo4j("http://localhost:7474/db/data/", "match (n:Class) where n.fqn ends with 'Action' return n");

        assertThat(fqns.size(), is(1));
    }
}
