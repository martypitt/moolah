package com.mangofactory.moolah;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public interface Ledger {

	Money getBalance();
	Money getAvailableBalance();
	PostingSet getPostings();
	
	/**
	 * Returns a 'view' of postings in the given
	 * status.
	 * 
	 * The view remains live - changes made to the 
	 * ledger will be reflected in the filtered view.
	 * 
	 * @param status
	 * @return
	 */
	PostingSet getPostings(TransactionStatus status);

	CurrencyUnit getCurrency();
	TransactionStatus hold(LedgerPost posting);
	boolean hasSufficientFunds(LedgerPost posting);
	void rollback(LedgerPost posting);
	TransactionStatus commit(LedgerPost posting);
	Account getAccount();
}
