package com.mangofactory.moolah;

import java.math.BigDecimal;

import lombok.Getter;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class SimpleAccount implements Account {

	@Getter
	private Ledger ledger = new BaseLedger(CurrencyUnit.AUD, this);

	@Override
	public BigDecimal getCreditLimit() {
		return BigDecimal.valueOf(100000D);
	}

	@Override
	public Money getBalance() {
		return ledger.getBalance();
	}

}
