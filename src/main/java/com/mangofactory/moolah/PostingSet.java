package com.mangofactory.moolah;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Sets;

public class PostingSet extends ForwardingSet<LedgerPost> {

	private Set<LedgerPost> delegate;
	private CurrencyUnit currencyUnit;
	public PostingSet(CurrencyUnit currencyUnit) {
		this (currencyUnit, new HashSet<LedgerPost>());
	}
	public PostingSet(CurrencyUnit currencyUnit, Set<LedgerPost> postings)
	{
		this.delegate = postings;
		this.currencyUnit = currencyUnit;
	}
	private static final long serialVersionUID = -1569777228974250812L;

	/**
	 * Returns the sum of all the transactions - both debits and credits.
	 * 
	 * @param currencyUnit
	 * @return
	 */
	public Money sum()
	{
		Money value = Money.zero(currencyUnit);
		for (LedgerPost posting : this) {
			value = value.plus(posting.getValue());
		}
		return value;
	}
	public boolean isBalanced()
	{
		return sum().isZero();
	}
	public Money sumDebitsOnly()
	{
		Money value = Money.zero(currencyUnit);
		for (LedgerPost posting : this) {
			if (posting.isDebit())
				value = value.plus(posting.getValue());
		}
		return value;
	}
	@Override
	protected Set<LedgerPost> delegate() {
		return delegate;
	}
	public Set<LedgerPost> asSet()
	{
		return new HashSet<LedgerPost>(this);
	}
	public PostingSet inStatus(final TransactionStatus status)
	{
		final Predicate<LedgerPost> statusMatcher = new Predicate<LedgerPost>() {
			@Override
			public boolean apply(@Nullable LedgerPost input) {
				return input.getTransactionStatus() == status;
			}
		};
		return new PostingSet(currencyUnit,Sets.filter(this, statusMatcher));
	}
	public Money sumCreditsOnly() {
		Money value = Money.zero(currencyUnit);
		for (LedgerPost posting : this) {
			if (posting.isCredit())
				value = value.plus(posting.getValue());
		}
		return value;
	}
}
