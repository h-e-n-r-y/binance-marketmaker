package de.hw4.binance.marketmaker.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.hw4.binance.marketmaker.persistence.SchedulerTask;

@Component
public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    
    List<SchedulerTask> tasks = new ArrayList<>();

    @Scheduled(fixedRate = 10000)
    public void reportCurrentTime() {
        log.debug("The time is now {}", dateFormat.format(new Date()));
    }

}
