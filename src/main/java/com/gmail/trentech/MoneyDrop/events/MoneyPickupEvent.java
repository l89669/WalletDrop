package com.gmail.trentech.MoneyDrop.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.item.inventory.ItemStack;

public class MoneyPickupEvent extends AbstractEvent implements Cancellable {

	protected Player player;
	protected ItemStack item;
	protected double value;
	protected Cause cause;
	protected boolean cancelled = false;

	public MoneyPickupEvent(Player player, ItemStack sourceItem, double value, Cause cause) {
		this.player = player;
		this.item = sourceItem;
		this.value = value;
		this.cause = cause;
	}

	public Player getPlayer() {
		return player;
	}

	public ItemStack getItem() {
		return item;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public Cause getCause() {
		return cause;
	}

}
