package com.mangofactory.moolah;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public interface Ledger {

	Money getBalance();
	Money getAvailableBalance();
	PostingSet getPostings();
	CurrencyUnit getCurrency();
	TransactionStatus hold(LedgerPost posting);
	boolean hasSufficientFunds(LedgerPost posting);
	void rollback(LedgerPost posting);
	TransactionStatus commit(LedgerPost posting);
	Account getAccount();
	boolean canRollback(LedgerPost posting);
}
