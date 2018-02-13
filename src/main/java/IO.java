import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class IO {
    static List<List<String>> readFile(final String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(WikispeediaConfig.create().get("wikispeedia.dataset.path") + fileName));
        List<List<String>> data = new ArrayList<>();
        String row;
        while ((row = reader.readLine()) != null) {
            row = row.trim();
            if(row.isEmpty() || row.startsWith("#"))
                continue;
            List<String> strings = asList(row.split("\\t"));
            strings = strings.stream().map(s -> {
                try {
                    return java.net.URLDecoder.decode(s, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return "";
                }
            }).collect(Collectors.toList());
            data.add(strings);
        }
        reader.close();
        return data;
    }
}
