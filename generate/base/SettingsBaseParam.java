package generate.base;

/**
 * @author zeronly 2023/7/20
 */
public enum SettingsBaseParam {
    NUMBEROFSHARDS("number_of_shards"),
    INDEX("index"),
    NUMBEROFREPLICAS("number_of_replicas");

    public final String name;

    SettingsBaseParam(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
