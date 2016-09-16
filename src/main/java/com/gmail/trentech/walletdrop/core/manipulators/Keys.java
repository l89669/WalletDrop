package com.gmail.trentech.walletdrop.core.manipulators;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;

import com.google.common.reflect.TypeToken;

public class Keys {

	private static final TypeToken<Value<Double>> VALUE_DOUBLE = new TypeToken<Value<Double>>() {
		private static final long serialVersionUID = 6686120503668717369L;
    };
    
	public static final Key<Value<Double>> AMOUNT = KeyFactory.makeSingleKey(TypeToken.of(Double.class), VALUE_DOUBLE, DataQuery.of("amount"), "walletdrop:amount", "amount");
}
