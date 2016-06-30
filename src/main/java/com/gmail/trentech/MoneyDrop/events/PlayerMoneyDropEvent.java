package com.gmail.trentech.MoneyDrop.events;

import org.spongepowered.api.event.entity.DestructEntityEvent;

import com.gmail.trentech.MoneyDrop.MoneyStack;

public class PlayerMoneyDropEvent extends MoneyDropEvent {

	protected double playerLossAmount;

	public PlayerMoneyDropEvent(DestructEntityEvent.Death event, MoneyStack moneyStack, boolean specialDrop) {
		super(event, moneyStack, specialDrop);

		playerLossAmount = moneyStack.getValue();
	}

	public double getPlayerLossAmount() {
		return playerLossAmount;
	}

	public void setPlayerLossAmount(double playerLossAmount) {
		this.playerLossAmount = playerLossAmount;
	}
}
