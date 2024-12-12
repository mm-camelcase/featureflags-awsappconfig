package com.workhuman.sync.appconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;



@Slf4j
@Component
public class AppConfigPollingTask {

    @Autowired
    private AppConfigsService appConfigsService;

    @Autowired
    private AppConfigRequestService appConfigRequestService;

    private static String token = null;

    @Async
    public Future<String> pollAppConfigData(String token){
        // todo: add a max poll limit

        boolean keepPolling = true;
        GetLatestConfigurationResponse response = null;

        log.info("****************** start polling ****************** ");

        while(keepPolling){
            log.info("****************** polling ****************** ");
            response = appConfigRequestService.getConfigData(token);
            keepPolling = appConfigsService.processConfigData(response.configuration().asByteArray(), response.configuration().asUtf8String());
            if(!keepPolling) break;
            token = response.nextPollConfigurationToken();

            try {
                TimeUnit.SECONDS.sleep(response.nextPollIntervalInSeconds());
            } catch (InterruptedException e) {
                log.info("****************** polling stopped early ****************** ");
                Thread.currentThread().interrupt();
            }
        }

        log.info("****************** polling done ****************** ");
        return CompletableFuture.completedFuture("Done");
    }




}
