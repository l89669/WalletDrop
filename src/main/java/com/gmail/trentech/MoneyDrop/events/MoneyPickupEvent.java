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
	protected boolean cancelled = false;

	public MoneyPickupEvent(Player player, ItemStack sourceItem, double value) {
		this.player = player;
		this.item = sourceItem;
		this.value = value;
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
		// TODO Auto-generated method stub
		return null;
	}

}
