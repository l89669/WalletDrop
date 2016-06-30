package com.gmail.trentech.MoneyDrop.events;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.MoneyDrop.MoneyStack;

public class MoneyDropEvent extends AbstractEvent implements Cancellable {

	protected Living entity;
	protected Location<World> dropLocation;
	protected MoneyStack moneyStack;
	protected DestructEntityEvent.Death sourceEvent;
	protected boolean specialDrop;
	protected boolean cancelled = false;

	public MoneyDropEvent(DestructEntityEvent.Death event, MoneyStack moneyStack, boolean specialDrop) {
		if (event != null) {
			entity = event.getTargetEntity();
			dropLocation = entity.getLocation();
		}
		this.moneyStack = moneyStack;
		sourceEvent = event;
		this.specialDrop = specialDrop;
	}

	public DestructEntityEvent.Death getSourceEvent() {
		return sourceEvent;
	}

	public MoneyStack getMoneyStack() {
		return moneyStack;
	}

	public Living getEntity() {
		return entity;
	}

	public Location<World> getDropLocation() {
		return dropLocation;
	}

	public void setDropLocation(Location<World> dropLocation) {
		this.dropLocation = dropLocation;
	}

	public boolean isSpecialDrop() {
		return specialDrop;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public Cause getCause() {
		return null;
	}

}
