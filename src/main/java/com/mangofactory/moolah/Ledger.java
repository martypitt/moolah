package com.mangofactory.moolah;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public interface Ledger {

	Money getBalance();
	Money getAvailableBalance();
	PostingSet getPostings();
	CurrencyUnit getCurrency();
	TransactionStatus hold(Posting posting);
	boolean hasSufficientFunds(Posting posting);
	void rollback(Posting posting);
	TransactionStatus commit(Posting posting);
	Account getAccount();
	boolean canRollback(Posting posting);
}
