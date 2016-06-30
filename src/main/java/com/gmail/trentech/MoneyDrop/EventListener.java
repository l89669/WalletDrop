package com.gmail.trentech.MoneyDrop;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
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
import org.spongepowered.api.event.item.inventory.AffectItemStackEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.gmail.trentech.MoneyDrop.PlayerDropData.MDDeathReason;
import com.gmail.trentech.MoneyDrop.data.MoneyData;
import com.gmail.trentech.MoneyDrop.data.MoneyDataManipulatorBuilder;
import com.gmail.trentech.MoneyDrop.events.MoneyDropEvent;
import com.gmail.trentech.MoneyDrop.events.MoneyPickupEvent;
import com.gmail.trentech.MoneyDrop.events.PlayerMoneyDropEvent;
import com.gmail.trentech.MoneyDrop.utils.Settings;

public class EventListener {

	private Random random = new Random();

	@Listener
	public void onWorldLoad(LoadWorldEvent event) {
		Settings.initSettings(event.getTargetWorld());
	}

	@Listener
	public void onAffectItemStackEvent(AffectItemStackEvent event, @First Player player) {
		for (Transaction<ItemStackSnapshot> transaction : event.getTransactions()) {
			ItemStack itemStack = transaction.getOriginal().createStack();

			double amount = itemToCash(itemStack);

			if (amount == 0) {
				return;
			}

			Settings settings = Settings.get(player.getWorld());
			
			if (player.gameMode().get().equals(GameModes.CREATIVE) && !settings.isCreativeModeAllowed()) {
				return;				
			}
			
			if (settings.isUsePermissions()) {
				if (!player.hasPermission("moneydrop.enable")) {
					return;
				}
			}
			
			MoneyPickupEvent mpEvent = new MoneyPickupEvent(player, itemStack, amount);

			if (!MoneyDrop.getGame().getEventManager().post(mpEvent)) {
				player.getInventory().query(itemStack).clear();

				giveOrTakeMoney(player, new BigDecimal(mpEvent.getValue()));

				sendPickupChatMessage(Settings.get(player.getWorld()), player, amount);
			}
		}
	}

	@Listener
	public void onChangeInventoryEventPickup(ChangeInventoryEvent.Pickup event, @First Player player) {
		for (Transaction<ItemStackSnapshot> snapshot : event.getTransactions()) {
			ItemStack itemStack = snapshot.getOriginal().createStack();

			double amount = itemToCash(itemStack);

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
			
			MoneyPickupEvent mpEvent = new MoneyPickupEvent(player, itemStack, amount);

			if (!MoneyDrop.getGame().getEventManager().post(mpEvent)) {
				player.getInventory().query(itemStack).clear();

				giveOrTakeMoney(player, new BigDecimal(mpEvent.getValue()));

				sendPickupChatMessage(Settings.get(player.getWorld()), player, amount);
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

		if (settings.isKillOnlyDrops()) {
			if (!(src.getSource() instanceof Player)) {
				return;
			}
			Player player = (Player) src.getSource();

			if (player.gameMode().get().equals(GameModes.CREATIVE) && !settings.isCreativeModeAllowed()) {
				return;
			}

			if (settings.isUsePermissions()) {
				if (!player.hasPermission("moneydrop.enable")) {
					return;
				}
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

		MoneyStack moneyStack = new MoneyStack(ItemStack.builder().itemType(settings.getItemType()).build(), basedrops);
		
		MoneyDropEvent moneyDropEvent = new MoneyDropEvent(event, moneyStack, specialDrop);

		if (!MoneyDrop.getGame().getEventManager().post(moneyDropEvent)) {
			moneyDropEvent.getMoneyStack().drop(moneyDropEvent.getDropLocation());
		}
	}

	@Listener
	public void onDestructEntityEventDeathPlayer(DestructEntityEvent.Death event, @First EntityDamageSource src) {
		if (!(event.getTargetEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getTargetEntity();

		Settings settings = Settings.get(player.getWorld());

		EconomyService economy = MoneyDrop.getEconomy();

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
					reason = MDDeathReason.MOB_ATTACK;;
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

		MoneyStack moneyStack = new MoneyStack(ItemStack.builder().itemType(settings.getItemType()).build(), dropAmount);

		PlayerMoneyDropEvent pmdEvent = new PlayerMoneyDropEvent(event, moneyStack, specialdrop);

		if (pmdEvent.getPlayerLossAmount() != 0 && (!MoneyDrop.getGame().getEventManager().post(pmdEvent))) {
			giveOrTakeMoney(player, new BigDecimal(-1 * pmdEvent.getPlayerLossAmount()));

			pmdEvent.getMoneyStack().drop(pmdEvent.getDropLocation());

			sendDeathChatMessage(Settings.get(player.getWorld()), player, pmdEvent.getPlayerLossAmount());
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
			MoneyDataManipulatorBuilder builder = (MoneyDataManipulatorBuilder) MoneyDrop.getGame().getDataManager().getManipulatorBuilder(MoneyData.class).get();
			MoneyData moneyData = builder.createFrom(value);
			entity.offer(moneyData).isSuccessful();
		}
	}

	private double itemToCash(ItemStack itemStack) {
		Optional<MoneyData> optionalData = itemStack.get(MoneyData.class);

		if (optionalData.isPresent()) {
			MoneyData moneyData = optionalData.get();

			return moneyData.amount().get();
		}
		return 0;
	}

	private double getSpecialDrop(Living entity) {
		Optional<MoneyData> optionalMoney = entity.get(MoneyData.class);

		if (optionalMoney.isPresent()) {
			return optionalMoney.get().amount().get();
		} else {
			return -1;
		}
	}

	private void giveOrTakeMoney(Player player, BigDecimal amount) {
		EconomyService economy = MoneyDrop.getEconomy();

		UniqueAccount account = economy.getOrCreateAccount(player.getUniqueId()).get();

		if (amount.compareTo(BigDecimal.ZERO) > 0) {
			account.deposit(economy.getDefaultCurrency(), amount, Cause.of(NamedCause.source(MoneyDrop.getPlugin())));
		} else if (amount.compareTo(BigDecimal.ZERO) < 0) {

			BigDecimal pocket = account.getBalance(economy.getDefaultCurrency());
			if (pocket.add(amount).compareTo(BigDecimal.ZERO) < 0) {
				account.withdraw(economy.getDefaultCurrency(), pocket, Cause.of(NamedCause.source(MoneyDrop.getPlugin())));
			} else {
				account.withdraw(economy.getDefaultCurrency(), amount, Cause.of(NamedCause.source(MoneyDrop.getPlugin())));
			}
		}
	}

	private void sendDeathChatMessage(Settings settings, Player player, double amount) {
		if (settings.isDeathChatNotification()) {
			double money;
			if (amount % 1 == 0) {
				money = amount;
			} else {
				money = (amount * 1000) / 1000.0;
			}
			String deathmessage = settings.getDeathChatMessage().replaceAll("<money>", new DecimalFormat("#,###,##0.00").format(money));
			player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(deathmessage));
		}
	}
	
	private void sendPickupChatMessage(Settings settings, Player player, double amount) {
		if(settings.isPickupChatNotification()) {
			String message = settings.getPickupChatMessage();
			double money;
			if (amount % 1 == 0) {
				money = amount;
			} else {
				money = (amount * 1000) / 1000.0;
			}
			message = message.replaceAll("<money>", new DecimalFormat("#,###,##0.00").format(money));
			player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
		}
	}
}
