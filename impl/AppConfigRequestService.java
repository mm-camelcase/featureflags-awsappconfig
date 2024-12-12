package com.cc.sync.appconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;

@Slf4j
@Component
public class AppConfigRequestService {

    @Value("${awsappconfig.appId}")
    private String applicationIdentifier;

    @Value("${awsappconfig.envId}")
    private String environmentIdentifier;

    @Value("${awsappconfig.configProfileId}")
    private String configurationProfileIdentifier;

    @Autowired
    private AppConfigDataClient dataClient;


    public boolean isTargetAppConfigData(String applicationId, String environmentId, String configurationProfileId){
        if(!applicationIdentifier.equals(applicationId)){
            return false;
        }
        if(!environmentIdentifier.equals(environmentId)){
            return false;
        }
        if(!configurationProfileIdentifier.equals(configurationProfileId)){
            return false;
        }
        // will react to all event types for sqs (ON_DEPLOYMENT_START, ON_DEPLOYMENT_COMPLETE, ON_DEPLOYMENT_ROLLED_BACK)
        return true;
    }

    public String getToken(){

        log.info("appid:" + applicationIdentifier + " envid:" + environmentIdentifier + " profid:" + configurationProfileIdentifier);

        // https://docs.aws.amazon.com/appconfig/2019-10-09/APIReference/API_appconfigdata_StartConfigurationSession.html
        String sessionToken = dataClient.startConfigurationSession(
                StartConfigurationSessionRequest.builder()
                        .applicationIdentifier(applicationIdentifier)
                        .environmentIdentifier(environmentIdentifier)
                        .configurationProfileIdentifier(configurationProfileIdentifier)
                        .build()).initialConfigurationToken();

        return sessionToken;
    }

    public GetLatestConfigurationResponse getConfigData(String sessionToken){
        // https://docs.aws.amazon.com/appconfig/2019-10-09/APIReference/API_appconfigdata_GetLatestConfiguration.html
        GetLatestConfigurationResponse response = dataClient.getLatestConfiguration(
                GetLatestConfigurationRequest.builder()
                        .configurationToken(sessionToken)
                        .build());
        return response;
    }


}
