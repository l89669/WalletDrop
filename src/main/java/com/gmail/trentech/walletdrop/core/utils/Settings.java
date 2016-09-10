package com.gmail.trentech.walletdrop.core.utils;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.World;

import com.gmail.trentech.walletdrop.Main;
import com.gmail.trentech.walletdrop.core.consumers.DropsPerSecond;
import com.gmail.trentech.walletdrop.core.data.MobDropData;
import com.gmail.trentech.walletdrop.core.data.PlayerDropData;
import com.gmail.trentech.walletdrop.core.data.PlayerDropData.MDDeathReason;

import ninja.leaping.configurate.ConfigurationNode;

public class Settings {

	private String worldName;
	private static ConcurrentHashMap<String, DropsPerSecond> dps = new ConcurrentHashMap<>();
	
	private Settings(World world) {
		this.worldName = world.getName();
	}

	private Settings() {

	}

	public static Settings get(World world) {
		return new Settings(world);
	}

	public static Settings get() {
		return new Settings();
	}

	public static void close(World world) {
		dps.remove(world.getName());
	}
	
	public static void init(World world) {
		String worldName = world.getName();
		
		ConfigManager.init(worldName);
		
		Settings settings = get(world);
		
		DropsPerSecond dps = new DropsPerSecond(0);
		dps.setDroplimit(settings.getDropLimit());

		Sponge.getGame().getScheduler().createTaskBuilder().intervalTicks(20).execute(dps).submit(Main.getPlugin());
		
		Settings.dps.put(world.getName(), dps);
	}

	public DropsPerSecond getDropsPerSecond() {
		return dps.get(worldName);
	}
	
	public boolean isKillOnlyDrops() {
		return ConfigManager.get(worldName).getConfig().getNode("1:drops", "only-on-kill").getBoolean();
	}

	public double getMaxStackValue() {
		return ConfigManager.get(worldName).getConfig().getNode("1:drops", "max-value").getDouble();
	}
	
	public int getDropLimit() {
		return ConfigManager.get(worldName).getConfig().getNode("1:drops", "max-per-second").getInt();
	}
	
	public double getPrecision() {
		return ConfigManager.get(worldName).getConfig().getNode("1:drops", "precision").getDouble();
	}
	
	public boolean isHopperAllowed() {
		return !ConfigManager.get(worldName).getConfig().getNode("1:drops", "hoppers-destroy").getBoolean();
	}
	
	public ItemType getItemType() {
		String itemType = ConfigManager.get(worldName).getConfig().getNode("1:drops", "1:item", "id").getString();

		Optional<ItemType> optionalType = Sponge.getRegistry().getType(ItemType.class, itemType);

		if (!optionalType.isPresent()) {
			Main.instance().getLog().error(itemType + " is an not valid");
			return null;
		} else {
			return optionalType.get();
		}
	}
	
	public int getItemUnsafeDamage() {
		return ConfigManager.get(worldName).getConfig().getNode("1:drops", "1:item", "unsafe-damage").getInt();
	}
	
	public boolean isVanillaSpawnerAllowed() {
		return ConfigManager.get(worldName).getConfig().getNode("2:spawners", "vanilla").getBoolean();
	}

	public boolean isModSpawnerAllowed() {
		return ConfigManager.get(worldName).getConfig().getNode("2:spawners", "mods").getBoolean();
	}

	public boolean isPluginSpawnerAllowed() {
		return ConfigManager.get(worldName).getConfig().getNode("2:spawners", "plugins").getBoolean();
	}
	
	public boolean isEggSpawnerAllowed() {
		return ConfigManager.get(worldName).getConfig().getNode("2:spawners", "egg").getBoolean();
	}
	
	public PlayerDropData getPlayerDrops() {
		PlayerDropData playerdrops = new PlayerDropData(getPrecision());

		for (MDDeathReason deathReason : MDDeathReason.values()) {
			if (!playerdrops.setDeathAmount(ConfigManager.get(worldName).getConfig().getNode("3:players", deathReason.getName()).getString(), deathReason)) {
				Main.instance().getLog().error("Invalid amount at 3:players, " + deathReason.getName() + " Reverting to 0.");
			}
		}

		return playerdrops;
	}
	
	private enum NotificationType {
		ACTION_BAR("ACTION_BAR", ChatTypes.ACTION_BAR), 
		CHAT("CHAT", ChatTypes.CHAT);
		
		private String name;
		private ChatType chatType;

		private NotificationType(String name, ChatType chatType) {
			this.name = name;
			this.chatType = chatType;
		}

		public String getName() {
			return name;
		}

		public ChatType getChatType() {
			return chatType;
		}
		
		public static Optional<NotificationType> get(String name) {
			
			for (NotificationType type : NotificationType.values()) {
				if(type.getName().equalsIgnoreCase(name)) {
					return Optional.of(type);
				}
			}
			
			return Optional.empty();
		}
	}
	
	public boolean isPickupChatNotification() {
		return ConfigManager.get(worldName).getConfig().getNode("4:notifications", "1:pickup", "enable").getBoolean();
	}

	public String getPickupChatMessage() {
		return ConfigManager.get(worldName).getConfig().getNode("4:notifications", "1:pickup", "message").getString();
	}

	public boolean isNotificationDelay() {
		return ConfigManager.get(worldName).getConfig().getNode("4:notifications", "1:pickup", "delay").getBoolean();
	}
	
	public boolean isDeathChatNotification() {
		return ConfigManager.get(worldName).getConfig().getNode("4:notifications", "2:death", "enable").getBoolean();
	}

	public String getDeathChatMessage() {
		return ConfigManager.get(worldName).getConfig().getNode("4:notifications", "2:death", "message").getString();
	}
	
	public ChatType getChatType() {
		String chatType = ConfigManager.get(worldName).getConfig().getNode("4:notifications", "location").getString();
		
		Optional<NotificationType> optionalType = NotificationType.get(chatType);

		if (!optionalType.isPresent()) {
			Main.instance().getLog().error(chatType + " is an not valid");
			return null;
		} else {
			return optionalType.get().getChatType();
		}
	}
	
	public boolean isCreativeModeAllowed() {
		return ConfigManager.get(worldName).getConfig().getNode("5:settings", "allow-creative-mode").getBoolean();
	}

	public boolean isUsePermissions() {
		return ConfigManager.get(worldName).getConfig().getNode("5:settings", "use-permissions").getBoolean();
	}
	
	private MobDropData getMobData(String entity) {
		ConfigurationNode node = ConfigManager.get(worldName).getConfig().getNode("6:mobs");

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
}
