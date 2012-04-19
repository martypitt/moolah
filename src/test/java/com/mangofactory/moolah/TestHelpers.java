package com.mangofactory.moolah;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class TestHelpers {

	public static Money AUD(Integer value)
	{
		return Money.of(CurrencyUnit.AUD,value);
	}
}
