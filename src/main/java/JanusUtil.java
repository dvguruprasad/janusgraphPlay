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
}
