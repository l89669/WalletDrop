package com.gmail.trentech.walletdrop.api.events;

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.item.inventory.ItemStack;

/*
 * Triggered when Player picks up money
 */
public class MoneyPickupEvent extends AbstractEvent implements Cancellable {

	protected ItemStack item;
	protected double value;
	protected Cause cause;
	protected boolean cancelled = false;

	public MoneyPickupEvent(ItemStack sourceItem, double value, Cause cause) {
		this.item = sourceItem;
		this.value = value;
		this.cause = cause;
	}

	/*
	 * Get the source ItemStack that represents money
	 */
	public ItemStack getItem() {
		return item;
	}

	/*
	 * Gets the amount of money player is picking up
	 */
	public double getValue() {
		return value;
	}

	/*
	 * Sets the amount of money player will receive
	 */
	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public Cause getCause() {
		return cause;
	}

}
