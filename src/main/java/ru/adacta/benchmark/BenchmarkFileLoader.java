package ru.adacta.benchmark;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkFileLoader {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkFileLoader.class);

    public static List<BenchmarkParams> load(String filename) throws IOException {

        List<BenchmarkParams> result = new ArrayList<>();

        logger.info(String.format("Loading benchmarks from the file %s", filename));

        try {
            InputStream is = new FileInputStream(filename);
            String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);

            (new JSONArray(jsonTxt)).forEach(j -> {
                JSONObject jobj = (JSONObject) j;
                result.add(new BenchmarkParams(jobj.getString("name"),
                        jobj.getInt("chatsNumber"),
                        jobj.getInt("messageExchangeCyclesNumber"),
                        jobj.getInt("messageIntervalMillis"),
                        jobj.getInt("latchTimeout")
                ));
            });

            logger.info(String.format("Loaded %d benchmarks", result.size()));

        } catch (Exception e) {
            logger.error(String.format("Socket error: %s \n", e.getMessage()), e);
            throw e;
        }

        return result;
    }

}
