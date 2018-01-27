import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WikispeediaTest {

    private final GraphRepo graphRepo = new GraphRepo(new WikispeediaConfig());

    @BeforeClass
    void initialize() throws InterruptedException {
        graphRepo.setupGraph();
    }

    @Test
    void articleLinks() {
        String articleName = "10th_century";
        Wikispeedia wikispeedia = new Wikispeedia(graphRepo);
        List<String> linkedArticles = wikispeedia.linksFrom(articleName);
        assertEquals(26, linkedArticles.size());
    }
}
