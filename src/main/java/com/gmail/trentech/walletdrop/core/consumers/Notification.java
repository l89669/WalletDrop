package com.gmail.trentech.walletdrop.core.consumers;

import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.gmail.trentech.walletdrop.Main;
import com.gmail.trentech.walletdrop.core.utils.Settings;

public class Notification implements Consumer<Task> {
	
	private double amount;
	private UUID uuid;

	public Notification(double amount, UUID uuid) {
		this.amount = amount;
		this.uuid = uuid;
	}

	public void addAmount(double amount) {
		this.amount += amount;
	}

	public double getAmount() {
		return this.amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public void sendNotification() {
		Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(uuid);

		if (optionalPlayer.isPresent()) {
			Player player = optionalPlayer.get();

			Settings settings = Settings.get(player.getWorld());

			if (settings.isPickupChatNotification()) {
				String message = settings.getPickupChatMessage();
				
				double money;
				if (amount % 1 == 0) {
					money = amount;
				} else {
					money = (amount * 1000) / 1000.0;
				}

				message = message.replaceAll("<money>", new DecimalFormat("#,###,##0.00").format(money));
				player.sendMessage(settings.getChatType(), TextSerializers.FORMATTING_CODE.deserialize(message));
			}
		}

		Main.instance().getNotificationManager().unregisterNotification(uuid);
	}

	@Override
	public void accept(Task arg0) {
		sendNotification();
	}
}