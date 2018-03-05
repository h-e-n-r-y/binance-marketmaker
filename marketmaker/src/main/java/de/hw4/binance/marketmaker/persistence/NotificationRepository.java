package de.hw4.binance.marketmaker.persistence;

import java.util.List;

public interface NotificationRepository {

	List<Notification> findByUserAndTimestampGreaterThan(String pUsername, Long pTimestamp); 

}
