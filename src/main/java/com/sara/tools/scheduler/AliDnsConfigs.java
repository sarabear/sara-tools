package com.sara.tools.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "")
@Component
public class AliDnsConfigs {

    private List<AliDnsConfig> aliDns;

    public List<AliDnsConfig> getAliDns() {
        return aliDns;
    }

    public void setAliDns(List<AliDnsConfig> aliDns) {
        this.aliDns = aliDns;
    }
}
