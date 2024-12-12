package com.cc.sync.appconfig;

import com.cc.sync.appconfig.observers.AppConfigUpdateObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
//@Configuration
//@EnableScheduling
// polling turned off, using sqs listener instead
@Deprecated
public class AwsAppConfigScheduler {

    private static String APPCONFIG_REGION_STATIC;

    @Value("${awsappconfig.region}")
    public void setNameStatic(String appConfigRegion){
        AwsAppConfigScheduler.APPCONFIG_REGION_STATIC = appConfigRegion;
    }

    private static String sessionToken = null;

    @Autowired
    private AppConfigsService appConfigsService;

    @Value("${awsappconfig.appId}")
    private String applicationIdentifier;

    @Value("${awsappconfig.envId}")
    private String environmentIdentifier;

    @Value("${awsappconfig.configProfileId}")
    private String configurationProfileIdentifier;

    @Autowired(required = false)
    private List<AppConfigUpdateObserver> appConfigUpdateListeners = new ArrayList<>();

    private static AppConfigDataClient dataClient;

    public AwsCredentialsProvider awsCredentialsProvider(){
        // this will do for dev & local for now (local will require AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY and AWS_SESSION_TOKEN env vars to be set)
        return DefaultCredentialsProvider.create();
    }

    @PostConstruct
    public void init() throws InterruptedException {
        dataClient = AppConfigDataClient
                .builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(APPCONFIG_REGION_STATIC))
                .build();

        // https://docs.aws.amazon.com/appconfig/2019-10-09/APIReference/API_appconfigdata_StartConfigurationSession.html
        sessionToken = dataClient.startConfigurationSession(
            StartConfigurationSessionRequest.builder()
                    .applicationIdentifier(applicationIdentifier)
                    .environmentIdentifier(environmentIdentifier)
                    .configurationProfileIdentifier(configurationProfileIdentifier)
                    .build()).initialConfigurationToken();

        log.debug("init Token: " + sessionToken);
    }

    /*@Scheduled(fixedRate = 30000) // 30 secs
    public void pollConfiguration() throws JsonProcessingException {
        log.info("polling AWS AppConfig for updates");

        // https://docs.aws.amazon.com/appconfig/2019-10-09/APIReference/API_appconfigdata_GetLatestConfiguration.html
        GetLatestConfigurationResponse response = dataClient.getLatestConfiguration(
                GetLatestConfigurationRequest.builder()
                        .configurationToken(sessionToken)
                        .build());

        log.debug("checking response for AWS AppConfig");
        if(response.configuration().asByteArray().length > 0){
            log.info("** Feature Flag state update detected **");

            appConfigsService.setAppConfigs(response.configuration().asUtf8String());

            // notify listeners if registered
            for(AppConfigUpdateObserver appConfigUpdateListener:appConfigUpdateListeners){
                appConfigUpdateListener.apply(appConfigsService.getAppConfigs());
            }

            log.debug("=================================");
            log.debug(new ObjectMapper().readTree(response.configuration().asUtf8String()).toPrettyString());
            log.debug("=================================");
        }

        // Need to re-assign the next token returned
        sessionToken = response.nextPollConfigurationToken();
    }*/

}
