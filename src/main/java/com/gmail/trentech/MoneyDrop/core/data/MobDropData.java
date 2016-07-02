package com.gmail.trentech.MoneyDrop.core.data;

public class MobDropData {

	private double dropPercent;
	private double min;
	private double max;

	public MobDropData() {
	}

	public MobDropData(double dropPercent, double min, double max) {
		this.dropPercent = dropPercent;
		setMin(min);
		setMax(max);
		minmaxCheck();
	}

	private void minmaxCheck() {
		if (min > max) {
			max = min;
		}
	}

	public double getDropPercent() {
		return dropPercent;
	}

	public void setDropPercent(double dropPercent) {
		this.dropPercent = dropPercent;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		if (min < 0) {
			min = 0;
		}
		this.min = min;
		minmaxCheck();
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		if (max < 0) {
			max = 0;
		}
		this.max = max;
		minmaxCheck();
	}
}
