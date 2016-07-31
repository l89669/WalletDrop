package com.gmail.trentech.walletdrop;

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

import com.gmail.trentech.walletdrop.core.EventListener;
import com.gmail.trentech.walletdrop.core.manipulators.ImmutableMoneyData;
import com.gmail.trentech.walletdrop.core.manipulators.MoneyData;
import com.gmail.trentech.walletdrop.core.manipulators.MoneyDataManipulatorBuilder;
import com.gmail.trentech.walletdrop.core.utils.ConfigManager;
import com.gmail.trentech.walletdrop.core.utils.Resource;
import com.gmail.trentech.walletdrop.core.utils.Settings;

import me.flibio.updatifier.Updatifier;

@Updatifier(repoName = Resource.NAME, repoOwner = Resource.AUTHOR, version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION, description = Resource.DESCRIPTION, authors = Resource.AUTHOR, url = Resource.URL, dependencies = { @Dependency(id = "Updatifier", optional = true) })
public class Main {

	private Logger log;
	private PluginContainer plugin;
	private EconomyService economy;
	
	private static Main instance;
	
	@Listener
	public void onPreInitializationEvent(GamePreInitializationEvent event) {
		instance = this;
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
			getLog().error("No economy plugin found. Aborting initialization.");
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

	public static Main getInstance() {
        return instance;
    }
	
	public EconomyService getEconomy() {
		return economy;
	}

	public Logger getLog() {
		return log;
	}

	public PluginContainer getPlugin() {
		return plugin;
	}
}
