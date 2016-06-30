package com.gmail.trentech.MoneyDrop.data;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;

public class Keys {

	public static final Key<Value<Double>> AMOUNT = KeyFactory.makeSingleKey(Double.class, Value.class, DataQuery.of("amount"));
}
