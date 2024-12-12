package com.cc.sync.appconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cc.sync.appconfig.observers.AppConfigUpdateObserver;
import com.cc.sync.appconfig.model.data.AppConfigs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AppConfigsService {

    @Autowired
    private AppConfigRequestService appConfigRequestService;

    @Autowired(required = false)
    private List<AppConfigUpdateObserver> appConfigUpdateObservers = new ArrayList<>();

    private AppConfigs appConfigs = new AppConfigs();

    // returns true if there was data to update
    public boolean processConfigData(byte[] data, String utf8Data)  {
        log.info("** Checking Feature Flag State**");
        if(data.length > 0){
            log.info("** Feature Flag state update detected **");

            setAppConfigs(utf8Data);

            log.debug("=================================");
            try {
                log.debug(new ObjectMapper().readTree(utf8Data).toPrettyString());
            } catch (JsonProcessingException e) {
                log.error("Couldn't parse response");
            }
            log.debug("=================================");

            // notify observers if registered
            for(AppConfigUpdateObserver appConfigUpdateObserver: appConfigUpdateObservers){
                appConfigUpdateObserver.apply(getAppConfigs());
            }
            return true;
        }
        return false;
    }

    public AppConfigs getAppConfigs(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // return a deep copy
            return  objectMapper.readValue(objectMapper.writeValueAsString(appConfigs), AppConfigs.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void setAppConfigs(String appConfigJson){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            appConfigs = objectMapper.readValue(appConfigJson, AppConfigs.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            if(appConfigs==null){
                log.warn("AppConfig in unexpected state... setting to default values");
                appConfigs = new AppConfigs();
            }
        }
    }
}
