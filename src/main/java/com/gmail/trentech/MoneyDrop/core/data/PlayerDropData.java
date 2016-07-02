package com.gmail.trentech.MoneyDrop.core.data;

public class PlayerDropData {

	private double[] amount = new double[10];
	private boolean[] percent = new boolean[10];
	private double precision;

	public PlayerDropData(double precision) {
		this.precision = precision;
	}

	public double getDropAmount(MDDeathReason reason, double playerBalance) {
		if (playerBalance <= 0) {
			return 0;
		}

		int reasonIndex = reason.getIndex();

		double dropAmount = amount[reasonIndex];

		if (percent[reasonIndex]) {
			double drop = (playerBalance * dropAmount * 10.0);
			return ((drop - (drop % (precision * 1000))) / 1000);
		} else {
			if (dropAmount >= 0) {
				if (playerBalance >= dropAmount) {
					return dropAmount;
				} else {
					return (int) playerBalance;
				}
			} else {
				if (playerBalance >= dropAmount) {
					return (playerBalance + dropAmount);
				} else {
					return 0;
				}
			}
		}
	}

	public boolean setDeathAmount(String amount, MDDeathReason reason) {
		int index = reason.getIndex();
		String amnt = amount;
		boolean pct = false;

		if (amount.charAt(amount.length() - 1) == '%') {
			pct = true;
			amnt = amount.substring(0, amount.length() - 1);
		}

		try {
			double parsed = Double.parseDouble(amnt);
			if (pct) {
				if (parsed < 100) {
					parsed = Math.abs(parsed);
				}
				if (parsed > 100) {
					parsed = 100;
				}
			}

			this.amount[index] = parsed;
			this.percent[index] = pct;

			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public String getDeathAmount(MDDeathReason reason) {
		if (percent[reason.getIndex()]) {
			return amount + "%";
		} else {
			return amount + "";
		}
	}

	public enum MDDeathReason {
		MOB_ATTACK(0, "mob-attack"), 
		PLAYER_ATTACK(1, "player-attack"), 
		BLOCK_EXPLOSION(2, "block-explosion"), 
		BLOCK_CONTACT(3, "block-contact"), 
		DROWNING(4, "drowning"), 
		FALLING(5, "falling"), 
		FIRE(6, "fire"), 
		SUFFOCATION(7, "suffocation"), 
		SUICIDE(8, "suicide"), 
		OTHER(9, "other");

		private int index;
		private String name;

		private MDDeathReason(int index, String name) {
			this.index = index;
			this.name = name;
		}

		protected int getIndex() {
			return index;
		}

		public String getName() {
			return name;
		}
	}
}
