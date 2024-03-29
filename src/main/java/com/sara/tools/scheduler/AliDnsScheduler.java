package com.sara.tools.scheduler;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.AddDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeSubDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

@Component
public class AliDnsScheduler {

    private static final Logger log = LoggerFactory.getLogger(AliDnsScheduler.class);

    @Resource
    private AliDnsConfigs aliDnsConfigs;

    private String lastIpv6;

    @PostConstruct
    public void init() {
        //启动后先执行一次
        aliDdnsUpdate();
    }

    @Scheduled(cron = "${ali-dns.cron}")
    public void aliDdnsUpdate(){
        log.info("DNS更新任务启动");
        String ipv6 = getLocalIPv6Address();
        if(ipv6 != null && !Objects.equals(ipv6, lastIpv6)){
            log.info("IPV6地址变更，进行地址更新");
            //获取会变动的前缀
            String[] split = ipv6.split(":");
            String prefix = String.format("%s:%s:%s:%s:", split[0], split[1], split[2], split[3]);
            for(AliDnsConfig config : aliDnsConfigs.getRecords()){
                String updateIpv6 = StringUtils.isEmpty(config.getPostfix()) ? ipv6 : prefix + config.getPostfix();

                String regionId = "cn-hangzhou"; //必填固定值，必须为“cn-hanghou”
                IClientProfile profile = DefaultProfile.getProfile(regionId, config.getAccessKeyId(), config.getAccessKeySecret());
                // 若报Can not find endpoint to access异常，请添加以下此行代码
                // DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Alidns", "alidns.aliyuncs.com");
                IAcsClient client = new DefaultAcsClient(profile);

                DescribeSubDomainRecordsRequest request = new DescribeSubDomainRecordsRequest();
                request.setSubDomain(config.getHost());
                DescribeSubDomainRecordsResponse response;
                try {
                    response = client.getAcsResponse(request);
                    List<DescribeSubDomainRecordsResponse.Record> list = response.getDomainRecords();
                    DescribeSubDomainRecordsResponse.Record aliyunRecord = null;
                    for (DescribeSubDomainRecordsResponse.Record record : list) {
                        if("AAAA".equals(record.getType())){
                            aliyunRecord = record;
                            break;
                        }
                    }
                    if(aliyunRecord != null){
                        if(Objects.equals(aliyunRecord.getValue(), updateIpv6)){
                            log.info("记录没有变化，ipv6: {}", updateIpv6);
                        } else {
                            log.info("记录有变化，进行更新，原有ipv6：{}， 新的ipv6： {}", aliyunRecord.getValue(), updateIpv6);
                            UpdateDomainRecordRequest updateRequest = new UpdateDomainRecordRequest();
                            updateRequest.setRecordId(aliyunRecord.getRecordId());
                            updateRequest.setRR(aliyunRecord.getRR());
                            updateRequest.setValue(updateIpv6);
                            updateRequest.setType("AAAA");
                            client.getAcsResponse(updateRequest);
                        }
                    } else {
                        log.info("没有ipv6记录，开始插入，ipv6: {}", updateIpv6);
                        int rrIndex = config.getHost().indexOf(".");
                        String rr = config.getHost().substring(0, rrIndex);
                        String domainName= config.getHost().substring(rrIndex+1);
                        AddDomainRecordRequest addRequest = new AddDomainRecordRequest();
                        addRequest.setRR(rr);
                        addRequest.setDomainName(domainName);
                        addRequest.setValue(updateIpv6);
                        addRequest.setType("AAAA");
                        client.getAcsResponse(addRequest);
                    }
                    lastIpv6 = ipv6;
                } catch (ServerException e) {
                    log.error(e.getMessage(), e);
                } catch (ClientException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private String getLocalIPv6Address(){
        InetAddress inetAddress = null;
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAds = networkInterfaces.nextElement().getInetAddresses();
                while (inetAds.hasMoreElements()) {
                    inetAddress = inetAds.nextElement();
                    //Check if it's ipv6 address and reserved address
                    if (inetAddress instanceof Inet6Address) {
                        String ipv6 = inetAddress.getHostAddress();
                        if(!ipv6.startsWith("0:") && !ipv6.startsWith("fe80:")){
                            //过滤0开头的本机地址和fe80开头的内网地址
                            return ipv6;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
