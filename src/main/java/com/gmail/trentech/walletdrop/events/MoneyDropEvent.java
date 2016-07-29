package com.gmail.trentech.walletdrop.events;

import java.util.List;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.walletdrop.data.MoneyStack;

public class MoneyDropEvent extends AbstractEvent implements Cancellable {

	protected Living entity;
	protected Location<World> dropLocation;
	protected List<MoneyStack> moneyStacks;
	protected DestructEntityEvent.Death sourceEvent;
	protected boolean specialDrop;
	protected Cause cause;
	protected boolean cancelled = false;

	public MoneyDropEvent(DestructEntityEvent.Death event, List<MoneyStack> moneyStacks, boolean specialDrop, Cause cause) {
		if (event != null) {
			entity = event.getTargetEntity();
			dropLocation = entity.getLocation();
		}
		this.moneyStacks = moneyStacks;
		sourceEvent = event;
		this.specialDrop = specialDrop;
		this.cause = cause;
	}

	public DestructEntityEvent.Death getSourceEvent() {
		return sourceEvent;
	}

	public List<MoneyStack> getMoneyStacks() {
		return moneyStacks;
	}

	public Living getEntity() {
		return entity;
	}

	public Location<World> getLocation() {
		return dropLocation;
	}

	public void setDropLocation(Location<World> dropLocation) {
		this.dropLocation = dropLocation;
	}

	public boolean isSpecialDrop() {
		return specialDrop;
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
