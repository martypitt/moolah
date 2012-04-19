package com.mangofactory.moolah;

import java.math.BigDecimal;

import org.joda.money.Money;


public interface Account {
	Ledger getLedger();
	BigDecimal getCreditLimit();
	Money getBalance();
}
