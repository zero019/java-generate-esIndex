package generate.service.impl;

import com.alibaba.fastjson.JSONObject;
import generate.annotation.Aliases;
import generate.annotation.Settings;
import generate.base.BaseParam;
import generate.service.EsGenerateService;
import generate.service.EsGenerateSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;


/**
 * @author zeronly 2023/7/19
 */
@Service
@Slf4j
public class EsGenerateServiceImpl implements EsGenerateService {
    public static String MAIN = "";

    @Resource
    private EsGenerateSettingsService esGenerateSettingsService;

    @Override
    public String generateRes(Class clazz) throws ClassNotFoundException {
        Class<?> clazz1 = Class.forName(clazz.getName());
        JSONObject res = new JSONObject(new LinkedHashMap<>());
        JSONObject mappingJson = new JSONObject();
        JSONObject settingsJson = getSettings(clazz1);
        JSONObject aliasesJson = getAliases(clazz1);
        //这里mappings和properties其实可以整合成一个mappings
        JSONObject propertiesJson = getProperties(clazz1);

        mappingJson.put(BaseParam.PROPERTIES.name, propertiesJson);

        res.put(BaseParam.ALIASES.name, aliasesJson);
        res.put(BaseParam.MAPPINGS.name, mappingJson);
        res.put(BaseParam.SETTINGS.name, settingsJson);
        if (clazz1.isAnnotationPresent(Document.class)){
            Document document = clazz1.getAnnotation(Document.class);
            String headName = document.indexName();
            return "PUT" + " " + headName + "\n" + res.toJSONString();
        }else{
            log.error("class:{} is not annotated with @Document", clazz.getName());
            return res.toJSONString();
        }


    }

    /**
     * 生成别名JSON
     * @param clazz
     * @return
     */
    public JSONObject getAliases(Class<?> clazz){
        JSONObject aliasesJson = new JSONObject();
        if (clazz.isAnnotationPresent(Aliases.class)){
            Aliases aliases = clazz.getAnnotation(Aliases.class);
            aliasesJson.put(aliases.name(),new JSONObject());
        }else{
            aliasesJson.put(clazz.getSimpleName(),new JSONObject());
        }
        return aliasesJson;
    }

    public JSONObject getSettings(Class<?> clazz){
        if (clazz.isAnnotationPresent(Settings.class)){
            Settings settings = clazz.getAnnotation(Settings.class);
            return esGenerateSettingsService.getSettings(settings);
        }else{
            return esGenerateSettingsService.getDefaultSettings();
        }
    }

    /**
     * 生成的ES索引表，因为propertis和mappings在有嵌套nested字段下需要单独拆分两个结构，nested下没有mappings
     *
     * @param clazz
     * @return
     */
    @Override
    public JSONObject getProperties(Class clazz) {
        if(MAIN.isEmpty()){
            MAIN = clazz.getName();
        }
        //获取类下的字段
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        JSONObject propertiesJson = new JSONObject();
        for(java.lang.reflect.Field fieldWord : fields) {
            //解析字段注解
            if(fieldWord.isAnnotationPresent(Field.class)){
                fieldWord.setAccessible(true);
                JSONObject typeJson = getFieldAnnotationPresent(fieldWord);
                if(typeJson == null){
                    continue;
                }

                /*
                   1. 此时生成ES的json类似于如下格式:
                   "delivererId" : {
                      "type" : "long"
                    }
                 */
                propertiesJson.put(fieldWord.getName(), typeJson);
            }
        }
        return propertiesJson;
    }

    /**
     * warning：取出的泛型只取出第一个字段！
     * 获取当前字段的@field注解的type值，并返回Enum对应的值
     * 格式如下{BaseParam.TYPE : mapperName}
     * @param fieldWord
     */
    JSONObject getFieldAnnotationPresent(java.lang.reflect.Field fieldWord) {
        JSONObject res = new JSONObject();

        //获取解析字段@Field的下type的值,如果字段是FieldType nested类型，会进行嵌套执行
        FieldType fieldType = fieldWord.getAnnotation(Field.class).type();
        if(fieldType.equals(FieldType.Nested)){
            //判断是否为List等泛型类，需要取出泛型（只取出一个字段）
            Type type = fieldWord.getGenericType();
            if(type instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] types = parameterizedType.getActualTypeArguments();
                Class<?> clazz = (Class<?>) types[0];

                if(clazz.getName().equals(MAIN)){
                    return null;
                }else {
                    JSONObject nestedJson = new JSONObject();
                    nestedJson.put(BaseParam.TYPE.name,fieldType.getMappedName());
                    nestedJson.put(BaseParam.PROPERTIES.name, getProperties(clazz));
                    return nestedJson;
                }
            }else{
                if(((Class<?>) type).getName().equals(MAIN)){
                    return null;
                }
                //不是泛型的nested字段
                JSONObject nestedJson = new JSONObject();
                nestedJson.put(BaseParam.TYPE.name, fieldType.getMappedName());
                nestedJson.put(BaseParam.PROPERTIES.name, getProperties(fieldWord.getType()));
                return nestedJson;
            }
        }else if(fieldType.equals(FieldType.Object)) {
            if(fieldWord.getType().getName().equals(MAIN)){
                return null;
            }
            JSONObject objectJson = new JSONObject();
            objectJson.put(BaseParam.PROPERTIES.name, getProperties(fieldWord.getType()));
            return objectJson;
        }

        String mapperName = fieldType.getMappedName();
        res.put(BaseParam.TYPE.name, mapperName);
        return res;
    }

    String getMinClassName(Class clazz) {
        String[] nameSplit = clazz.getName().split("\\.");
        return nameSplit[nameSplit.length - 1];
    }
}
