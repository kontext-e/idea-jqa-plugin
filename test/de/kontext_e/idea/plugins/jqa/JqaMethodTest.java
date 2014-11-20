package de.kontext_e.idea.plugins.jqa;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JqaMethodTest {

    @Test
    public void extractClassFqnFromSignature() throws Exception {
        String signature = getClass().getName()+" extractClassFqnFromSignature(ToolWindowPanelBuilder.Numbers numbers)";
        String classFqn = JqaMethod.classFqnOfMethod(signature);

        assertThat(classFqn, is(getClass().getName()));
    }

}
