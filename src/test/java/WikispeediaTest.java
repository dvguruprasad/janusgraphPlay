import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WikispeediaTest {

    private static GraphRepo graphRepo = new GraphRepo(WikispeediaConfig.create());

    @BeforeAll
    public static void setup() throws InterruptedException {
        graphRepo.build();
    }

    @Test
    void articleLinks() {
        String articleName = "10th_century";
        Wikispeedia wikispeedia = new Wikispeedia(graphRepo);
        List<String> linkedArticles = wikispeedia.linksFrom(articleName);
        assertEquals(26, linkedArticles.size());
    }

    @Test
    void articleCategory() {
        String category = "subject.History.General_history";
        Wikispeedia wikispeedia = new Wikispeedia(graphRepo);
        List<String> linkedArticles = wikispeedia.articlesBelongingTo(category);
        assertEquals(27, linkedArticles.size());
    }
}
