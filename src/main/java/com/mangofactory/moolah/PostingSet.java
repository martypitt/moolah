package com.mangofactory.moolah;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.google.common.collect.ForwardingSet;

public class PostingSet extends ForwardingSet<Posting> {

	private Set<Posting> delegate;
	private CurrencyUnit currencyUnit;
	public PostingSet(CurrencyUnit currencyUnit) {
		this (currencyUnit, new CopyOnWriteArraySet<Posting>());
	}
	public PostingSet(CurrencyUnit currencyUnit, Set<Posting> postings)
	{
		this.delegate = postings;
		this.currencyUnit = currencyUnit;
	}
	private static final long serialVersionUID = -1569777228974250812L;

	/**
	 * Returns the sum of all the transactions - both debits and credits.
	 * 
	 * If no transactions are present, then null is returned.
	 * 
	 * @param currencyUnit
	 * @return
	 */
	public Money sum()
	{
		Money value = Money.zero(currencyUnit);
		for (Posting posting : this) {
			value = value.plus(posting.getValue());
		}
		return value;
	}
	public Money sumDebitsOnly()
	{
		Money value = Money.zero(currencyUnit);
		for (Posting posting : this) {
			if (posting.isDebit())
				value = value.plus(posting.getValue());
		}
		return value;
	}
	@Override
	protected Set<Posting> delegate() {
		return delegate;
	}
	public Set<Posting> asSet()
	{
		return new HashSet<Posting>(this);
	}
}
