package com.gmail.trentech.walletdrop.core.manipulators;

import static com.gmail.trentech.walletdrop.core.manipulators.Keys.AMOUNT;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableMoneyData extends AbstractImmutableSingleData<Double, ImmutableMoneyData, MoneyData> {

	protected ImmutableMoneyData(double value) {
		super(value, AMOUNT);
	}

	public ImmutableValue<Double> amount() {
		return getValueGetter();
	}

	@Override
	public <E> Optional<ImmutableMoneyData> with(Key<? extends BaseValue<E>> key, E value) {
		if (this.supports(key)) {
			return Optional.of(asMutable().set(key, value).asImmutable());
		} else {
			return Optional.empty();
		}
	}

	@Override
	public int getContentVersion() {
		return 1;
	}

	@Override
	protected ImmutableValue<Double> getValueGetter() {
		return Sponge.getRegistry().getValueFactory().createValue(AMOUNT, getValue()).asImmutable();
	}

	@Override
	public MoneyData asMutable() {
		return new MoneyData(this.getValue());
	}
}
