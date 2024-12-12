package com.cc.sync.appconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cc.sync.appconfig.model.sqs.AppConfigEventMsg;
import com.cc.sync.appconfig.model.sqs.Type;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;

@Slf4j
@Component
public class AppConfigListener {

    public static final int MAX_POLLS = 10;

    @Autowired
    private AppConfigsService appConfigsService;

    @Autowired
    private AppConfigRequestService appConfigRequestService;

    @Autowired
    private AppConfigPoller appConfigPoller;


    @SqsListener("${awsappconfig.sqs-queue-name}")
    public void recieveMessage(String stringJson) throws InterruptedException {
        AppConfigEventMsg msg = parseEventMsg(stringJson);
        if(!appConfigRequestService.isTargetAppConfigData(msg.getApplication().getId(), msg.getEnvironment().getId(), msg.getConfigurationProfile().getId())){
            log.info("Event not for this environment");
            return;
        }

        // events are ON_DEPLOYMENT_START, ON_DEPLOYMENT_STEP, ON_DEPLOYMENT_BAKING, ON_DEPLOYMENT_ROLLED_BACK, ON_DEPLOYMENT_COMPLETE

        // Each time a new event arrives a new async polling session is started
        // if a polling session is already running then it is cancelled and replaced by the new polling sessions.
        // polling sessions will end when no data is returned (as per appconfig docs)

        appConfigPoller.pollAppConfigData();

    }


    private AppConfigEventMsg parseEventMsg(String stringJson){
        log.info("Event Message: " + stringJson);
        AppConfigEventMsg msg = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            msg = objectMapper.readValue(stringJson, AppConfigEventMsg.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return msg;
    }


}
