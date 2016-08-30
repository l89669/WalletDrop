package com.gmail.trentech.walletdrop.api;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.gmail.trentech.walletdrop.Main;
import com.gmail.trentech.walletdrop.core.manipulators.MoneyData;
import com.gmail.trentech.walletdrop.core.utils.Settings;

public class WalletDrop {

	public static List<MoneyStack> createMoneyStacks(Settings settings, double amount) {
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

		if (split != 0) {
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

	public static double getItemStackValue(ItemStack itemStack) {
		Optional<MoneyData> optionalData = itemStack.get(MoneyData.class);

		if (optionalData.isPresent()) {
			MoneyData moneyData = optionalData.get();

			return moneyData.amount().get();
		}
		return 0;
	}
	
	public static void depositOrWithdraw(Player player, EconomyService economy, BigDecimal amount) {
		UniqueAccount account = economy.getOrCreateAccount(player.getUniqueId()).get();

		if (amount.compareTo(BigDecimal.ZERO) > 0) {
			account.deposit(economy.getDefaultCurrency(), amount, Cause.of(NamedCause.source(Main.instance().getPlugin())));
		} else if (amount.compareTo(BigDecimal.ZERO) < 0) {

			BigDecimal pocket = account.getBalance(economy.getDefaultCurrency());
			if (pocket.add(amount).compareTo(BigDecimal.ZERO) < 0) {
				account.withdraw(economy.getDefaultCurrency(), pocket, Cause.of(NamedCause.source(Main.instance().getPlugin())));
			} else {
				account.withdraw(economy.getDefaultCurrency(), amount, Cause.of(NamedCause.source(Main.instance().getPlugin())));
			}
		}
	}

	public static void sendDeathChatMessage(Settings settings, Player player, double amount) {
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
	
	public static void sendPickupChatMessage(Player player, double amount) {
		Main.instance().getNotificationManager().addNotification(player, amount);
	}
}
