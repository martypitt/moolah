package com.mangofactory.moolah;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;

public class FinancialTransactionSetTest {

	private Account debitAccount;
	
	private Account credAccount;
	@Before
	public void setup()
	{
		debitAccount = new SimpleAccount();
		credAccount = new SimpleAccount();
	}
	
	@Test
	public void canHoldTwoSimilarTransactions()
	{
		FinancialTransactionSet set = new FinancialTransactionSet();
		set.add(TransactionBuilder.newTransaction().debit(debitAccount).credit(credAccount).amount(Money.of(CurrencyUnit.AUD, 20)).build());
		set.add(TransactionBuilder.newTransaction().debit(debitAccount).credit(credAccount).amount(Money.of(CurrencyUnit.AUD, 20)).build());
		assertThat(set.size(), equalTo(2));
	}
	
	@Test
	public void testTransactionEquality()
	{
		FinancialTransaction transactionA = TransactionBuilder.newTransaction().debit(debitAccount).credit(credAccount).amount(Money.of(CurrencyUnit.AUD, 20)).build();
		FinancialTransaction transactionB = TransactionBuilder.newTransaction().debit(debitAccount).credit(credAccount).amount(Money.of(CurrencyUnit.AUD, 20)).build();
		
		assertThat(transactionA, not(equalTo(transactionB)));
		assertThat(transactionA.hashCode(), not(equalTo(transactionB.hashCode())));
	}
}
