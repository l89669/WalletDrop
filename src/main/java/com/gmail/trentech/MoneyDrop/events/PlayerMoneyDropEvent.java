package com.gmail.trentech.MoneyDrop.events;

import java.util.List;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DestructEntityEvent;

import com.gmail.trentech.MoneyDrop.data.MoneyStack;

public class PlayerMoneyDropEvent extends MoneyDropEvent {

	protected double playerLossAmount;

	public PlayerMoneyDropEvent(DestructEntityEvent.Death event, List<MoneyStack> moneyStacks, boolean specialDrop, Cause cause) {
		super(event, moneyStacks, specialDrop, cause);
		for (int i = 0; i < moneyStacks.size(); i++) {
			this.playerLossAmount += moneyStacks.get(i).getValue();
		}
	}

	public double getPlayerLossAmount() {
		return playerLossAmount;
	}

	public void setPlayerLossAmount(double playerLossAmount) {
		this.playerLossAmount = playerLossAmount;
	}
}
