package com.gmail.trentech.walletdrop.core.manipulators;

import static com.gmail.trentech.walletdrop.core.manipulators.Keys.AMOUNT;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import com.google.common.base.Preconditions;

public class MoneyData extends AbstractSingleData<Double, MoneyData, ImmutableMoneyData> {

	protected MoneyData(double value) {
		super(value, AMOUNT);
	}

	public Value<Double> amount() {
		return getValueGetter();
	}

	@Override
	public MoneyData copy() {
		return new MoneyData(this.getValue());
	}

	@Override
	public Optional<MoneyData> fill(DataHolder dataHolder, MergeFunction mergeFn) {
		MoneyData moneyData = Preconditions.checkNotNull(mergeFn).merge(copy(), dataHolder.get(MoneyData.class).orElse(copy()));
		return Optional.of(set(AMOUNT, moneyData.get(AMOUNT).get()));
	}

	@Override
	public Optional<MoneyData> from(DataContainer container) {
		if (container.contains(AMOUNT.getQuery())) {
			return Optional.of(set(AMOUNT, container.getDouble(AMOUNT.getQuery()).orElse(getValue())));
		}
		return Optional.empty();
	}

	@Override
	public int getContentVersion() {
		return 1;
	}

	@Override
	public ImmutableMoneyData asImmutable() {
		return new ImmutableMoneyData(this.getValue());
	}

	@Override
	protected Value<Double> getValueGetter() {
		return Sponge.getRegistry().getValueFactory().createValue(AMOUNT, getValue());
	}

	@Override
	public DataContainer toContainer() {
		return super.toContainer().set(AMOUNT, getValue());
	}
}