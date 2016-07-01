package com.gmail.trentech.MoneyDrop;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
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

	private static ThreadLocalRandom random = ThreadLocalRandom.current();

	@Listener
	public void onWorldLoad(LoadWorldEvent event) {
		Settings.initSettings(event.getTargetWorld());
	}

	@Listener
	public void onDestructEntityEventItem(DestructEntityEvent event, @First Player player) {
		if (event.getTargetEntity() instanceof Item) {
			Item item = (Item) event.getTargetEntity();
			Settings settings = Settings.get(item.getWorld());

			if (!settings.getItemType().equals(item.getItemType())) {
				return;
			}

			ItemStack itemStack = item.item().get().createStack();

			double amount = getMoney(item.item().get().createStack());

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

			MoneyPickupEvent mpEvent = new MoneyPickupEvent(player, itemStack, amount, Cause.of(NamedCause.source(MoneyDrop.getPlugin())));

			if (!MoneyDrop.getGame().getEventManager().post(mpEvent)) {
				player.getInventory().query(itemStack).clear();

				giveOrTakeMoney(player, new BigDecimal(mpEvent.getValue()));

				sendPickupChatMessage(Settings.get(player.getWorld()), player, amount);
			}
		}
	}

	// @Listener
	public void onChangeInventoryEventPickup(ChangeInventoryEvent.Pickup event, @First Player player) {
		for (Transaction<ItemStackSnapshot> snapshot : event.getTransactions()) {
			ItemStack itemStack = snapshot.getOriginal().createStack();

			double amount = getMoney(itemStack);

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

			MoneyPickupEvent mpEvent = new MoneyPickupEvent(player, itemStack, amount, Cause.of(NamedCause.source(MoneyDrop.getPlugin())));

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

		MoneyDropEvent moneyDropEvent = new MoneyDropEvent(event, getMoneyStacks(settings, basedrops), specialDrop, Cause.of(NamedCause.source(MoneyDrop.getPlugin())));

		if (!MoneyDrop.getGame().getEventManager().post(moneyDropEvent)) {
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

		PlayerMoneyDropEvent playerMoneyDropEvent = new PlayerMoneyDropEvent(event, getMoneyStacks(settings, dropAmount), specialdrop, Cause.of(NamedCause.source(MoneyDrop.getPlugin())));

		if (playerMoneyDropEvent.getPlayerLossAmount() != 0 && (!MoneyDrop.getGame().getEventManager().post(playerMoneyDropEvent))) {
			giveOrTakeMoney(player, new BigDecimal(-1 * playerMoneyDropEvent.getPlayerLossAmount()));

			for (MoneyStack moneyStack : playerMoneyDropEvent.getMoneyStacks()) {
				moneyStack.drop(playerMoneyDropEvent.getLocation());
			}

			sendDeathChatMessage(Settings.get(player.getWorld()), player, playerMoneyDropEvent.getPlayerLossAmount());
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

	private List<MoneyStack> getMoneyStacks(Settings settings, double amount) {
		List<MoneyStack> moneyStacks = new ArrayList<>();

		double split = settings.getMaxStackValue();

		ItemStack itemStack = ItemStack.builder().quantity(1).itemType(settings.getItemType()).build();
		
		int unsafeDamage = settings.getItemUnsafeDamage();
		
		if(unsafeDamage > 0) {
			DataContainer container = itemStack.toContainer();
			DataQuery query = DataQuery.of('/', "UnsafeDamage");
			container.set(query, unsafeDamage);
			
			itemStack = ItemStack.builder().fromContainer(container).build();
		}

		if (settings.isIndependentDrops()) {
			while (amount > 0) {
				if (amount > split) {
					moneyStacks.add(new MoneyStack(itemStack, split));
					amount -= split;
				} else {
					moneyStacks.add(new MoneyStack(itemStack, amount));
					amount = 0;
				}
			}
		} else {
			moneyStacks.add(new MoneyStack(itemStack, amount));
		}

		return moneyStacks;
	}

	private double getMoney(ItemStack itemStack) {
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
		if (settings.isPickupChatNotification()) {
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
