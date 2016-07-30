package com.gmail.trentech.walletdrop.api.events;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.walletdrop.api.MoneyStack;

/*
 * Triggered when Entity dies and drops money
 */
public class MoneyDropEvent extends AbstractEvent implements Cancellable {

	protected Living entity;
	protected Location<World> dropLocation;
	protected List<MoneyStack> moneyStacks;
	protected boolean specialDrop;
	protected Cause cause;
	protected boolean cancelled = false;

	public MoneyDropEvent(List<MoneyStack> moneyStacks, boolean specialDrop, Cause cause) {
		Optional<Living> optionalLiving = cause.first(Living.class);
		
		if(optionalLiving.isPresent()) {
			entity = optionalLiving.get();
			dropLocation = entity.getLocation();
		}
		
		this.moneyStacks = moneyStacks;
		this.specialDrop = specialDrop;
		this.cause = cause;
	}

	/*
	 * Gets a List of MoneyStack's 
	 */
	public List<MoneyStack> getMoneyStacks() {
		return moneyStacks;
	}

	/*
	 * Gets the Entity that caused this event.
	 * 
	 * Can be null.
	 */
	public Living getEntity() {
		return entity;
	}

	/*
	 * Gets the location money will drop
	 * 
	 * Can be null;
	 */
	public Location<World> getLocation() {
		return dropLocation;
	}

	/*
	 * Set the location money will drop
	 */
	public void setLocation(Location<World> dropLocation) {
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
