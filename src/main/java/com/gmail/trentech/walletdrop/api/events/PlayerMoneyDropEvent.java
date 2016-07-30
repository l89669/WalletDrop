package com.gmail.trentech.walletdrop.api.events;

import java.util.List;

import org.spongepowered.api.event.cause.Cause;

import com.gmail.trentech.walletdrop.api.MoneyStack;
/*
 * Triggered when player dies and drops money
 */
public class PlayerMoneyDropEvent extends MoneyDropEvent {

	protected double playerLossAmount;

	public PlayerMoneyDropEvent(List<MoneyStack> moneyStacks, boolean specialDrop, Cause cause) {
		super(moneyStacks, specialDrop, cause);
		
		for (int i = 0; i < moneyStacks.size(); i++) {
			this.playerLossAmount += moneyStacks.get(i).getValue();
		}
	}

	/*
	 *  Gets the amount of money the player will drop
	 */
	public double getPlayerLossAmount() {
		return playerLossAmount;
	}

	/*
	 * Set the amount of money the player will lose
	 */
	public void setPlayerLossAmount(double playerLossAmount) {
		this.playerLossAmount = playerLossAmount;
	}
}
