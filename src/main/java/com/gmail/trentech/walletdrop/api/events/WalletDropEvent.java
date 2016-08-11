package com.gmail.trentech.walletdrop.api.events;

import java.util.List;

import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.walletdrop.api.MoneyStack;

/*
 * Triggered when Entity, that is not instanceof Player, dies and drops money
 */
public class WalletDropEvent extends AbstractEvent implements Cancellable {

	protected Living entity;
	protected Location<World> dropLocation;
	protected List<MoneyStack> moneyStacks;
	protected boolean specialDrop;
	protected Cause cause;
	protected boolean cancelled = false;

	public WalletDropEvent(List<MoneyStack> moneyStacks, Living entity) {
		this.entity = entity;
		this.dropLocation = entity.getLocation();
		this.moneyStacks = moneyStacks;
		this.cause = Cause.of(NamedCause.source(entity));
	}

	/*
	 * Gets a List of MoneyStack's 
	 */
	public List<MoneyStack> getMoneyStacks() {
		return moneyStacks;
	}

	/*
	 * Gets the Entity that caused this event.
	 */
	public Living getEntity() {
		return entity;
	}

	/*
	 * Gets the location money will drop
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
	
	public static class Player extends WalletDropEvent {

		protected double dropAmount;

		public Player(List<MoneyStack> moneyStacks, org.spongepowered.api.entity.living.player.Player player) {
			super(moneyStacks, player);
			
			for (int i = 0; i < moneyStacks.size(); i++) {
				this.dropAmount += moneyStacks.get(i).getValue();
			}
		}

		/*
		 *  Gets the amount of money the player will drop
		 */
		public double getDropAmount() {
			return dropAmount;
		}

		/*
		 * Set the amount of money the player will lose
		 */
		public void setDropAmount(double dropAmount) {
			this.dropAmount = dropAmount;
		}
	}
}
