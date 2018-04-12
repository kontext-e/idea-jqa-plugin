package de.kontext_e.idea.plugins.jqa;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class FindInNeo4jDatabaseActionTest {
	@Test
	public void escape() {
		assertThat(FindInNeo4jDatabaseAction.escape(""), is(""));
		assertThat(FindInNeo4jDatabaseAction.escape("{name: \"FindInNeo4jDatabaseAction\"}) "), is("{name: \\\"FindInNeo4jDatabaseAction\\\"}) "));
	}

}
