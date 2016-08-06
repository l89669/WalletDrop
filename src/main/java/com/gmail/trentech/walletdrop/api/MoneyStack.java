package com.gmail.trentech.walletdrop.api;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.gmail.trentech.walletdrop.core.manipulators.MoneyData;
import com.gmail.trentech.walletdrop.core.manipulators.MoneyDataManipulatorBuilder;

public class MoneyStack {

	private ItemStack item;
	private double value;

	public MoneyStack(ItemStack item, double value) {
		this.item = item;
		this.value = (value * 1000) / 1000.0;
	}

	public void drop(Location<World> location) {
		MoneyDataManipulatorBuilder builder = (MoneyDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(MoneyData.class).get();
		MoneyData data = builder.createFrom(value);

		ItemStack money = item.copy();
		money.offer(data);

		Item item = (Item) location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
		item.offer(Keys.REPRESENTED_ITEM, money.createSnapshot());
		
		location.getExtent().spawnEntity(item, Cause.of(NamedCause.source(EntitySpawnCause.builder().entity(item).type(SpawnTypes.PLUGIN).build())));
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		value = (value * 1000) / 1000.0;
		this.value = value;
	}
}
