package com.mangofactory.moolah;

import lombok.Getter;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class SimpleAccount implements Account {

	@Getter
	private Ledger ledger = new BaseLedger(CurrencyUnit.AUD, this);

	@Override
	public Money getCreditLimit() {
		return Money.of(CurrencyUnit.AUD, 0);
	}

	@Override
	public Money getBalance() {
		return ledger.getBalance();
	}

	@Override
	public Object getId() {
		return "Simple account";
	}

}
