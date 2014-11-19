package de.kontext_e.idea.plugins.jqa;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class CreateTestDatabase {

    public static void createTestDatabase() {
        String path = "jqassistant/store";
        GraphDatabaseService graphDb = null;
        try {
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path)
                    .newGraphDatabase();

            try ( Transaction tx = graphDb.beginTx(); )
            {
                ExecutionEngine engine = new ExecutionEngine(graphDb);
                engine.execute("START n = node(*) OPTIONAL MATCH n-[r]-() DELETE n, r");

                Node myNode = graphDb.createNode();
                myNode.addLabel(FindInNeo4jDatabaseAction.LABEL_CLASS);
                myNode.setProperty( "fqn", CreateTestDatabase.class.getName() );
                tx.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            graphDb.shutdown();
        }

    }
}
