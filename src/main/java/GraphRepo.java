import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.graphdb.database.management.ManagementSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class GraphRepo {
    private static final String ARTICLE_NAME = "articleName";
    private final WikispeediaConfig config;

    public GraphRepo(WikispeediaConfig config) {
        this.config = config;
    }

    public void setupGraph() throws InterruptedException {
        JanusGraph graph = load();
        JanusGraphManagement janusGraphManagement = graph.openManagement();
        makeSchema(janusGraphManagement);
        buildIndices(janusGraphManagement);
        janusGraphManagement.commit();

        createArticleVertices(graph);
        createLinkEdges(graph);
    }

    private void createLinkEdges(JanusGraph graph) {
        List<List<String>> articleLinks;
        try {
            articleLinks = IO.readFile(config.get("data.links.file"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred in reading file " + config.get("data.links.file") + ": " + e);
        }

        articleLinks = articleLinks.stream().skip(0).limit(500).collect(Collectors.toList());
        for (List<String> articleLink : articleLinks) {
            String sourceArticle = articleLink.get(0);
            String linkedArticle = articleLink.get(1);
            Vertex sourceArticleVertex = graph.traversal().V().has(ARTICLE_NAME, sourceArticle).next();
            Vertex linkedArticleVertex = graph.traversal().V().has(ARTICLE_NAME, linkedArticle).next();
            sourceArticleVertex.addEdge("linksTo", linkedArticleVertex);
        }

        graph.tx().commit();
    }

    private void makeSchema(final JanusGraphManagement janusGraphManagement) {
        janusGraphManagement.makeEdgeLabel("linksTo").make();
        janusGraphManagement.makePropertyKey(ARTICLE_NAME).dataType(String.class).cardinality(Cardinality.SINGLE).make();
    }

    private void buildIndices(final JanusGraphManagement janusGraphManagement) {
        PropertyKey articleNamePropertyKey = janusGraphManagement.getPropertyKey(ARTICLE_NAME);
        janusGraphManagement.buildIndex("byArticleName", Vertex.class).addKey(articleNamePropertyKey).buildCompositeIndex();
    }

    private void createArticleVertices(JanusGraph graph) throws InterruptedException {
        Set<String> articleNames = articleNames();
        for (String articleName : articleNames) {
            graph.addVertex(ARTICLE_NAME, articleName, "timestamp", System.currentTimeMillis());
        }
        graph.tx().commit();

        ManagementSystem.awaitGraphIndexStatus(graph, "byArticleName").call();
    }
    private Set<String> articleNames() {
        List<List<String>> articleData;
        try {
            articleData = IO.readFile(config.get("data.articles.file"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred in reading file " + config.get("data.articles.file") + ": " + e);
        }
        return articleData.stream().map(row -> row.get(0)).collect(Collectors.toSet());
    }

    public List<String> linksFrom(String articleName) {
        JanusGraph graph = load();
        Vertex sourceVertex = graph.traversal().V().has(ARTICLE_NAME, articleName).next();
        Iterator<Edge> edges = sourceVertex.edges(Direction.OUT);
        List<String> linksFromSourceVertex = new ArrayList<>();
        while (edges.hasNext()) {
            Edge linkTo = edges.next();
            Vertex vertex = linkTo.inVertex();
            linksFromSourceVertex.add((String) vertex.property(ARTICLE_NAME).value());
        }
        return linksFromSourceVertex;
    }

    private JanusGraph load() {
        return JanusGraphFactory.open(config.getConfigPath());
    }
}
