package com.mangofactory.moolah;

import org.joda.money.Money;


public interface Account {
	Ledger getLedger();
	Money getCreditLimit();
	Money getBalance();
	Object getId();
}
