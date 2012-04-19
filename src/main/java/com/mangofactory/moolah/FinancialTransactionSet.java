package com.mangofactory.moolah;

import java.util.Set;

import org.joda.money.Money;

import com.google.common.collect.ForwardingSet;

public class FinancialTransactionSet extends ForwardingSet<FinancialTransaction> {
	private Set<FinancialTransaction> delegate;
	
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
}
