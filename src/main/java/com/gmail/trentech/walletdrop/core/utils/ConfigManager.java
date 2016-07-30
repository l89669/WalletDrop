package com.gmail.trentech.walletdrop.core.utils;

import java.io.File;
import java.io.IOException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.SkeletonType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.world.World;

import com.gmail.trentech.walletdrop.Main;
import com.gmail.trentech.walletdrop.core.data.PlayerDropData.MDDeathReason;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class ConfigManager {

	private File file;
	private CommentedConfigurationNode config;
	private ConfigurationLoader<CommentedConfigurationNode> loader;

	private ConfigManager() {
		String folder = "config" + File.separator + Resource.ID;
		if (!new File(folder).isDirectory()) {
			new File(folder).mkdirs();
		}
		file = new File(folder, "global.conf");

		create();
		load();
	}

	private ConfigManager(String configName) {
		String folder = "config" + File.separator + Resource.ID;

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

			if (config.getNode("1:drops", "1:item", "id").isVirtual()) {
				config.getNode("1:drops", "1:item", "id").setValue(ItemTypes.GOLD_NUGGET.getId()).setComment("Item id or ItemStack used to represent money");
			}
			if (config.getNode("1:drops", "1:item", "unsafe-damage").isVirtual()) {
				config.getNode("1:drops", "1:item", "unsafe-damage").setValue(0).setComment("Unsafe damage value of ItemStack for items such as colored wool, wooden plank varients etc.");
			}
			if (config.getNode("1:drops", "max-value").isVirtual()) {
				config.getNode("1:drops", "max-value").setValue(0).setComment("Split money into multiple ItemStacks base on the max value a ItemStack can have. Set this to 0 to drop 1 ItemStack per kill");
			}
			if (config.getNode("1:drops", "precision").isVirtual()) {
				config.getNode("1:drops", "precision").setValue(0.01).setComment("The precision of the dropped money. 0.0 to 1.0. If mob has a minimum of 1 and a maximum of 3, with precision of 1, mob can drop 1, 2, or 3, with precision of 0.5, mob can drop 1, 1.5, 2, 2.5, or 3");
			}
			if (config.getNode("1:drops", "hoppers-destroy").isVirtual()) {
				config.getNode("1:drops", "hoppers-destroy").setValue(false).setComment("!!!NOT IMPLEMENTED YET!!!");
			}
			if (config.getNode("1:drops", "max-per-second").isVirtual()) {
				config.getNode("1:drops", "max-per-second").setValue(0).setComment("The maximum amount of drops that can happen in a second. Set this to 0 if you don't want your drops to be limited. Requires reload");
			}
			if (config.getNode("1:drops", "only-on-kill").isVirtual()) {
				config.getNode("1:drops", "only-on-kill").setValue(true).setComment("Collect money from mobs killed by players only");
			}
			if (config.getNode("2:spawners", "mods").isVirtual()) {
				config.getNode("2:spawners").setComment("enable mobs from different spawners dropping money");
				config.getNode("2:spawners", "mods").setValue(true);
			}
			if (config.getNode("2:spawners", "plugins").isVirtual()) {
				config.getNode("2:spawners", "plugins").setValue(true);
			}
			if (config.getNode("2:spawners", "vanilla").isVirtual()) {
				config.getNode("2:spawners", "vanilla").setValue(true);
			}
			if (config.getNode("2:spawners", "egg").isVirtual()) {
				config.getNode("2:spawners", "egg").setValue(true);
			}
			
			for (MDDeathReason deathReason : MDDeathReason.values()) {
				if (config.getNode("3:players", deathReason.getName()).isVirtual()) {
					config.getNode("3:players").setComment("Amount to charge player for dying depending on the death reason");
					config.getNode("3:players", deathReason.getName()).setValue(0);
				}
			}
			if (config.getNode("4:notifications", "location").isVirtual()) {
				config.getNode("4:notifications", "location").setValue("ACTION_BAR").setComment("Set to ACTION_BAR to have notifications display bottom on the action bar or CHAT to dislay in normal chat window");
			}
			if (config.getNode("4:notifications", "1:pickup", "enable").isVirtual()) {
				config.getNode("4:notifications", "1:pickup").setComment("Send player notification message when picking up money");
				config.getNode("4:notifications", "1:pickup", "enable").setValue(true);
			}
			if (config.getNode("4:notifications", "1:pickup", "message").isVirtual()) {
				config.getNode("4:notifications", "1:pickup", "message").setValue("&aYou picked up &e$<money>");
			}
			if (config.getNode("4:notifications", "2:death", "enable").isVirtual()) {
				config.getNode("4:notifications", "2:death").setComment("Send player notification message when dies and loses money");
				config.getNode("4:notifications", "2:death", "enable").setValue(true);
			}
			if (config.getNode("4:notifications", "2:death", "message").isVirtual()) {
				config.getNode("4:notifications", "2:death", "message").setValue("&cYou wake up, your wallet missing &e$<money>");
			}
			if (config.getNode("5:settings", "use-permissions").isVirtual()) {
				config.getNode("5:settings", "use-permissions").setValue(false).setComment("If true, players will require moneydrop.enable to collect money");
			}
			if (config.getNode("5:settings", "allow-creative-mode").isVirtual()) {
				config.getNode("5:settings", "allow-creative-mode").setValue(true).setComment("If true, players will be allowed to collect money from mobs in creative mode");
			}

			for (EntityType entityType : Sponge.getRegistry().getAllOf(EntityType.class)) {
				if (Living.class.isAssignableFrom(entityType.getEntityClass()) && !(entityType.equals(EntityTypes.ARMOR_STAND) || entityType.equals(EntityTypes.HUMAN))) {
					switch (entityType.getId()) {
					case "minecraft:skeleton":
						String skeleton = entityType.getId();

						for (SkeletonType type : Sponge.getRegistry().getAllOf(SkeletonType.class)) {
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

						for (Profession type : Sponge.getRegistry().getAllOf(Profession.class)) {
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

			if (config.getNode("1:drops", "1:item", "id").isVirtual()) {
				config.getNode("1:drops", "1:item", "id").setValue(global.getNode("1:drops", "1:item", "id").getString()).setComment("Item id or ItemStack used to represent money");
			}
			if (config.getNode("1:drops", "1:item", "unsafe-damage").isVirtual()) {
				config.getNode("1:drops", "1:item", "unsafe-damage").setValue(global.getNode("1:drops", "1:item", "unsafe-damage").getInt()).setComment("Unsafe damage value of ItemStack for items such as colored wool, wooden plank varients etc.");
			}
			if (config.getNode("1:drops", "max-value").isVirtual()) {
				config.getNode("1:drops", "max-value").setValue(global.getNode("1:drops", "max-value").getDouble()).setComment("Split money into multiple ItemStacks base on the max value a ItemStack can have. Set this to 0 to drop 1 ItemStack per kill");
			}
			if (config.getNode("1:drops", "precision").isVirtual()) {
				config.getNode("1:drops", "precision").setValue(global.getNode("1:drops", "precision").getDouble()).setComment("The precision of the dropped money. 0.0 to 1.0. If mob has a minimum of 1 and a maximum of 3, with precision of 1, mob can drop 1, 2, or 3, with precision of 0.5, mob can drop 1, 1.5, 2, 2.5, or 3");
			}
			if (config.getNode("1:drops", "hoppers-destroy").isVirtual()) {
				config.getNode("1:drops", "hoppers-destroy").setValue(global.getNode("1:drops", "hoppers-destroy").getBoolean()).setComment("!!!NOT IMPLEMENTED YET!!!");
			}
			if (config.getNode("1:drops", "max-per-second").isVirtual()) {
				config.getNode("1:drops", "max-per-second").setValue(global.getNode("1:drops", "max-per-second").getInt()).setComment("The maximum amount of drops that can happen in a second. Set this to 0 if you don't want your drops to be limited. Requires reload");
			}
			if (config.getNode("1:drops", "only-on-kill").isVirtual()) {
				config.getNode("1:drops", "only-on-kill").setValue(global.getNode("1:drops", "only-drop-on-kill").getBoolean()).setComment("Collect money from mobs killed by players only");
			}
			if (config.getNode("2:spawners", "mods").isVirtual()) {
				config.getNode("2:spawners").setComment("enable mobs from different spawners dropping money");
				config.getNode("2:spawners", "mods").setValue(global.getNode("2:spawners", "mods").getBoolean());
			}
			if (config.getNode("2:spawners", "plugins").isVirtual()) {
				config.getNode("2:spawners", "plugins").setValue(global.getNode("2:spawners", "plugins").getBoolean());
			}
			if (config.getNode("2:spawners", "vanilla").isVirtual()) {
				config.getNode("2:spawners", "vanilla").setValue(global.getNode("2:spawners", "vanilla").getBoolean());
			}
			if (config.getNode("2:spawners", "egg").isVirtual()) {
				config.getNode("2:spawners", "egg").setValue(global.getNode("2:spawners", "egg").getBoolean());
			}

			for (MDDeathReason deathReason : MDDeathReason.values()) {
				if (config.getNode("3:players", deathReason.getName()).isVirtual()) {
					config.getNode("3:players").setComment("Amount to charge player for dying depending on the death reason");
					config.getNode("3:players", deathReason.getName()).setValue(global.getNode("1:players", deathReason.getName()).getDouble());
				}
			}
			
			if (config.getNode("4:notifications", "location").isVirtual()) {
				config.getNode("4:notifications", "location").setValue(global.getNode("4:notifications", "location").getString()).setComment("Set to ACTION_BAR to have notifications display bottom on the action bar or CHAT to dislay in normal chat window");
			}
			if (config.getNode("4:notifications", "1:pickup", "enable").isVirtual()) {
				config.getNode("4:notifications", "1:pickup").setComment("Send player notification message when picking up money");
				config.getNode("4:notifications", "1:pickup", "enable").setValue(global.getNode("4:notifications", "1:pickup", "enable").getBoolean());
			}
			if (config.getNode("4:notifications", "1:pickup", "message").isVirtual()) {
				config.getNode("4:notifications", "1:pickup", "message").setValue(global.getNode("4:notifications", "1:pickup", "message").getString());
			}
			if (config.getNode("4:notifications", "2:death", "enable").isVirtual()) {
				config.getNode("4:notifications", "2:death").setComment("Send player notification message when dies and loses money");
				config.getNode("4:notifications", "2:death", "enable").setValue(global.getNode("4:notifications", "2:death", "enable").getBoolean());
			}
			if (config.getNode("4:notifications", "2:death", "message").isVirtual()) {
				config.getNode("4:notifications", "2:death", "message").setValue(global.getNode("4:notifications", "2:death", "message").getString());
			}
			if (config.getNode("5:settings", "use-permissions").isVirtual()) {
				config.getNode("5:settings", "use-permissions").setValue(global.getNode("5:settings", "use-permissions").getBoolean()).setComment("If true, players will require moneydrop.enable to collect money");
			}
			if (config.getNode("5:settings", "allow-creative-mode").isVirtual()) {
				config.getNode("5:settings", "allow-creative-mode").setValue(global.getNode("5:settings", "allow-creative-mode").getBoolean()).setComment("If true, players will be allowed to collect money from mobs in creative mode");
			}

			for (EntityType entityType : Sponge.getRegistry().getAllOf(EntityType.class)) {
				if (Living.class.isAssignableFrom(entityType.getEntityClass()) && !(entityType.equals(EntityTypes.ARMOR_STAND) || entityType.equals(EntityTypes.HUMAN))) {
					switch (entityType.getId()) {
					case "minecraft:skeleton":
						String skeleton = entityType.getId();

						for (SkeletonType type : Sponge.getRegistry().getAllOf(SkeletonType.class)) {
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

						for (Profession type : Sponge.getRegistry().getAllOf(Profession.class)) {
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
		if (config.getNode("6:mobs", entity, "dropped-minimum").isVirtual()) {
			config.getNode("6:mobs", entity, "dropped-minimum").setValue(1.0).setComment("Minimum amount of money mob will drop");
		}
		if (config.getNode("6:mobs", entity, "dropped-maximum").isVirtual()) {
			config.getNode("6:mobs", entity, "dropped-maximum").setValue(3.0).setComment("Maximum amount of money mob will drop");
		}
		if (config.getNode("6:mobs", entity, "dropped-frequency").isVirtual()) {
			config.getNode("6:mobs", entity, "dropped-frequency").setValue(1.0).setComment("Percentage mob will drop money. 0.0 to 1.0");
		}
	}

	private void initMobData(ConfigurationNode global, String entity) {
		if (config.getNode("6:mobs", entity, "dropped-minimum").isVirtual()) {
			config.getNode("6:mobs", entity, "dropped-minimum").setValue(global.getNode("6:mobs", entity, "dropped-minimum").getDouble()).setComment("Minimum amount of money mob will drop");
		}
		if (config.getNode("6:mobs", entity, "dropped-maximum").isVirtual()) {
			config.getNode("6:mobs", entity, "dropped-maximum").setValue(global.getNode("6:mobs", entity, "dropped-maximum").getDouble()).setComment("Maximum amount of money mob will drop");
		}
		if (config.getNode("6:mobs", entity, "dropped-frequency").isVirtual()) {
			config.getNode("6:mobs", entity, "dropped-frequency").setValue(global.getNode("6:mobs", entity, "dropped-frequency").getDouble()).setComment("Percentage mob will drop money. 0.0 to 1.0");
		}
	}

	private void create() {
		if (!file.exists()) {
			try {
				Main.getInstance().getLog().info("Creating new " + file.getName() + " file...");
				file.createNewFile();
			} catch (IOException e) {
				Main.getInstance().getLog().error("Failed to create new config file");
				e.printStackTrace();
			}
		}
	}

	private void load() {
		loader = HoconConfigurationLoader.builder().setFile(file).build();
		try {
			config = loader.load();
		} catch (IOException e) {
			Main.getInstance().getLog().error("Failed to load config");
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			loader.save(config);
		} catch (IOException e) {
			Main.getInstance().getLog().error("Failed to save config");
			e.printStackTrace();
		}
	}
}