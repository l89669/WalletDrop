package com.gmail.trentech.MoneyDrop.utils;

import java.util.Optional;

import org.spongepowered.api.data.manipulator.mutable.entity.SkeletonData;
import org.spongepowered.api.data.manipulator.mutable.entity.VillagerZombieData;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.SkeletonType;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.entity.living.monster.Skeleton;
import org.spongepowered.api.entity.living.monster.Zombie;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.world.World;

import com.gmail.trentech.MoneyDrop.MobDropData;
import com.gmail.trentech.MoneyDrop.MoneyDrop;
import com.gmail.trentech.MoneyDrop.PlayerDropData;
import com.gmail.trentech.MoneyDrop.PlayerDropData.MDDeathReason;

import ninja.leaping.configurate.ConfigurationNode;

public class Settings {

	private ConfigManager configManager;

	private Settings(ConfigManager config) {
		this.configManager = config;
	}

	private Settings() {
		this.configManager = ConfigManager.get();
	}

	public static Settings get(World world) {
		return new Settings(ConfigManager.get(world));
	}

	public static Settings get() {
		return new Settings();
	}

	public static void initSettings(World world) {
		ConfigManager configManager = ConfigManager.get(world);
		configManager.init();
	}

	private MobDropData getMobData(String entity) {
		ConfigurationNode node = configManager.getConfig().getNode("2:mobs");

		double min = node.getNode(entity, "dropped-minimum").getDouble();
		double max = node.getNode(entity, "dropped-maximum").getDouble();
		double droppercent = node.getNode(entity, "dropped-frequency").getDouble();

		MobDropData data = new MobDropData(droppercent, min, max);

		return data;
	}

	public MobDropData getMobData(Living entity) {
		String entityId;
		switch (entity.getType().getId()) {
		case "minecraft:zombie":
			Zombie zombie = (Zombie) entity;
			entityId = entity.getType().getId();

			if (zombie.get(VillagerZombieData.class).isPresent()) {
				entityId += "-villager";
			}
			if (zombie.getAgeData().baby().get()) {
				entityId += "-child";
			}
			break;
		case "minecraft:skeleton":
			Skeleton skeleton = (Skeleton) entity;
			Value<SkeletonType> type = skeleton.get(SkeletonData.class).get().type();
			entityId = entity.getType().getId();

			if (type.exists()) {
				entityId += "-" + skeleton.get(SkeletonData.class).get().type().get().getId().toLowerCase();
			}
			break;
		case "minecraft:creeper":
			Creeper creeper = (Creeper) entity;
			entityId = entity.getType().getId();

			if (creeper.charged().get()) {
				entityId += "-charged";
			}
			break;
		case "minecraft:villager":
			Villager villager = (Villager) entity;
			Value<Career> career = villager.getCareerData().type();
			entityId = entity.getType().getId();

			if (career.exists()) {
				entityId += "-" + villager.getCareerData().type().get().getProfession().getId();
			}
			break;
		default:
			entityId = entity.getType().getId();
			break;
		}

		return getMobData(entityId);
	}

	public PlayerDropData getPlayerDrops() {
		PlayerDropData playerdrops = new PlayerDropData(getPrecision());

		for (MDDeathReason deathReason : MDDeathReason.values()) {
			if (!playerdrops.setDeathAmount(configManager.getConfig().getNode("1:settings", "1:players", deathReason.getName()).getString(), deathReason)) {
				MoneyDrop.getLog().error("Invalid amount at 1:settings, 1:players, " + deathReason.getName() + " Reverting to 0.");
			}
		}

		return playerdrops;
	}

	public boolean isVanillaSpawnerAllowed() {
		return configManager.getConfig().getNode("1:settings", "allow-vanilla-mobspawners").getBoolean();
	}

	public boolean isModSpawnerAllowed() {
		return configManager.getConfig().getNode("1:settings", "allow-mod-mobspawners").getBoolean();
	}

	public boolean isPluginSpawnerAllowed() {
		return configManager.getConfig().getNode("1:settings", "allow-plugin-mobspawners").getBoolean();
	}
	
	public boolean isEggSpawnerAllowed() {
		return configManager.getConfig().getNode("1:settings", "allow-egg-spawners").getBoolean();
	}

	public boolean isKillOnlyDrops() {
		return configManager.getConfig().getNode("1:settings", "mobs-only-drop-on-kill").getBoolean();
	}

	public boolean isIndependentDrops() {
		return configManager.getConfig().getNode("1:settings", "independent-drops").getBoolean();
	}
	
	public double getMaxStackValue() {
		return configManager.getConfig().getNode("1:settings", "max-stack-value").getDouble();
	}
	
	public ItemType getItemType() {
		String itemType = configManager.getConfig().getNode("1:settings", "dropped-item-id").getString();

		Optional<ItemType> optionalType = MoneyDrop.getGame().getRegistry().getType(ItemType.class, itemType);

		if (!optionalType.isPresent()) {
			MoneyDrop.getLog().error(itemType + " is an not valid");
			return null;
		} else {
			return optionalType.get();
		}
	}

	public boolean isPickupChatNotification() {
		return configManager.getConfig().getNode("1:settings", "pickup-chat-notification-enabled").getBoolean();
	}

	public String getPickupChatMessage() {
		return configManager.getConfig().getNode("1:settings", "pickup-chat-notification-message").getString();
	}

	public boolean isDeathChatNotification() {
		return configManager.getConfig().getNode("1:settings", "death-chat-notification-enabled").getBoolean();
	}

	public String getDeathChatMessage() {
		return configManager.getConfig().getNode("1:settings", "death-chat-notification-message").getString();
	}

	public double getPrecision() {
		return configManager.getConfig().getNode("1:settings", "precision").getDouble();
	}

	public boolean isCreativeModeAllowed() {
		return configManager.getConfig().getNode("1:settings", "allow-creative-mode").getBoolean();
	}

	public boolean isUsePermissions() {
		return configManager.getConfig().getNode("1:settings", "use-permissions").getBoolean();
	}
}
