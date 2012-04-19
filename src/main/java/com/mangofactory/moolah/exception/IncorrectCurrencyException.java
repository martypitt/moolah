package com.mangofactory.moolah.exception;

import org.joda.money.CurrencyUnit;

public class IncorrectCurrencyException extends RuntimeException {

	private static final long serialVersionUID = 7364842406541168680L;

	public IncorrectCurrencyException(CurrencyUnit expected,CurrencyUnit actual)
	{
		super("Incorrect currency.  Expected " + expected.toString() + " but got " + actual.toString());
	}
}
