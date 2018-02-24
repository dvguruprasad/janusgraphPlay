import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.graphdb.database.management.ManagementSystem;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class GraphRepo {
    private static final String ARTICLE_NAME = "articleName";
    private static final String CATEGORY = "category";
    private final WikispeediaConfig config;

    public GraphRepo(WikispeediaConfig config) {
        this.config = config;
    }

    private JanusGraph load() {
        return JanusGraphFactory.open(config.getConfigPath());
    }

    public void build() throws InterruptedException {
        JanusGraph graph = load();
        JanusGraphManagement janusGraphManagement = graph.openManagement();
        buildSchema(janusGraphManagement);
        buildIndices(janusGraphManagement);
        janusGraphManagement.commit();

        createArticleVertices(graph);
        createLinkEdges(graph);
        List<List<String>> categoryRows = categoryData();
        createCategoriesVertices(graph, categoryRows);
        createCategoriesEdges(graph, categoryRows);
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

    private void createCategoriesEdges(JanusGraph graph, List<List<String>> categoryRows) {
        for (List<String> categoryPair : categoryRows) {
            String article = categoryPair.get(0);
            String category = categoryPair.get(1);
            Vertex articleVertex = graph.traversal().V().has(ARTICLE_NAME, article).next();
            Vertex categoryVertex = graph.traversal().V().has(CATEGORY, category).next();
            articleVertex.addEdge("belongsTo", categoryVertex);
        }
        graph.tx().commit();
    }

    private void buildSchema(final JanusGraphManagement janusGraphManagement) {
        janusGraphManagement.makeEdgeLabel("linksTo").make();
        janusGraphManagement.makePropertyKey(ARTICLE_NAME).dataType(String.class).cardinality(Cardinality.SINGLE).make();
        janusGraphManagement.makeEdgeLabel("belongsTo").make();
        janusGraphManagement.makePropertyKey(CATEGORY).dataType(String.class).cardinality(Cardinality.SET).make();
    }

    private void buildIndices(final JanusGraphManagement janusGraphManagement) {
        PropertyKey articleNamePropertyKey = janusGraphManagement.getPropertyKey(ARTICLE_NAME);
        janusGraphManagement.buildIndex("byArticleName", Vertex.class).addKey(articleNamePropertyKey).buildCompositeIndex();
        PropertyKey categoryPropertyKey = janusGraphManagement.getPropertyKey(CATEGORY);
        janusGraphManagement.buildIndex("categories", Vertex.class).addKey(categoryPropertyKey).buildCompositeIndex();

    }

    private void createArticleVertices(JanusGraph graph) throws InterruptedException {
        Set<String> articleNames = articleNames();
        for (String articleName : articleNames) {
            graph.addVertex(ARTICLE_NAME, articleName, "timestamp", System.currentTimeMillis());
        }
        graph.tx().commit();

        ManagementSystem.awaitGraphIndexStatus(graph, "byArticleName").call();
    }

    private void createCategoriesVertices(JanusGraph graph, List<List<String>> categories) throws InterruptedException {
        for (List<String> categoryData : categories) {
            graph.addVertex(CATEGORY, categoryData.get(1), "timestamp", System.currentTimeMillis());
        }
        graph.tx().commit();

        ManagementSystem.awaitGraphIndexStatus(graph, "categories").call();
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

    private List<List<String>> categoryData() {
        List<List<String>> categoryData;
        try {
            categoryData = IO.readFile(config.get("data.category.file"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred in reading file " + config.get("data.articles.file") + ": " + e);
        }
        return categoryData.stream().skip(0).limit(500).collect(Collectors.toList());
    }

    public List<String> linksFrom(String articleName) {
        JanusGraph graph = load();
        Vertex sourceVertex = graph.traversal().V().has(ARTICLE_NAME, articleName).next();
        Iterator<Edge> edges = sourceVertex.edges(Direction.OUT, "linksTo");
        List<String> linksFromSourceVertex = new ArrayList<>();
        while (edges.hasNext()) {
            Edge linkTo = edges.next();
            Vertex vertex = linkTo.inVertex();
            linksFromSourceVertex.add((String) vertex.property(ARTICLE_NAME).value());
        }
        return linksFromSourceVertex;
    }

    public List<String> articlesBelongingTo(String category) {
        JanusGraph graph = load();
        Vertex sourceVertex = graph.traversal().V().has(CATEGORY, category).next();
        Iterator<Edge> edges = sourceVertex.edges(Direction.IN, "belongsTo");
        List<String> linksFromSourceVertex = new ArrayList<>();
        while (edges.hasNext()) {
            Edge linkTo = edges.next();
            Vertex vertex = linkTo.outVertex();
            linksFromSourceVertex.add((String) vertex.property(ARTICLE_NAME).value());
        }
        return linksFromSourceVertex;
    }
}
