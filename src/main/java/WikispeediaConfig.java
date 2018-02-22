import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    public String getConfigPath() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL fileURI = classLoader.getResource(get("janusGraph.properties.file"));
        assert fileURI != null;
        return new File(fileURI.getFile()).getPath();
    }


    public String get(String key){
        return properties.getProperty(key);
    }
}
