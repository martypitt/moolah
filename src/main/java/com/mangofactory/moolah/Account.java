package com.mangofactory.moolah;

import java.math.BigDecimal;

import org.joda.money.Money;


public interface Account {
	Ledger getLedger();
	Money getCreditLimit();
	Money getBalance();
}
