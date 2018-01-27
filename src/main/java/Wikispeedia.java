import java.util.List;

public class Wikispeedia {
    private GraphRepo graphRepo;

    public Wikispeedia(GraphRepo graphRepo) {
        this.graphRepo = graphRepo;
    }

    public List<String> linksFrom(String articleName) {
        return graphRepo.linksFrom(articleName);
    }
}
