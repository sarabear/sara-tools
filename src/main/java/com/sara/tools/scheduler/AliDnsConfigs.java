package com.sara.tools.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "ali-dns")
@Component
public class AliDnsConfigs {

    private List<AliDnsConfig> records;

    public List<AliDnsConfig> getRecords() {
        return records;
    }

    public void setRecords(List<AliDnsConfig> records) {
        this.records = records;
    }
}
