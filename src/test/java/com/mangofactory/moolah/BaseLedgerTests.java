package com.mangofactory.moolah;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.joda.money.CurrencyUnit.AUD;
import static org.joda.money.CurrencyUnit.USD;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mangofactory.moolah.exception.IncorrectAccountException;
import com.mangofactory.moolah.exception.IncorrectCurrencyException;
import com.mangofactory.moolah.processing.FinancialTransactionController;

@RunWith(MockitoJUnitRunner.class)
public class BaseLedgerTests {

	private FinancialTransactionController controller;

	@Mock
	private Account cashAccount;

	@Mock
	private Account testAccount;

	@Before
	public void setup()
	{
		controller = new FinancialTransactionController();
		when(cashAccount.getLedger()).thenReturn(new CashAccountLedger(AUD, cashAccount));
		when(testAccount.getLedger()).thenReturn(new BaseLedger(AUD, testAccount));
	}
	@Test
	public void whenCreatedThatBalanceIsZero()
	{
		assertThat(cashAccount.getLedger().getBalance(),equalTo(Money.zero(AUD)));
	}

	@Test(expected=IncorrectCurrencyException.class)
	public void whenAddingTransactionOfWrongCurrency_that_exceptionIsThrown()
	{
		FinancialTransaction transaction = TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(USD,10))
				.build();
		hold(transaction);
	}

	@Test(expected=IllegalStateException.class)
	public void whenBothCreditorAndDebitorAreTheSame_that_transactionStateIsInvalid()
	{
		FinancialTransaction transaction = TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(cashAccount)
				.amount(Money.of(AUD,10))
				.build();
		hold(transaction);
	}

	@Test(expected=IncorrectAccountException.class)
	public void whenAddingTransactionForWrongAccount_that_exceptionIsThrown()
	{
		Account wrongAccount = mock(Account.class);
		FinancialTransaction transaction = TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(USD,10))
				.build();
		Posting posting = transaction.getPostingFor(cashAccount.getLedger());
		BaseLedger ledger = new BaseLedger(USD, wrongAccount);
		ledger.hold(posting);
	}
	@Test
	public void whenCreditingLedger_that_balanceIncreases()
	{
		FinancialTransaction transaction = controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,10)));

		assertThat(transaction.getStatus(), equalTo(TransactionStatus.COMPLETED));

		assertThat(testAccount.getLedger().getBalance(), equalTo(Money.of(AUD, 10)));
		assertThat(testAccount.getLedger().getAvailableBalance(), equalTo(Money.of(AUD, 10)));
		assertThat(cashAccount.getLedger().getBalance(), equalTo(Money.of(AUD, -10)));
	}
	@Test
	public void whenDebitingLedger_that_balanceDecreases()
	{
		controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,100)));
		assertThat(testAccount.getLedger().getBalance(), equalTo(Money.of(AUD, 100)));
		assertThat(testAccount.getLedger().getAvailableBalance(), equalTo(Money.of(AUD, 100)));
		FinancialTransaction transaction = controller.commit(TransactionBuilder.newTransaction()
				.debit(testAccount)
				.credit(cashAccount)
				.amount(Money.of(AUD,60)));

		assertThat(transaction.getStatus(), equalTo(TransactionStatus.COMPLETED));

		assertThat(testAccount.getLedger().getBalance(), equalTo(Money.of(AUD, 40)));
		assertThat(testAccount.getLedger().getAvailableBalance(), equalTo(Money.of(AUD, 40)));
		assertThat(cashAccount.getLedger().getBalance(), equalTo(Money.of(AUD, -40)));
	}

	@Test
	public void whenInsufficientFunds_that_balanceIsUnchanged()
	{
		controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,10)));

		FinancialTransaction transaction = controller.commit(TransactionBuilder.newTransaction()
				.debit(testAccount)
				.credit(cashAccount)
				.amount(Money.of(AUD,100)));

		assertThat(transaction.getStatus(), equalTo(TransactionStatus.REJECTED_INSUFFICIENT_FUNDS));
		assertThat(testAccount.getLedger().getBalance(),equalTo(Money.of(AUD,10)));
		assertThat(cashAccount.getLedger().getBalance(),equalTo(Money.of(AUD,-10)));
	}

	@Test
	public void whenCheckingAvailableBalance_thatHeldDebitsAreConsidered()
	{
		controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,1000)));
		controller.hold(TransactionBuilder.newTransaction()
				.credit(cashAccount)
				.debit(testAccount)
				.amount(Money.of(AUD,100)));
		// testAccount now has $1000 cash, and a hold on $100
		assertThat(testAccount.getLedger().getBalance(),equalTo(Money.of(AUD,1000)));
		assertThat(testAccount.getLedger().getAvailableBalance(),equalTo(Money.of(AUD,900)));

		controller.hold(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,100)));

		assertThat(testAccount.getLedger().getBalance(),equalTo(Money.of(AUD,1000)));
		// Note that credits don't appear in the available balance.
		assertThat(testAccount.getLedger().getAvailableBalance(),equalTo(Money.of(AUD,900)));
	}
	@Test
	public void whenCheckingAvailableBalance_that_heldCreditsAreNotConsidered()
	{
		controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,1000)));
		controller.hold(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,100)));
		// testAccount now has $1000 cash, with a held credit for a further $100
		assertThat(testAccount.getLedger().getBalance(),equalTo(Money.of(AUD,1000)));
		// Held credits don't count towards available balance.
		assertThat(testAccount.getLedger().getAvailableBalance(),equalTo(Money.of(AUD,1000)));
	}
	
	@Test
	public void whenExceptionIsThrownDuringCommit_that_transactionIsRolledBack()
	{
		Ledger exceptionThrowingLedger = mock(Ledger.class);
		when(exceptionThrowingLedger.hold(any(Posting.class))).thenReturn(TransactionStatus.HELD);
		when(exceptionThrowingLedger.commit(any(Posting.class))).thenThrow(NullPointerException.class);
		
		when(testAccount.getLedger()).thenReturn(exceptionThrowingLedger);
		
		FinancialTransaction transaction = controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,1000)));
		Posting posting = transaction.getPostingFor(exceptionThrowingLedger);
		verify(exceptionThrowingLedger).rollback(posting);
		assertThat(transaction.getStatus(), equalTo(TransactionStatus.INTERNAL_ERROR));
	}
	
	@Test
	public void whenExceptionIsThrownDuringHold_that_transactionIsRolledBack()
	{
		Ledger exceptionThrowingLedger = mock(Ledger.class);
		when(exceptionThrowingLedger.hold(any(Posting.class))).thenThrow(NullPointerException.class);
		when(testAccount.getLedger()).thenReturn(exceptionThrowingLedger);
		
		FinancialTransaction transaction = controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(AUD,1000)));
		Posting posting = transaction.getPostingFor(exceptionThrowingLedger);
		verify(exceptionThrowingLedger).rollback(posting);
		assertThat(transaction.getStatus(), equalTo(TransactionStatus.INTERNAL_ERROR));
	}
	private void hold(FinancialTransaction transaction) {
		for (Posting posting : transaction.getPostings())
		{
			posting.hold();
		}
	}	
	
	
	
}
