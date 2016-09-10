package com.gmail.trentech.walletdrop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import com.gmail.trentech.walletdrop.core.EventListener;
import com.gmail.trentech.walletdrop.core.manipulators.ImmutableMoneyData;
import com.gmail.trentech.walletdrop.core.manipulators.MoneyData;
import com.gmail.trentech.walletdrop.core.manipulators.MoneyDataManipulatorBuilder;
import com.gmail.trentech.walletdrop.core.utils.ConfigManager;
import com.gmail.trentech.walletdrop.core.utils.NotificationManager;
import com.gmail.trentech.walletdrop.core.utils.Resource;
import com.gmail.trentech.walletdrop.core.utils.Settings;
import com.google.inject.Inject;

import me.flibio.updatifier.Updatifier;

@Updatifier(repoName = Resource.NAME, repoOwner = Resource.AUTHOR, version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION, description = Resource.DESCRIPTION, authors = Resource.AUTHOR, url = Resource.URL, dependencies = { @Dependency(id = "Updatifier", optional = true) })
public class Main {

	@Inject @ConfigDir(sharedRoot = false)
    private Path path;

	@Inject
	private Logger log;
	private NotificationManager notificationManager;
	
	private static PluginContainer plugin;
	private static Main instance;
	
	@Listener
	public void onPreInitializationEvent(GamePreInitializationEvent event) {
		plugin = Sponge.getPluginManager().getPlugin(Resource.ID).get();
		instance = this;
		notificationManager = new NotificationManager();
		
		try {			
			Files.createDirectories(path);		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Listener
	public void onInitializationEvent(GameInitializationEvent event) {
		Sponge.getDataManager().register(MoneyData.class, ImmutableMoneyData.class, new MoneyDataManipulatorBuilder());
	}

	@Listener
	public void onPostInitializationEvent(GamePostInitializationEvent event) {
		ConfigManager.init("global");

		Sponge.getEventManager().registerListeners(this, new EventListener());
	}

	@Listener
	public void onReloadEvent(GameReloadEvent event) {
		ConfigManager.init("global");

		for (World world : Sponge.getServer().getWorlds()) {
			Settings.close(world);
			Settings.init(world);
		}
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public Logger getLog() {
		return log;
	}

	public Path getPath() {
		return path;
	}
	
	public static Main instance() {
        return instance;
    }

	public static PluginContainer getPlugin() {
		return plugin;
	}
}
