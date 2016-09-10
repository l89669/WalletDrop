package com.gmail.trentech.walletdrop.core;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.Hopper;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.service.economy.EconomyService;

import com.gmail.trentech.walletdrop.Main;
import com.gmail.trentech.walletdrop.api.MoneyStack;
import com.gmail.trentech.walletdrop.api.WalletDrop;
import com.gmail.trentech.walletdrop.api.events.WalletDropEvent;
import com.gmail.trentech.walletdrop.api.events.WalletPickupEvent;
import com.gmail.trentech.walletdrop.core.data.MobDropData;
import com.gmail.trentech.walletdrop.core.data.PlayerDropData;
import com.gmail.trentech.walletdrop.core.data.PlayerDropData.MDDeathReason;
import com.gmail.trentech.walletdrop.core.manipulators.MoneyData;
import com.gmail.trentech.walletdrop.core.manipulators.MoneyDataManipulatorBuilder;
import com.gmail.trentech.walletdrop.core.utils.Settings;

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

		ItemStack itemStack = item.item().get().createStack();
		
		double amount = WalletDrop.getItemStackValue(itemStack);

		if (amount == 0) {
			return;
		}

		Settings settings = Settings.get(item.getWorld());
		
		if(settings.isHopperAllowed()) {
			return;
		}
		
		hopper.getInventory().query(itemStack).clear();
	}

	@Listener(order = Order.POST)
	public void onChangeInventoryEventPickup(ChangeInventoryEvent.Pickup event, @First Player player) {
		for (SlotTransaction transaction : event.getTransactions()) {
			ItemStack itemStack = transaction.getFinal().createStack();

			double amount = WalletDrop.getItemStackValue(itemStack);

			if (amount == 0) {
				continue;
			}

			Settings settings = Settings.get(player.getWorld());

			if (player.gameMode().get().equals(GameModes.CREATIVE) && !settings.isCreativeModeAllowed()) {
				event.setCancelled(true);
				return;
			}

			if (settings.isUsePermissions()) {
				if (!player.hasPermission("walletdrop.enable")) {
					return;
				}
			}

			WalletPickupEvent moneyPickupEvent = new WalletPickupEvent(itemStack, amount, Cause.of(NamedCause.source(player)));

			if (!Sponge.getEventManager().post(moneyPickupEvent)) {
				Sponge.getScheduler().createTaskBuilder().delayTicks(2).execute(c -> {
					player.getInventory().query(itemStack).clear();
				}).submit(Main.getPlugin());			
				
				Optional<EconomyService> optionalEconomy = Sponge.getServiceManager().provide(EconomyService.class);
				
				if(!optionalEconomy.isPresent()) {
					Main.instance().getLog().error("Economy plugin not found");
					return;
				}		
				EconomyService economy = optionalEconomy.get();
				
				WalletDrop.depositOrWithdraw(player, economy, new BigDecimal(moneyPickupEvent.getValue()));

				WalletDrop.sendPickupChatMessage(player, amount);
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
			} else {
				return;
			}
			
			if (player.gameMode().get().equals(GameModes.CREATIVE) && !settings.isCreativeModeAllowed()) {
				return;
			}

			if (settings.isUsePermissions()) {
				if (!player.hasPermission("walletdrop.enable")) {
					return;
				}
			}
		}

		double dropAmount = 0;
		
		boolean validDrop = isValidDrop(entity);		
		
		if (validDrop) {
			MobDropData drops = settings.getMobData(entity);

			if (drops == null) {
				return;
			}

			if (random.nextDouble() >= drops.getDropPercent()) {
				return;
			}

			double extra = ((drops.getMax() - drops.getMin()) * random.nextDouble()) * 1000;
			dropAmount = ((long) (drops.getMin() * 1000 + (extra - (extra % (settings.getPrecision() * 1000))))) / 1000.0;
		}

		WalletDropEvent walletDropEvent = new WalletDropEvent(WalletDrop.createMoneyStacks(settings, dropAmount), entity);

		if (!Sponge.getEventManager().post(walletDropEvent)) {
			settings.getDropsPerSecond().add();
			
			for (MoneyStack moneyStack : walletDropEvent.getMoneyStacks()) {
				moneyStack.drop(walletDropEvent.getLocation());
			}
		}
	}

	@Listener(order = Order.PRE)
	public void onDestructEntityEventDeathPlayer(DestructEntityEvent.Death event, @First EntityDamageSource src) {
		if (!(event.getTargetEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getTargetEntity();

		Settings settings = Settings.get(player.getWorld());

		MDDeathReason reason = MDDeathReason.GENERIC;

		if (src.getSource() instanceof Player) {
			reason = MDDeathReason.PLAYER;
		} else if (src.getSource() instanceof Projectile) {
			Projectile projectile = (Projectile) src.getSource();

			Optional<UUID> optionalUUID = projectile.getCreator();

			if (!optionalUUID.isPresent()) {
				return;
			}

			Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(optionalUUID.get());

			if (optionalPlayer.isPresent()) {
				reason = MDDeathReason.PLAYER;
			} else {
				reason = MDDeathReason.PROJECTILE;
			}
		} else {
			DamageType cause = src.getType();
			reason = MDDeathReason.valueOf(cause.getName().toUpperCase());
		}

		PlayerDropData drops = settings.getPlayerDrops();

		Optional<EconomyService> optionalEconomy = Sponge.getServiceManager().provide(EconomyService.class);
		
		if(!optionalEconomy.isPresent()) {
			Main.instance().getLog().error("Economy plugin not found");
			return;
		}		
		EconomyService economy = optionalEconomy.get();

		BigDecimal balance = economy.getOrCreateAccount(player.getUniqueId()).get().getBalance(economy.getDefaultCurrency());
		double dropAmount = drops.getDropAmount(reason, balance.doubleValue());

		WalletDropEvent.Player playerWalletDropEvent = new WalletDropEvent.Player(WalletDrop.createMoneyStacks(settings, dropAmount), player);

		if (playerWalletDropEvent.getDropAmount() != 0 && (!Sponge.getEventManager().post(playerWalletDropEvent))) {
			WalletDrop.depositOrWithdraw(player, economy, new BigDecimal(-1 * playerWalletDropEvent.getDropAmount()));

			for (MoneyStack moneyStack : playerWalletDropEvent.getMoneyStacks()) {
				moneyStack.drop(playerWalletDropEvent.getLocation());
			}

			WalletDrop.sendDeathChatMessage(Settings.get(player.getWorld()), player, playerWalletDropEvent.getDropAmount());
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

	private boolean isValidDrop(Living entity) {
		Optional<MoneyData> optionalMoney = entity.get(MoneyData.class);

		if (optionalMoney.isPresent()) {
			return true;
		}
		
		return false;
	}
}
