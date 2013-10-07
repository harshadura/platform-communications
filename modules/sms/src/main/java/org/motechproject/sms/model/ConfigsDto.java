package org.motechproject.sms.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

/**
 * todo
 */
public class ConfigsDto {
    private String defaultConfigName;
    private List<Config> configs;

    public ConfigsDto() {
    }

    @JsonIgnore
    public Config getDefaultConfig() {
        return getConfig(defaultConfigName);
    }

    public String getDefaultConfigName() {
        return defaultConfigName;
    }

    public void setDefaultConfigName(String defaultConfig) {
        this.defaultConfigName = defaultConfig;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }

    public Config getConfig(String name) {
        for(Config config : configs) {
            if (config.getName().equals(name)) {
                return config;
            }
        }
        throw new IllegalArgumentException("'" + name + "': no such config");
    }
}
