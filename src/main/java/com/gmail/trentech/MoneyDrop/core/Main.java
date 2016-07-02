package com.gmail.trentech.MoneyDrop.core;

import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.world.World;

import com.gmail.trentech.MoneyDrop.core.data.ImmutableMoneyData;
import com.gmail.trentech.MoneyDrop.core.data.MoneyData;
import com.gmail.trentech.MoneyDrop.core.data.MoneyDataManipulatorBuilder;
import com.gmail.trentech.MoneyDrop.core.utils.ConfigManager;
import com.gmail.trentech.MoneyDrop.core.utils.Resource;
import com.gmail.trentech.MoneyDrop.core.utils.Settings;

import me.flibio.updatifier.Updatifier;

@Updatifier(repoName = "MoneyDrop", repoOwner = "TrenTech", version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, authors = Resource.AUTHOR, url = Resource.URL, dependencies = { @Dependency(id = "Updatifier", optional = true) })
public class Main {

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
			Settings.close(world);
			Settings.init(world);
		}
	}

	private boolean setupEconomy() {
		Optional<EconomyService> optionalEconomy = Sponge.getServiceManager().provide(EconomyService.class);

		if (optionalEconomy.isPresent()) {
			economy = optionalEconomy.get();
		}

		return optionalEconomy.isPresent();
	}

	public static EconomyService getEconomy() {
		return economy;
	}

	public static Logger getLog() {
		return log;
	}

	public static PluginContainer getPlugin() {
		return plugin;
	}
}
