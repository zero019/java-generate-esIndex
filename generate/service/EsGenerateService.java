package generate.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zeronly 2023/7/19
 */
public interface EsGenerateService {
    /**
     * 生成的ES Properties表
     * @param clazz
     * @return
     */
    JSONObject getProperties(Class clazz);

    /**
     * 生成ES索引表
     * @param clazz
     * @return
     */
    String generateRes(Class clazz) throws ClassNotFoundException;
}
