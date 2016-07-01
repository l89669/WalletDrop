package com.gmail.trentech.MoneyDrop.utils;

import java.io.File;
import java.io.IOException;

import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.SkeletonType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.world.World;

import com.gmail.trentech.MoneyDrop.MoneyDrop;
import com.gmail.trentech.MoneyDrop.PlayerDropData.MDDeathReason;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigManager {

	private File file;
	private CommentedConfigurationNode config;
	private ConfigurationLoader<CommentedConfigurationNode> loader;

	private ConfigManager() {
		String folder = "config" + File.separator + "moneydrop";
		if (!new File(folder).isDirectory()) {
			new File(folder).mkdirs();
		}
		file = new File(folder, "global.conf");

		create();
		load();
	}

	private ConfigManager(String configName) {
		String folder = "config" + File.separator + "moneydrop";

		if (!new File(folder).isDirectory()) {
			new File(folder).mkdirs();
		}
		file = new File(folder, configName + ".conf");

		create();
		load();
	}

	public static ConfigManager get(World world) {
		return new ConfigManager(world.getName());
	}

	public static ConfigManager get() {
		return new ConfigManager();
	}

	public ConfigurationLoader<CommentedConfigurationNode> getLoader() {
		return loader;
	}

	public CommentedConfigurationNode getConfig() {
		return config;
	}

	public void init() {
		if (file.getName().equalsIgnoreCase("global.conf")) {
			if (config.getNode("1:settings", "allow-mod-mobspawners").isVirtual()) {
				config.getNode("1:settings", "allow-mod-mobspawners").setValue(false).setComment("Collect money from mobs spawned by mod spawners");
			}
			if (config.getNode("1:settings", "allow-plugin-mobspawners").isVirtual()) {
				config.getNode("1:settings", "allow-plugin-mobspawners").setValue(false).setComment("Collect money from mobs spawned by plugin spawners");
			}
			if (config.getNode("1:settings", "allow-vanilla-mobspawners").isVirtual()) {
				config.getNode("1:settings", "allow-vanilla-mobspawners").setValue(false).setComment("Collect money from mobs spawned by vanilla spawners");
			}
			if (config.getNode("1:settings", "allow-egg-spawners").isVirtual()) {
				config.getNode("1:settings", "allow-egg-spawners").setValue(false).setComment("Collect money from mobs spawned by spawner eggs");
			}
			if (config.getNode("1:settings", "mobs-only-drop-on-kill").isVirtual()) {
				config.getNode("1:settings", "mobs-only-drop-on-kill").setValue(true).setComment("Collect money from mobs killed by players only");
			}
			if (config.getNode("1:settings", "use-permissions").isVirtual()) {
				config.getNode("1:settings", "use-permissions").setValue(false).setComment("If true, players will require moneydrop.enable to collect money");
			}
			if (config.getNode("1:settings", "allow-creative-mode").isVirtual()) {
				config.getNode("1:settings", "allow-creative-mode").setValue(false).setComment("Collect money from mobs in creative mode");
			}
			if (config.getNode("1:settings", "dropped-item-id").isVirtual()) {
				config.getNode("1:settings", "dropped-item-id").setValue(ItemTypes.GOLD_NUGGET.getId()).setComment("Item id or ItemStack used to represent money");
			}
			if (config.getNode("1:settings", "dropped-item-unsafe-damage").isVirtual()) {
				config.getNode("1:settings", "dropped-item-unsafe-damage").setValue(0).setComment("Unsafe damage value of ItemStack for items such as colored wool, wooden plank varients etc.");
			}
			if (config.getNode("1:settings", "independent-drops").isVirtual()) {
				config.getNode("1:settings", "independent-drops").setValue(true).setComment("If true, money spawns in multiple ItemStacks, If false one ItemStack per kill");
			}
			if (config.getNode("1:settings", "max-stack-value").isVirtual()) {
				config.getNode("1:settings", "max-stack-value").setValue(5).setComment("Set max value for each ItemStack");
			}
			if (config.getNode("1:settings", "precision").isVirtual()) {
				config.getNode("1:settings", "precision").setValue(0.01).setComment("The precision of the dropped money. If mob has a minimum of 1 and a maximum of 3, with precision of 1, mob can drop 1, 2, or 3, with precision of 0.5, mob can drop 1, 1.5, 2, 2.5, or 3");
			}
			if (config.getNode("1:settings", "pickup-chat-notification-enabled").isVirtual()) {
				config.getNode("1:settings", "pickup-chat-notification-enabled").setValue(true).setComment("Send player message when picking up money");
			}
			if (config.getNode("1:settings", "pickup-chat-notification-message").isVirtual()) {
				config.getNode("1:settings", "pickup-chat-notification-message").setValue("&aYou picked up &e$<money>").setComment("The message sent to player when money is picked up");
			}
			if (config.getNode("1:settings", "death-chat-notification-enabled").isVirtual()) {
				config.getNode("1:settings", "death-chat-notification-enabled").setValue(true).setComment("Send player message when dies");
			}
			if (config.getNode("1:settings", "death-chat-notification-message").isVirtual()) {
				config.getNode("1:settings", "death-chat-notification-message").setValue("&cYou wake up, your wallet missing &e$<money>").setComment("The message sent to player when player dies");
			}

			for (MDDeathReason deathReason : MDDeathReason.values()) {
				if (config.getNode("1:settings", "1:players", deathReason.getName()).isVirtual()) {
					config.getNode("1:settings", "1:players", deathReason.getName()).setValue(0);
				}
			}

			for (EntityType entityType : MoneyDrop.getGame().getRegistry().getAllOf(EntityType.class)) {
				if (Living.class.isAssignableFrom(entityType.getEntityClass()) && !(entityType.equals(EntityTypes.ARMOR_STAND) || entityType.equals(EntityTypes.HUMAN))) {
					switch (entityType.getId()) {
					case "minecraft:skeleton":
						String skeleton = entityType.getId();

						for (SkeletonType type : MoneyDrop.getGame().getRegistry().getAllOf(SkeletonType.class)) {
							initMobData(skeleton + "-" + type.getId().toLowerCase());
						}
						break;
					case "minecraft:zombie":
						initMobData(entityType.getId());
						initMobData(entityType.getId() + "-villager");
						initMobData(entityType.getId() + "-child");
						initMobData(entityType.getId() + "-villager-child");
						break;
					case "minecraft:creeper":
						initMobData(entityType.getId());
						initMobData(entityType.getId() + "-charged");
						break;
					case "minecraft:villager":
						String villager = entityType.getId();
						initMobData(villager);

						for (Profession type : MoneyDrop.getGame().getRegistry().getAllOf(Profession.class)) {
							initMobData(villager + "-" + type.getId());
						}
						break;
					case "minecraft:entityhorse":
						initMobData(entityType.getId());
						initMobData(entityType.getId());
						break;
					default:
						initMobData(entityType.getId());
						break;
					}
				}
			}
		} else {
			ConfigurationNode global = new ConfigManager().getConfig();

			if (config.getNode("1:settings", "allow-mod-mobspawners").isVirtual()) {
				config.getNode("1:settings", "allow-mod-mobspawners").setValue(global.getNode("1:settings", "allow-mod-mobspawners").getBoolean()).setComment("Collect money from mobs spawned by mod spawners");
			}
			if (config.getNode("1:settings", "allow-plugin-mobspawners").isVirtual()) {
				config.getNode("1:settings", "allow-plugin-mobspawners").setValue(global.getNode("1:settings", "allow-plugin-mobspawners").getBoolean()).setComment("Collect money from mobs spawned by plugin spawners");
			}
			if (config.getNode("1:settings", "allow-vanilla-mobspawners").isVirtual()) {
				config.getNode("1:settings", "allow-vanilla-mobspawners").setValue(global.getNode("1:settings", "allow-vanilla-mobspawners").getBoolean()).setComment("Collect money from mobs spawned by vanilla spawners");
			}
			if (config.getNode("1:settings", "allow-egg-spawners").isVirtual()) {
				config.getNode("1:settings", "allow-egg-spawners").setValue(global.getNode("1:settings", "allow-egg-spawners").getBoolean()).setComment("Collect money from mobs spawned by spawner eggs");
			}
			if (config.getNode("1:settings", "mobs-only-drop-on-kill").isVirtual()) {
				config.getNode("1:settings", "mobs-only-drop-on-kill").setValue(global.getNode("1:settings", "mobs-only-drop-on-kill").getBoolean()).setComment("Collect money from mobs killed by players only");
			}
			if (config.getNode("1:settings", "use-permissions").isVirtual()) {
				config.getNode("1:settings", "use-permissions").setValue(global.getNode("1:settings", "use-permissions").getBoolean()).setComment("If true, players will require moneydrop.enable to collect money");
			}
			if (config.getNode("1:settings", "allow-creative-mode").isVirtual()) {
				config.getNode("1:settings", "allow-creative-mode").setValue(global.getNode("1:settings", "allow-creative-mode").getBoolean()).setComment("If true, players will require moneydrop.enable to collect money");
			}
			if (config.getNode("1:settings", "dropped-item-id").isVirtual()) {
				config.getNode("1:settings", "dropped-item-id").setValue(global.getNode("1:settings", "dropped-item-id").getString()).setComment("Item id or ItemStack used to represent money");
			}
			if (config.getNode("1:settings", "dropped-item-unsafe-damage").isVirtual()) {
				config.getNode("1:settings", "dropped-item-unsafe-damage").setValue(global.getNode("1:settings", "dropped-item-unsafe-damage").getInt()).setComment("Unsafe damage value of ItemStack for items such as colored wool, wooden plank varients etc.");
			}
			if (config.getNode("1:settings", "independent-drops").isVirtual()) {
				config.getNode("1:settings", "independent-drops").setValue(global.getNode("1:settings", "independent-drops").getBoolean()).setComment("If true, money spawns in multiple ItemStacks, If false one ItemStack per kill");
			}
			if (config.getNode("1:settings", "max-stack-value").isVirtual()) {
				config.getNode("1:settings", "max-stack-value").setValue(global.getNode("1:settings", "max-stack-value").getDouble()).setComment("Set max value for each ItemStack");
			}
			if (config.getNode("1:settings", "precision").isVirtual()) {
				config.getNode("1:settings", "precision").setValue(global.getNode("1:settings", "precision").getDouble()).setComment("The precision of the dropped money. 0.0 to 1.0. If mob has a minimum of 1 and a maximum of 3, with precision of 1, mob can drop 1, 2, or 3, with precision of 0.5, mob can drop 1, 1.5, 2, 2.5, or 3");
			}
			if (config.getNode("1:settings", "pickup-chat-notification-enabled").isVirtual()) {
				config.getNode("1:settings", "pickup-chat-notification-enabled").setValue(global.getNode("1:settings", "pickup-chat-notification-enabled").getBoolean()).setComment("Send player message when picking up money");
			}
			if (config.getNode("1:settings", "pickup-chat-notification-message").isVirtual()) {
				config.getNode("1:settings", "pickup-chat-notification-message").setValue(global.getNode("1:settings", "pickup-chat-notification-message").getString()).setComment("The message sent to player when money is picked up");
			}
			if (config.getNode("1:settings", "death-chat-notification-enabled").isVirtual()) {
				config.getNode("1:settings", "death-chat-notification-enabled").setValue(global.getNode("1:settings", "death-chat-notification-enabled").getBoolean()).setComment("Send player message when dies");
			}
			if (config.getNode("1:settings", "death-chat-notification-message").isVirtual()) {
				config.getNode("1:settings", "death-chat-notification-message").setValue(global.getNode("1:settings", "death-chat-notification-message").getString()).setComment("The message sent to player when player dies");
			}

			for (MDDeathReason deathReason : MDDeathReason.values()) {
				if (config.getNode("1:settings", "1:players", deathReason.getName()).isVirtual()) {
					config.getNode("1:settings", "1:players").setComment("Amount to charge player for dying depending on the death reason");
					config.getNode("1:settings", "1:players", deathReason.getName()).setValue(global.getNode("1:settings", "1:players", deathReason.getName()).getDouble());
				}
			}

			for (EntityType entityType : MoneyDrop.getGame().getRegistry().getAllOf(EntityType.class)) {
				if (Living.class.isAssignableFrom(entityType.getEntityClass()) && !(entityType.equals(EntityTypes.ARMOR_STAND) || entityType.equals(EntityTypes.HUMAN))) {
					switch (entityType.getId()) {
					case "minecraft:skeleton":
						String skeleton = entityType.getId();

						for (SkeletonType type : MoneyDrop.getGame().getRegistry().getAllOf(SkeletonType.class)) {
							initMobData(global, skeleton + "-" + type.getId().toLowerCase());
						}
						break;
					case "minecraft:zombie":
						initMobData(global, entityType.getId());
						initMobData(global, entityType.getId() + "-villager");
						initMobData(global, entityType.getId() + "-child");
						initMobData(global, entityType.getId() + "-villager-child");
						break;
					case "minecraft:creeper":
						initMobData(global, entityType.getId());
						initMobData(global, entityType.getId() + "-charged");
						break;
					case "minecraft:villager":
						String villager = entityType.getId();
						initMobData(global, villager);

						for (Profession type : MoneyDrop.getGame().getRegistry().getAllOf(Profession.class)) {
							initMobData(global, villager + "-" + type.getId());
						}
						break;
					case "minecraft:entityhorse":
						initMobData(global, entityType.getId());
						initMobData(global, entityType.getId());
						break;
					default:
						initMobData(global, entityType.getId());
						break;
					}
				}
			}
		}
		save();
	}

	private void initMobData(String entity) {
		if (config.getNode("2:mobs", entity, "dropped-minimum").isVirtual()) {
			config.getNode("2:mobs", entity, "dropped-minimum").setValue(1.0).setComment("Minimum amount of money mob will drop");
		}
		if (config.getNode("2:mobs", entity, "dropped-maximum").isVirtual()) {
			config.getNode("2:mobs", entity, "dropped-maximum").setValue(3.0).setComment("Maximum amount of money mob will drop");
		}
		if (config.getNode("2:mobs", entity, "dropped-frequency").isVirtual()) {
			config.getNode("2:mobs", entity, "dropped-frequency").setValue(1.0).setComment("Percentage mob will drop money. 0.0 to 1.0");
		}
	}

	private void initMobData(ConfigurationNode global, String entity) {
		if (config.getNode("2:mobs", entity, "dropped-minimum").isVirtual()) {
			config.getNode("2:mobs", entity, "dropped-minimum").setValue(global.getNode("2:mobs", entity, "dropped-minimum").getDouble()).setComment("Minimum amount of money mob will drop");
		}
		if (config.getNode("2:mobs", entity, "dropped-maximum").isVirtual()) {
			config.getNode("2:mobs", entity, "dropped-maximum").setValue(global.getNode("2:mobs", entity, "dropped-maximum").getDouble()).setComment("Maximum amount of money mob will drop");
		}
		if (config.getNode("2:mobs", entity, "dropped-frequency").isVirtual()) {
			config.getNode("2:mobs", entity, "dropped-frequency").setValue(global.getNode("2:mobs", entity, "dropped-frequency").getDouble()).setComment("Percentage mob will drop money. 0.0 to 1.0");
		}
	}

	private void create() {
		if (!file.exists()) {
			try {
				MoneyDrop.getLog().info("Creating new " + file.getName() + " file...");
				file.createNewFile();
			} catch (IOException e) {
				MoneyDrop.getLog().error("Failed to create new config file");
				e.printStackTrace();
			}
		}
	}

	private void load() {
		loader = HoconConfigurationLoader.builder().setFile(file).build();
		try {
			config = loader.load();
		} catch (IOException e) {
			MoneyDrop.getLog().error("Failed to load config");
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			loader.save(config);
		} catch (IOException e) {
			MoneyDrop.getLog().error("Failed to save config");
			e.printStackTrace();
		}
	}
}