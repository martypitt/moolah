package com.mangofactory.moolah;

import java.math.BigDecimal;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.mangofactory.moolah.exception.IncorrectAccountException;
import com.mangofactory.moolah.exception.IncorrectCurrencyException;

@MappedSuperclass
public class BaseLedger implements Ledger {

	private BigDecimal balance;

	private CurrencyUnit currency;
	private PostingSet postings;
	private final PostingSet uncommittedPostings;
	private Account account;

	public BaseLedger(CurrencyUnit currency,Account account) {
		this.currency = currency;
		this.account = account;
		this.postings = new PostingSet(currency);
		this.uncommittedPostings = new PostingSet(currency);
		this.balance = BigDecimal.ZERO;
	}

	@Override
	public Money getBalance() {
		return Money.of(getCurrency(),balance);
	}
	@Override
	@Transient
	/**
	 * Returns the available balance.
	 * 
	 * An available balance is calculated as the current balance,
	 * minus any held (uncommitted) debits.
	 * Uncommitted credits are ignored.
	 */
	public Money getAvailableBalance()
	{
		Money uncomittedPostings = uncommittedPostings.sumDebitsOnly();
		// uncommitedPostings are a negative value, so add them.
		return getBalance().plus(uncomittedPostings).plus(account.getCreditLimit());
	}
	@SuppressWarnings("unused") // for JPA
	private void setBalance(Money balance)
	{
		this.balance = balance.getAmount();
	}
	@Override
	public TransactionStatus hold(Posting posting)
	{
		synchronized (this) {
			assertCorrectCurrency(posting);
			assertIsNotUncommittedPosting(posting);
			assertIsForThisLedger(posting);
			return processHold(posting);
		}
	}
	private void assertIsForThisLedger(Posting posting) {
		if (!posting.getLedger().equals(this))
		{
			throw new IncorrectAccountException();
		}
		
	}

	private void assertIsNotUncommittedPosting(
			Posting posting) {
		if (uncommittedPostings.contains(posting))
			throw new IllegalStateException("Transaction has already been held, but not committed");
	}


	@Override
	public TransactionStatus commit(Posting posting)
	{
		if (posting.getTransactionStatus().isErrorState())
			throw new IllegalStateException("The transaction contains an error");
		assertIsUncommittedTransaction(posting);
		doInternalPost(posting);
		assertIsForThisLedger(posting);
		uncommittedPostings.remove(posting);
		postings.add(posting);
		return TransactionStatus.COMPLETED;

	}
	private void doInternalPost(Posting posting)
	{
		this.balance = balance.add(posting.getValue().getAmount());
	}


	void assertIsUncommittedTransaction(Posting transaction) {
		if (!uncommittedPostings.contains(transaction))
		{
			throw new IllegalStateException("The supplied transaction has not been applied to this ledger");
		}
	}

	private TransactionStatus processHold(Posting posting) {
		if (!hasSufficientFunds(posting))
		{
			return TransactionStatus.REJECTED_INSUFFICIENT_FUNDS;
		}
		uncommittedPostings.add(posting);
		// TODO ... hold it, somehow
		return TransactionStatus.HELD;
	}


	private void assertCorrectCurrency(Posting posting) {
		if (!posting.getCurrencyUnit().equals(getCurrency()))
			throw new IncorrectCurrencyException(getCurrency(),posting.getCurrencyUnit());
	}

	@Override
	public boolean hasSufficientFunds(Posting posting) {
		if (posting.isCredit())
			return true;
		Money value = posting.getNegatedDebitValue();
		return getAvailableBalance().isGreaterThan(value) || getAvailableBalance().isEqual(value);
	}


	@Override
	public void rollback(Posting posting) {
		if (uncommittedPostings.contains(posting))
		{
			uncommittedPostings.remove(posting);
		} else {
			throw new IllegalStateException("Posting is not in an uncommitted state on this ledger");
		}
	} 

	@Transient
	public CurrencyUnit getCurrency() {
		return currency;
	}

	@Transient
	@Override
	public Account getAccount() {
		return account;
	}
	protected void setAccount(Account value)
	{
		if (this.account != null && !this.account.equals(value))
		{
			throw new IllegalStateException("Cannot change account once set");
		}
		this.account = value;
	}
	protected void setCurrency(CurrencyUnit value) {
		if (this.currency != null && !this.currency.equals(value))
		{
			throw new IllegalStateException("Cannot change currency once set");
		}
		this.currency = value;
	}
	
	/**
	 * TODO : Currently eagerly fetching transactions.
	 * This is a bit mixed -- we need to know the transactions for doing
	 * things like summing available balance, etc.
	 * However, loading thousands of transactions is dumb.
	 * Need to introduce something like a balance point, and we only
	 * keep persistent transactions that have occurred after the last balance point.
	 * @return
	 */
	@OneToMany(fetch=FetchType.EAGER)
	protected Set<Posting> getPersistentTransactions()
	{
		return postings.asSet();
	}
	protected void setPersistentTransactions(Set<Posting> value)
	{
		postings = new PostingSet(currency,value);
	}
	@Transient
	public PostingSet getPostings()
	{
		return postings;
	}
	@Override
	public boolean canRollback(Posting posting)
	{
		return uncommittedPostings.contains(posting);
	}
}
