package com.gmail.trentech.walletdrop.core.manipulators;

import static com.gmail.trentech.walletdrop.core.manipulators.Keys.AMOUNT;

import java.util.Optional;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

public class MoneyDataManipulatorBuilder extends AbstractDataBuilder<MoneyData> implements DataManipulatorBuilder<MoneyData, ImmutableMoneyData> {

	public MoneyDataManipulatorBuilder() {
		super(MoneyData.class, 1);
	}
	
	@Override
	protected Optional<MoneyData> buildContent(DataView container) throws InvalidDataException {
		if (!container.contains(AMOUNT.getQuery())) {
			return Optional.empty();
		}
		double amount = container.getDouble(AMOUNT.getQuery()).get();

		return Optional.of(new MoneyData(amount));
	}

	@Override
	public MoneyData create() {
		return new MoneyData(0);
	}

	public MoneyData createFrom(double amount) {
		return new MoneyData(amount);
	}

	@Override
	public Optional<MoneyData> createFrom(DataHolder dataHolder) {
		return create().fill(dataHolder);
	}
}
