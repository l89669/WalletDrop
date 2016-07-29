package com.gmail.trentech.walletdrop.core.data;

import java.util.function.Consumer;

import org.spongepowered.api.scheduler.Task;

public class DropsPerSecond implements Consumer<Task> {
	
	private int drops = 0;
	private int droplimit;
	
	public DropsPerSecond(int droplimit) {
		this.droplimit = droplimit;
	}
	
	public boolean isAtDropLimit() {
		return (drops >= droplimit && droplimit != 0);
	}
	
	public void add() {
		drops++;
	}

	@Override
	public void accept(Task task) {
		drops = 0;
	}

	public int getDrops() {
		return drops;
	}

	public void setDrops(int drops) {
		this.drops = drops;
	}

	public int getDroplimit() {
		return droplimit;
	}

	public void setDroplimit(int droplimit) {
		this.droplimit = droplimit;
	}
}
