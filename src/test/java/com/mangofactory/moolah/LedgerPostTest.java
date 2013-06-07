package com.mangofactory.moolah;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.TreeSet;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;

public class LedgerPostTest {
private Account debitAccount;
	
	private Account credAccount;
	@Before
	public void setup()
	{
		debitAccount = new SimpleAccount();
		credAccount = new SimpleAccount();
	}
	
	@Test
	public void givenTwoPostsAtSameTime_that_treeSetContainsBoth()
	{
		FinancialTransaction transaction = TransactionBuilder.newTransaction().debit(debitAccount).credit(credAccount).amount(Money.of(CurrencyUnit.AUD, 20)).build();
		Set<LedgerPost> ledgerPosts = transaction.getLedgerPosts();
		assertThat(ledgerPosts.size(), equalTo(2));
		TreeSet<LedgerPost> sortedPosts = new TreeSet<LedgerPost>(ledgerPosts);
		assertThat(sortedPosts.size(), equalTo(2));
	}
}
