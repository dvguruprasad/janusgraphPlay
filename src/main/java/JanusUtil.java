import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.core.schema.SchemaStatus;
import org.janusgraph.graphdb.database.management.ManagementSystem;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class JanusUtil {
    /*
     *
     * killAllTransaction terminates all the open connections to the graph
     *
     */
    public void killAllTransaction(JanusGraph graph) {
        JanusGraphManagement mgmt = graph.openManagement();
        Set<String> openInstances = mgmt.getOpenInstances();
        for (String instance : openInstances) {
            try {
                mgmt.forceCloseInstance(instance);
            } catch (IllegalArgumentException _) {
                graph.tx().commit();
                graph.tx().close();
            }
        }
    }

    /*
     *
     * deleteAllVertices removes all the vertices from the graph
     *
     */
    public void deleteAllVertices(JanusGraph graph) {
        GraphTraversal<Vertex, Vertex> v = graph.traversal().V();
        while (v.hasNext()) {
            Vertex next = v.next();
            next.remove();
        }
        graph.tx().commit();
        graph.tx().close();
    }

    /*
     *
     * enableIndex enables an already installed index. It does not create the index
     *
     */
    public void enableIndex(JanusGraph graph, String indexName) {
        try {
            // Block until the SchemaStatus transitions from INSTALLED to REGISTERED
            ManagementSystem.awaitGraphIndexStatus(graph, indexName).status(SchemaStatus.REGISTERED).call();

            // Enable the index using JanusGraphManagement
            JanusGraphManagement mgmt = graph.openManagement();
            JanusGraphIndex index = mgmt.getGraphIndex(indexName);
            mgmt.updateIndex(index, SchemaAction.ENABLE_INDEX).get();
            mgmt.commit();
            graph.tx().commit();

            // Block until the SchemaStatus transitions from REGISTERED to ENABLED
            ManagementSystem.awaitGraphIndexStatus(graph, indexName).status(SchemaStatus.ENABLED).call();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    /*
     *
     * disableIndex disables an already enabled composite index.
     * A mixed index must be manually dropped from the indexing backend.
     *
     */

    public void disableIndex(JanusGraph graph, String indexName) {
        try {
            JanusGraphManagement mgmt = graph.openManagement();
            JanusGraphIndex graphIndex = mgmt.getGraphIndex(indexName);
            mgmt.updateIndex(graphIndex, SchemaAction.DISABLE_INDEX).get();
            mgmt.commit();
            graph.tx().commit();

            // Block until the SchemaStatus transitions from ENABLED to DISABLE
            ManagementSystem.awaitGraphIndexStatus(graph, indexName).status(SchemaStatus.DISABLED).call();

            // Remove the index using JanusGraphManagement
            mgmt = graph.openManagement();
            JanusGraphIndex deleteIndex = mgmt.getGraphIndex(indexName);
            mgmt.updateIndex(deleteIndex, SchemaAction.REMOVE_INDEX).get();
            mgmt.commit();
            graph.tx().commit();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
