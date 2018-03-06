package de.hw4.binance.marketmaker.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface NotificationRepository extends CrudRepository<Notification, Long>{

	List<Notification> findByUserAndTimestampGreaterThan(String pUsername, Long pTimestamp); 

}
