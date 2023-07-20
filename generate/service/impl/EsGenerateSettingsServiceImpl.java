package es.generate.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.meipingmi.center.datasync.util.es.generate.annotation.Settings;
import com.meipingmi.center.datasync.util.es.generate.base.SettingsBaseParam;
import com.meipingmi.center.datasync.util.es.generate.service.EsGenerateSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zeronly 2023/7/20
 */
@Service
@Slf4j
public class EsGenerateSettingsServiceImpl implements EsGenerateSettingsService {

    String rawSettings =
            "{" + "\n" +
            "\"index\" :{\n" +
            "    \"number_of_shards\" : \"5\",\n" +
            "    \"analysis\" : {\n" +
            "      \"filter\" : {\n" +
            "        \"pinyin_full_filter\" : {\n" +
            "          \"lowercase\" : \"true\",\n" +
            "          \"keep_original\" : \"false\",\n" +
            "          \"keep_first_letter\" : \"false\",\n" +
            "          \"keep_separate_first_letter\" : \"false\",\n" +
            "          \"type\" : \"pinyin\",\n" +
            "          \"limit_first_letter_length\" : \"50\",\n" +
            "          \"keep_full_pinyin\" : \"true\"\n" +
            "        },\n" +
            "        \"my_pinyin\" :{\n" +
            "          \"lowercase\" : \"true\",\n" +
            "          \"keep_original\" : \"false\",\n" +
            "          \"keep_first_letter\" : \"true\",\n" +
            "          \"keep_separate_first_letter\" : \"true\",\n" +
            "          \"type\" : \"pinyin\",\n" +
            "          \"limit_first_letter_length\" : \"16\",\n" +
            "          \"keep_full_pinyin\" : \"true\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"analyzer\" : {\n" +
            "        \"pinyin_full_analyzer\" : {\n" +
            "          \"filter\" : [\n" +
            "            \"pinyin_full_filter\"\n" +
            "          ],\n" +
            "          \"tokenizer\" : \"ngram_tokenizer\"\n" +
            "        },\n" +
            "        \"ngram_analyzer\":{\n" +
            "          \"filter\" : [\n" +
            "            \"lowercase\"\n" +
            "          ],\n" +
            "          \"tokenizer\" : \"ngram_tokenizer\"\n" +
            "        },\n" +
            "        \"pinyin_analyzer\" : {\n" +
            "          \"filter\" : [\n" +
            "            \"my_pinyin\"\n" +
            "          ],\n" +
            "          \"tokenizer\" : \"ngram_tokenizer\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"tokenizer\" : {\n" +
            "        \"ngram_tokenizer\" : {\n" +
            "          \"token_chars\" :[ ],\n" +
            "          \"min_gram\" : \"1\",\n" +
            "          \"type\" : \"ngram\",\n" +
            "          \"max_gram\" : \"1\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"number_of_replicas\" : \"0\"" +
            "}";

    JSONObject SETTINGS = JSONObject.parseObject(rawSettings.replaceAll("\\s+", ""));


    @Override
    public JSONObject getSettings(Settings settings) {
        String numberOfReplicas = String.valueOf(settings.numberOfReplicas());
        getIndex(settings);
        SETTINGS.put(SettingsBaseParam.NUMBEROFREPLICAS.name, numberOfReplicas);
        return SETTINGS;
    }

    @Override
    public JSONObject getIndex(Settings settings) {
        String numberOfShards = String.valueOf(settings.numberOfShards());
        SETTINGS.getJSONObject(SettingsBaseParam.INDEX.name).put(SettingsBaseParam.NUMBEROFSHARDS.name, numberOfShards);
        return SETTINGS;
    }

    @Override
    public JSONObject getDefaultSettings() {
        return SETTINGS;
    }
}
