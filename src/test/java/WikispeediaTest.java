import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WikispeediaTest {

    private final GraphRepo graphRepo = new GraphRepo(WikispeediaConfig.create());

    @BeforeEach
    void setup() throws InterruptedException {
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
