package com.mangofactory.moolah.util;

import org.joda.money.Money;

public class MoneyUtils {


	/**
	 * Similar to calling equal, however allows for null values.
	 * 
	 * A zero balance is considered equal to null.
	 * @param valueA
	 * @param valueB
	 * @return
	 */
	public static boolean areSame(Money valueA, Money valueB)
	{
		if (valueA == null && valueB == null)
			return true;
		if (valueA == null)
		{
			return valueB.isZero();
		}
		if (valueB == null)
		{
			return valueA.isZero();
		}
		return valueA.equals(valueB);
	}
}
