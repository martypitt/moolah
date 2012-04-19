package com.mangofactory.moolah;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

class CashAccountLedger extends BaseLedger {

	public CashAccountLedger(CurrencyUnit currency, Account account) {
		super(currency, account);
	}
	@Override
	public Money getAvailableBalance() {
		// TODO...
		// The theory here is that the cash account
		// always can be debited.
		return Money.of(getCurrency(), 999999999L);
	}
}
