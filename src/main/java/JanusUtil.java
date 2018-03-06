import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphException;
import org.janusgraph.core.schema.JanusGraphManagement;

import java.util.Set;

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
            } catch (JanusGraphException _) {
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

}
