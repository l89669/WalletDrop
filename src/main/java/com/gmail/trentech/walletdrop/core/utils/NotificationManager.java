package com.gmail.trentech.walletdrop.core.utils;

import java.util.HashMap;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import com.gmail.trentech.walletdrop.Main;
import com.gmail.trentech.walletdrop.core.consumers.Notification;

public class NotificationManager {
	
	private HashMap<UUID, Notification> notifications = new HashMap<>();

	public void addNotification(Player player, double amount) {
		UUID uuid = player.getUniqueId();

		Settings settings = Settings.get(player.getWorld());

		if (!settings.isNotificationDelay()) {
			Notification notification = new Notification(amount, uuid);
			notification.sendNotification();
		} else {
			if(notifications.containsKey(uuid)) {
				Notification notification = notifications.get(uuid);				
				notification.addAmount(amount);
			} else {
				Notification notification = new Notification(amount, uuid);
				Task.builder().delayTicks(30).execute(notification).submit(Main.getInstance().getPlugin());

				notifications.put(uuid, notification);
			}
		}
	}

	public void unregisterNotification(UUID uuid) {
		notifications.remove(uuid);
	}
}
