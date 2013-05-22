package com.mangofactory.moolah;

import static com.mangofactory.moolah.TestHelpers.AUD;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mangofactory.moolah.exception.IncorrectAccountException;
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
		when(testAccount.getCreditLimit()).thenReturn(AUD(0));
	}
	@Test
	public void whenCreatedThatBalanceIsZero()
	{
		assertThat(cashAccount.getLedger().getBalance(),equalTo(Money.zero(AUD)));
	}

	@Test
	public void whenAddingTransactionOfWrongCurrency_that_transactionIsCancelled()
	{
		FinancialTransaction transaction = TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(Money.of(USD,10))
				.build();
		controller.hold(transaction);
		assertThat(transaction.getStatus(), equalTo(TransactionStatus.INTERNAL_ERROR));
		assertThat(transaction.getErrorMessage(),equalTo("Incorrect currency.  Expected AUD but got USD"));
	}

	@Test(expected=IllegalStateException.class)
	@Ignore("Unsure if this is valid")
	public void whenBothCreditorAndDebitorAreTheSame_that_transactionStateIsInvalid()
	{
		FinancialTransaction transaction = TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(cashAccount)
				.amount(Money.of(AUD,10))
				.build();
		controller.hold(transaction);
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
		LedgerPost posting = transaction.getPostingFor(cashAccount.getLedger());
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
				.amount(AUD(100)));
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
				.amount(AUD(100)));

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
				.amount(AUD(1000)));
		controller.hold(TransactionBuilder.newTransaction()
				.credit(cashAccount)
				.debit(testAccount)
				.amount(AUD(100)));
		// testAccount now has $1000 cash, and a hold on $100
		assertThat(testAccount.getLedger().getBalance(),equalTo(AUD(1000)));
		assertThat(testAccount.getLedger().getAvailableBalance(),equalTo(Money.of(AUD,900)));

		controller.hold(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(AUD(100)));

		assertThat(testAccount.getLedger().getBalance(),equalTo(AUD(1000)));
		// Note that credits don't appear in the available balance.
		assertThat(testAccount.getLedger().getAvailableBalance(),equalTo(Money.of(AUD,900)));
	}
	@Test
	public void whenCheckingAvailableBalance_that_heldCreditsAreNotConsidered()
	{
		controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(AUD(1000)));
		controller.hold(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(AUD(100)));
		// testAccount now has $1000 cash, with a held credit for a further $100
		assertThat(testAccount.getLedger().getBalance(),equalTo(AUD(1000)));
		// Held credits don't count towards available balance.
		assertThat(testAccount.getLedger().getAvailableBalance(),equalTo(AUD(1000)));
	}
	
	@Test
	public void whenExceptionIsThrownDuringCommit_that_transactionIsRolledBack()
	{
		Ledger exceptionThrowingLedger = mock(Ledger.class);
		when(exceptionThrowingLedger.hold(any(LedgerPost.class))).thenReturn(TransactionStatus.HELD);
		when(exceptionThrowingLedger.commit(any(LedgerPost.class))).thenThrow(NullPointerException.class);
		when(testAccount.getLedger()).thenReturn(exceptionThrowingLedger);
		
		FinancialTransaction transaction = controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(AUD(1000)));	
		
		LedgerPost posting = transaction.getPostingFor(exceptionThrowingLedger);
		verify(exceptionThrowingLedger).rollback(posting);
		assertThat(transaction.getStatus(), equalTo(TransactionStatus.INTERNAL_ERROR));
	}
	
	@Test
	public void whenExceptionIsThrownDuringHold_that_transactionIsRolledBack()
	{
		Ledger exceptionThrowingLedger = mock(Ledger.class);
		when(exceptionThrowingLedger.hold(any(LedgerPost.class))).thenThrow(NullPointerException.class);
		when(testAccount.getLedger()).thenReturn(exceptionThrowingLedger);
		FinancialTransaction transaction = controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(AUD(1000)));
		LedgerPost posting = transaction.getPostingFor(exceptionThrowingLedger);
		verify(exceptionThrowingLedger).rollback(posting);
		assertThat(transaction.getStatus(), equalTo(TransactionStatus.INTERNAL_ERROR));
	}
	
	@Test
	public void testFilteringOfPostings()
	{
		controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(AUD(1000)));
		controller.hold(TransactionBuilder.newTransaction()
				.credit(cashAccount)
				.debit(testAccount)
				.amount(AUD(100)));
		
		assertThat(testAccount.getLedger().getPostings().size(), equalTo(2));
		PostingSet heldTransactions = testAccount.getLedger().getPostings(TransactionStatus.HELD);
		PostingSet completedTransactions = testAccount.getLedger().getPostings(TransactionStatus.COMPLETED);
		
		assertThat(heldTransactions.size(), equalTo(1));
		assertThat(completedTransactions.size(), equalTo(1));
		assertThat(heldTransactions.sum(), equalTo(Money.of(AUD, -100)));
		assertThat(completedTransactions.sum(), equalTo(Money.of(AUD, 1000)));
		
		
		// The "view" should remain 'live' -- ie., updates to the 
		// source should be reflected in the view.
		// Event though the filtered sets are already defined,
		// adding new transactions to the underlying set should still be
		// reflected in the view.
		controller.commit(TransactionBuilder.newTransaction()
				.debit(cashAccount)
				.credit(testAccount)
				.amount(AUD(1000)));
		controller.hold(TransactionBuilder.newTransaction()
				.credit(cashAccount)
				.debit(testAccount)
				.amount(AUD(100)));
		
		assertThat(heldTransactions.size(), equalTo(2));
		assertThat(completedTransactions.size(), equalTo(2));
		assertThat(heldTransactions.sum(), equalTo(Money.of(AUD, -200)));
		assertThat(completedTransactions.sum(), equalTo(Money.of(AUD, 2000)));

	}
}
