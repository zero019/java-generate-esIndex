package generate.base;

/**
 * @author zeronly 2023/7/19
 */
public enum BaseParam {
    TYPE("type"),
    MAPPINGS("mappings"),
    SETTINGS("settings"),
    PROPERTIES("properties"),
    FIELDS("fields"),
    FORMAT("format"),
    OBJECT("object"),

    ALIASES("aliases"),
    NESTED("nested");


    public final String name;

    BaseParam(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }
}
