package com.gmail.trentech.MoneyDrop;

import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
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

	private static Game game;
	private static Logger log;
	private static PluginContainer plugin;

	private static EconomyService economy;

	@Listener
	public void onPreInitializationEvent(GamePreInitializationEvent event) {
		game = Sponge.getGame();
		plugin = getGame().getPluginManager().getPlugin(Resource.ID).get();
		log = getPlugin().getLogger();
	}

	@Listener
	public void onInitializationEvent(GameInitializationEvent event) {
		getGame().getDataManager().register(MoneyData.class, ImmutableMoneyData.class, new MoneyDataManipulatorBuilder());
	}

	@Listener
	public void onPostInitializationEvent(GamePostInitializationEvent event) {
		if (!setupEconomy()) {
			getLog().error("No economy plugin found. Disabling MoneyDrop...");
			return;
		}

		ConfigManager.get().init();

		getGame().getEventManager().registerListeners(this, new EventListener());
	}

	@Listener
	public void onReloadEvent(GameReloadEvent event) {
		ConfigManager.get().init();

		for (World world : getGame().getServer().getWorlds()) {
			Settings.initSettings(world);
		}
	}

	private boolean setupEconomy() {
		Optional<EconomyService> optionalEconomy = getGame().getServiceManager().provide(EconomyService.class);

		if (optionalEconomy.isPresent()) {
			economy = optionalEconomy.get();
		}

		return optionalEconomy.isPresent();
	}

	public static EconomyService getEconomy() {
		return economy;
	}

	public static Game getGame() {
		return game;
	}

	public static Logger getLog() {
		return log;
	}

	public static PluginContainer getPlugin() {
		return plugin;
	}
}
