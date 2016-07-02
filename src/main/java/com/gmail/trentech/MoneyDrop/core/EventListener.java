package com.gmail.trentech.MoneyDrop.core;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.Hopper;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.economy.EconomyService;

import com.gmail.trentech.MoneyDrop.MoneyDrop;
import com.gmail.trentech.MoneyDrop.core.data.MobDropData;
import com.gmail.trentech.MoneyDrop.core.data.MoneyData;
import com.gmail.trentech.MoneyDrop.core.data.MoneyDataManipulatorBuilder;
import com.gmail.trentech.MoneyDrop.core.data.PlayerDropData;
import com.gmail.trentech.MoneyDrop.core.data.PlayerDropData.MDDeathReason;
import com.gmail.trentech.MoneyDrop.core.utils.Settings;
import com.gmail.trentech.MoneyDrop.data.MoneyStack;
import com.gmail.trentech.MoneyDrop.events.MoneyDropEvent;
import com.gmail.trentech.MoneyDrop.events.MoneyPickupEvent;
import com.gmail.trentech.MoneyDrop.events.PlayerMoneyDropEvent;

public class EventListener {

	private static ThreadLocalRandom random = ThreadLocalRandom.current();

	@Listener
	public void onLoadWorldEvent(LoadWorldEvent event) {
		Settings.init(event.getTargetWorld());
	}

	@Listener
	public void onUnloadWorldEvent(UnloadWorldEvent event) {
		Settings.close(event.getTargetWorld());
	}
	
	@Listener
	public void onDestructEntityEventHopper(DestructEntityEvent event, @First Hopper hopper) {
		if (!(event.getTargetEntity() instanceof Item)) {
			return;
		}
		Item item = (Item) event.getTargetEntity();

		Settings settings = Settings.get(item.getWorld());

		if(settings.isHopperAllowed()) {
			return;
		}
		
		if (!settings.getItemType().equals(item.getItemType())) {
			return;
		}
		ItemStack itemStack = item.item().get().createStack();
		
		double amount = MoneyDrop.getItemStackValue(item.item().get().createStack());

		if (amount == 0) {
			return;
		}

		hopper.getInventory().query(itemStack).clear();
	}
	
	@Listener
	public void onDestructEntityEventItem(DestructEntityEvent event, @Root Player player) {
		if (!(event.getTargetEntity() instanceof Item)) {
			return;
		}
		Item item = (Item) event.getTargetEntity();
		
		Settings settings = Settings.get(item.getWorld());

		if (!settings.getItemType().equals(item.getItemType())) {
			return;
		}

		ItemStack itemStack = item.item().get().createStack();

		double amount = MoneyDrop.getItemStackValue(item.item().get().createStack());

		if (amount == 0) {
			return;
		}

		if (player.gameMode().get().equals(GameModes.CREATIVE) && !settings.isCreativeModeAllowed()) {
			return;
		}

		if (settings.isUsePermissions()) {
			if (!player.hasPermission("moneydrop.enable")) {
				return;
			}
		}

		MoneyPickupEvent mpEvent = new MoneyPickupEvent(player, itemStack, amount, Cause.of(NamedCause.source(Main.getPlugin())));

		if (!Sponge.getEventManager().post(mpEvent)) {
			player.getInventory().query(itemStack).clear();

			MoneyDrop.giveOrTakeMoney(player, new BigDecimal(mpEvent.getValue()));

			MoneyDrop.sendPickupChatMessage(Settings.get(player.getWorld()), player, amount);
		}
	}

	// @Listener
	public void onChangeInventoryEventPickup(ChangeInventoryEvent.Pickup event, @First Player player) {
		for (Transaction<ItemStackSnapshot> snapshot : event.getTransactions()) {
			ItemStack itemStack = snapshot.getOriginal().createStack();

			double amount = MoneyDrop.getItemStackValue(itemStack);

			if (amount == 0) {
				continue;
			}

			Settings settings = Settings.get(player.getWorld());

			if (player.gameMode().get().equals(GameModes.CREATIVE) && !settings.isCreativeModeAllowed()) {
				event.setCancelled(true);
				return;
			}

			if (settings.isUsePermissions()) {
				if (!player.hasPermission("moneydrop.enable")) {
					return;
				}
			}

			MoneyPickupEvent mpEvent = new MoneyPickupEvent(player, itemStack, amount, Cause.of(NamedCause.source(Main.getPlugin())));

			if (!Sponge.getEventManager().post(mpEvent)) {
				player.getInventory().query(itemStack).clear();

				MoneyDrop.giveOrTakeMoney(player, new BigDecimal(mpEvent.getValue()));

				MoneyDrop.sendPickupChatMessage(Settings.get(player.getWorld()), player, amount);
			}
		}
	}

	@Listener
	public void onDestructEntityEventDeath(DestructEntityEvent.Death event, @First EntityDamageSource src) {
		if (event.getTargetEntity() instanceof Player) {
			return;
		}
		Living entity = event.getTargetEntity();

		if (entity.getType().equals(EntityTypes.ARMOR_STAND) || entity.getType().equals(EntityTypes.HUMAN)) {
			return;
		}

		Settings settings = Settings.get(entity.getWorld());

		if(settings.getDropsPerSecond().isAtDropLimit()) {
			return;
		}
		
		if (settings.isKillOnlyDrops()) {
			Player player;
			if (src.getSource() instanceof Player) {
				player = (Player) src.getSource();
			} else if (src.getSource() instanceof Projectile) {
				Projectile projectile = (Projectile) src.getSource();

				Optional<UUID> optionalUUID = projectile.getCreator();

				if (!optionalUUID.isPresent()) {
					return;
				}

				Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(optionalUUID.get());

				if (!optionalPlayer.isPresent()) {
					return;
				}
				player = optionalPlayer.get();

				if (player.gameMode().get().equals(GameModes.CREATIVE) && !settings.isCreativeModeAllowed()) {
					return;
				}

				if (settings.isUsePermissions()) {
					if (!player.hasPermission("moneydrop.enable")) {
						return;
					}
				}
			} else {
				return;
			}
		}

		double basedrops = getSpecialDrop(entity);

		boolean specialDrop = (basedrops == -1);

		if (specialDrop) {
			MobDropData drops = settings.getMobData(entity);

			if (drops == null) {
				return;
			}

			if (random.nextDouble() >= drops.getDropPercent()) {
				return;
			}

			double extra = ((drops.getMax() - drops.getMin()) * random.nextDouble()) * 1000;
			basedrops = ((long) (drops.getMin() * 1000 + (extra - (extra % (settings.getPrecision() * 1000))))) / 1000.0;
		}

		MoneyDropEvent moneyDropEvent = new MoneyDropEvent(event, MoneyDrop.createMoneyStacks(settings, basedrops), specialDrop, Cause.of(NamedCause.source(Main.getPlugin())));

		if (!Sponge.getEventManager().post(moneyDropEvent)) {
			settings.getDropsPerSecond().add();
			for (MoneyStack moneyStack : moneyDropEvent.getMoneyStacks()) {
				moneyStack.drop(moneyDropEvent.getLocation());
			}
		}
	}

	@Listener
	public void onDestructEntityEventDeathPlayer(DestructEntityEvent.Death event, @First EntityDamageSource src) {
		if (!(event.getTargetEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getTargetEntity();

		Settings settings = Settings.get(player.getWorld());

		EconomyService economy = Main.getEconomy();

		double dropAmount = getSpecialDrop(player);
		boolean specialdrop = (dropAmount == -1);

		if (specialdrop) {
			MDDeathReason reason = MDDeathReason.OTHER;

			if (src.getSource() instanceof Player) {
				reason = MDDeathReason.PLAYER_ATTACK;
			} else if (src.getSource() instanceof Projectile) {
				Projectile projectile = (Projectile) src.getSource();

				Optional<UUID> optionalUUID = projectile.getCreator();

				if (!optionalUUID.isPresent()) {
					return;
				}

				Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(optionalUUID.get());

				if (optionalPlayer.isPresent()) {
					reason = MDDeathReason.PLAYER_ATTACK;
				} else {
					reason = MDDeathReason.MOB_ATTACK;
					;
				}
			} else {
				DamageType cause = src.getType();

				if (cause.equals(DamageTypes.ATTACK) || cause.equals(DamageTypes.PROJECTILE)) {
					reason = MDDeathReason.MOB_ATTACK;
				} else if (cause.equals(DamageTypes.EXPLOSIVE)) {
					reason = MDDeathReason.BLOCK_EXPLOSION;
				} else if (cause.equals(DamageTypes.CONTACT)) {
					reason = MDDeathReason.BLOCK_CONTACT;
				} else if (cause.equals(DamageTypes.DROWN)) {
					reason = MDDeathReason.DROWNING;
				} else if (cause.equals(DamageTypes.FALL)) {
					reason = MDDeathReason.FALLING;
				} else if (cause.equals(DamageTypes.FIRE)) {
					reason = MDDeathReason.FIRE;
				} else if (cause.equals(DamageTypes.SUFFOCATE)) {
					reason = MDDeathReason.SUFFOCATION;
				} else if (cause.equals(DamageTypes.GENERIC)) {
					reason = MDDeathReason.SUICIDE;
				}
			}

			PlayerDropData drops = settings.getPlayerDrops();

			BigDecimal balance = economy.getOrCreateAccount(player.getUniqueId()).get().getBalance(economy.getDefaultCurrency());
			dropAmount = drops.getDropAmount(reason, balance.doubleValue());
		}

		PlayerMoneyDropEvent playerMoneyDropEvent = new PlayerMoneyDropEvent(event, MoneyDrop.createMoneyStacks(settings, dropAmount), specialdrop, Cause.of(NamedCause.source(Main.getPlugin())));

		if (playerMoneyDropEvent.getPlayerLossAmount() != 0 && (!Sponge.getEventManager().post(playerMoneyDropEvent))) {
			MoneyDrop.giveOrTakeMoney(player, new BigDecimal(-1 * playerMoneyDropEvent.getPlayerLossAmount()));

			for (MoneyStack moneyStack : playerMoneyDropEvent.getMoneyStacks()) {
				moneyStack.drop(playerMoneyDropEvent.getLocation());
			}

			MoneyDrop.sendDeathChatMessage(Settings.get(player.getWorld()), player, playerMoneyDropEvent.getPlayerLossAmount());
		}
	}

	@Listener
	public void onSpawnEntityEvent(SpawnEntityEvent event, @First SpawnCause cause) {
		Settings settings = Settings.get(event.getTargetWorld());

		int value = -1;

		if (!settings.isVanillaSpawnerAllowed() && cause.getType().equals(SpawnTypes.MOB_SPAWNER)) {
			value = 0;
		} else if (!settings.isModSpawnerAllowed() && cause.getType().equals(SpawnTypes.CUSTOM)) {
			value = 0;
		} else if (!settings.isPluginSpawnerAllowed() && cause.getType().equals(SpawnTypes.PLUGIN)) {
			value = 0;
		} else if (!settings.isEggSpawnerAllowed() && cause.getType().equals(SpawnTypes.SPAWN_EGG)) {
			value = 0;
		}

		for (Entity entity : event.getEntities()) {
			MoneyDataManipulatorBuilder builder = (MoneyDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(MoneyData.class).get();
			MoneyData moneyData = builder.createFrom(value);
			entity.offer(moneyData).isSuccessful();
		}
	}

	private double getSpecialDrop(Living entity) {
		Optional<MoneyData> optionalMoney = entity.get(MoneyData.class);

		if (optionalMoney.isPresent()) {
			return optionalMoney.get().amount().get();
		} else {
			return -1;
		}
	}
}
