package de.hw4.binance.marketmaker.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.hw4.binance.marketmaker.Status;
import de.hw4.binance.marketmaker.Trader;
import de.hw4.binance.marketmaker.persistence.SchedulerTask;
import de.hw4.binance.marketmaker.persistence.SchedulerTaskRepository;

@Component
public class Scheduler {

    private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    @Autowired
    SchedulerTaskRepository tasksRepo;
    
    @Autowired
    Trader trader;

    @Scheduled(fixedRate = 10000)
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
        
        List<SchedulerTask> activeTasks = tasksRepo.findByActive(true);
        for (SchedulerTask task : activeTasks) {
        		Status status = trader.trade(task);
        		log.info("Task ({}, {}: {})", task.getUser(), task.getSymbol(), status);
        }
    }

}
