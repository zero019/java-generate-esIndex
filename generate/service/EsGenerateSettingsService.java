package generate.service;

import com.alibaba.fastjson.JSONObject;
import com.meipingmi.center.datasync.util.es.generate.annotation.Settings;

/**
 * @author zeronly 2023/7/20
 */
public interface EsGenerateSettingsService {
    JSONObject getSettings(Settings settings);

    JSONObject getIndex(Settings settings);

    JSONObject getDefaultSettings();
}
