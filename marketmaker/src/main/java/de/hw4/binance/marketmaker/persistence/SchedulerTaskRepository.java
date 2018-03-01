package de.hw4.binance.marketmaker.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SchedulerTaskRepository extends CrudRepository<SchedulerTask, Long> {
	
	List<SchedulerTask> findByUser(String pUsername); 

}
