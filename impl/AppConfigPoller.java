package com.cc.sync.appconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

@Slf4j
@Component
public class AppConfigPoller {

    @Autowired
    private AppConfigsService appConfigsService;

    @Autowired
    private AppConfigRequestService appConfigRequestService;

    @Autowired
    private AppConfigPollingTask appConfigPollingTask;

    private static Future<String> future = null;

    @PostConstruct
    public void init() {
        pollAppConfigData();
    }

    public synchronized void pollAppConfigData(){
        if(future != null){
            future.cancel(true);
        }
        String token = appConfigRequestService.getToken();
        future = appConfigPollingTask.pollAppConfigData(token);
    }



}
