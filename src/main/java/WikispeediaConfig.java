import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class WikispeediaConfig {
    private Properties properties;

    public WikispeediaConfig(Properties properties) {
        this.properties = properties;
    }

    static WikispeediaConfig create() {
        String wikiSpeediaPropertiesFile = "conf/wikispeedia.properties";
        InputStream resourceAsStream = Wikispeedia.class.getResourceAsStream(wikiSpeediaPropertiesFile);
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException("ERROR: Could not load properties file " + wikiSpeediaPropertiesFile);
        }
        return new WikispeediaConfig(properties);
    }

    public String get(String key){
        return properties.getProperty(key);
    }
}
