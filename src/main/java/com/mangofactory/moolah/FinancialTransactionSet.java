package com.mangofactory.moolah;

import java.util.HashSet;
import java.util.Set;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.google.common.collect.ForwardingSet;

public class FinancialTransactionSet extends ForwardingSet<FinancialTransaction> {
	private Set<FinancialTransaction> delegate;
	public FinancialTransactionSet()
	{
		this.delegate = new HashSet<FinancialTransaction>();
	}
	public FinancialTransactionSet(Set<FinancialTransaction> transactions)
	{
		this();
		addAll(transactions);
	}
	@Override
	protected Set<FinancialTransaction> delegate() {
		return delegate;
	}
	/**
	 * Returns the sum of all transactions.
	 * If isEmpty() is true, returns null.
	 * @return
	 */
	public Money sum()
	{
		Money sum = null;
		for (FinancialTransaction transaction : this)
		{
			if (sum == null)
			{
				sum = transaction.getValue();
			} else {
				sum = sum.plus(transaction.getValue());
			}
		}
		return sum;
	}
	public Money sumOrZero(CurrencyUnit currencyUnit)
	{
		if (isEmpty())
		{
			return Money.zero(currencyUnit);
		} else {
			return sum();
		}
	}
}
