package com.gmail.trentech.MoneyDrop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.world.World;

import com.gmail.trentech.MoneyDrop.data.ImmutableMoneyData;
import com.gmail.trentech.MoneyDrop.data.MoneyData;
import com.gmail.trentech.MoneyDrop.data.MoneyDataManipulatorBuilder;
import com.gmail.trentech.MoneyDrop.utils.ConfigManager;
import com.gmail.trentech.MoneyDrop.utils.Resource;
import com.gmail.trentech.MoneyDrop.utils.Settings;

import me.flibio.updatifier.Updatifier;

@Updatifier(repoName = "MoneyDrop", repoOwner = "TrenTech", version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, authors = Resource.AUTHOR, url = Resource.URL, dependencies = { @Dependency(id = "Updatifier", optional = true) })
public class MoneyDrop {

	private static Logger log;
	private static PluginContainer plugin;

	private static EconomyService economy;

	@Listener
	public void onPreInitializationEvent(GamePreInitializationEvent event) {
		plugin = Sponge.getPluginManager().getPlugin(Resource.ID).get();
		log = getPlugin().getLogger();
	}

	@Listener
	public void onInitializationEvent(GameInitializationEvent event) {
		Sponge.getDataManager().register(MoneyData.class, ImmutableMoneyData.class, new MoneyDataManipulatorBuilder());
	}

	@Listener
	public void onPostInitializationEvent(GamePostInitializationEvent event) {
		if (!setupEconomy()) {
			getLog().error("No economy plugin found. Disabling MoneyDrop...");
			return;
		}

		ConfigManager.get().init();

		Sponge.getEventManager().registerListeners(this, new EventListener());
	}

	@Listener
	public void onReloadEvent(GameReloadEvent event) {
		ConfigManager.get().init();

		for (World world : Sponge.getServer().getWorlds()) {
			Settings.initSettings(world);
		}
	}

	private boolean setupEconomy() {
		Optional<EconomyService> optionalEconomy = Sponge.getServiceManager().provide(EconomyService.class);

		if (optionalEconomy.isPresent()) {
			economy = optionalEconomy.get();
		}

		return optionalEconomy.isPresent();
	}

	static EconomyService getEconomy() {
		return economy;
	}

	public static Logger getLog() {
		return log;
	}

	public static PluginContainer getPlugin() {
		return plugin;
	}
	
	public static List<MoneyStack> createMoneyStacks(Settings settings, double amount) {
		List<MoneyStack> moneyStacks = new ArrayList<>();

		double split = settings.getMaxStackValue();

		ItemStack itemStack = ItemStack.builder().quantity(1).itemType(settings.getItemType()).build();
		
		int unsafeDamage = settings.getItemUnsafeDamage();
		
		if(unsafeDamage > 0) {
			DataContainer container = itemStack.toContainer();
			DataQuery query = DataQuery.of('/', "UnsafeDamage");
			container.set(query, unsafeDamage);
			
			itemStack = ItemStack.builder().fromContainer(container).build();
		}

		if (split != 0) {
			while (amount > 0) {
				if (amount > split) {
					moneyStacks.add(new MoneyStack(itemStack, split));
					amount -= split;
				} else {
					moneyStacks.add(new MoneyStack(itemStack, amount));
					amount = 0;
				}
			}
		} else {
			moneyStacks.add(new MoneyStack(itemStack, amount));
		}

		return moneyStacks;
	}

	public static double getItemStackValue(ItemStack itemStack) {
		Optional<MoneyData> optionalData = itemStack.get(MoneyData.class);

		if (optionalData.isPresent()) {
			MoneyData moneyData = optionalData.get();

			return moneyData.amount().get();
		}
		return 0;
	}
}
