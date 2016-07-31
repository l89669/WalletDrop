package com.gmail.trentech.walletdrop.api.events;

import java.util.List;

import org.spongepowered.api.event.cause.Cause;

import com.gmail.trentech.walletdrop.api.MoneyStack;
/*
 * Triggered when player dies and drops money
 */
public class PlayerMoneyDropEvent extends MoneyDropEvent {

	protected double dropAmount;

	public PlayerMoneyDropEvent(List<MoneyStack> moneyStacks, Cause cause) {
		super(moneyStacks, cause);
		
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
