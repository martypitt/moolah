package com.mangofactory.moolah.processing;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mangofactory.moolah.FinancialTransaction;
import com.mangofactory.moolah.TransactionStatus;

public class TransactionSet extends ForwardingSet<FinancialTransaction> {

	private Set<FinancialTransaction> delegate = Sets.newHashSet();
	@Override
	protected Set<FinancialTransaction> delegate() {
		return delegate;
	}
	
	public boolean hasErrors()
	{
		for (FinancialTransaction transaction : this)
		{
			if (transaction.getStatus().isErrorState())
				return true;
		}
		return false;
	}
	
	public String getErrorMessages()
	{
		StringBuilder sb = new StringBuilder();
		for (TransactionStatus error : getErrors())
		{
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(error.toString());
		}
		return sb.toString();
	}
	public List<TransactionStatus> getErrors()
	{
		List<TransactionStatus> result = Lists.newArrayList();
		for (FinancialTransaction transaction : this)
		{
			if (transaction.getStatus().isErrorState())
				result.add(transaction.getStatus());
		}
		return result;
	}
	

	public boolean hasAllWithStatus(TransactionStatus status) {
		for (FinancialTransaction transaction : this)
		{
			if (!transaction.getStatus().equals(status))
				return false;
		}
		return true;
	}
	public boolean hasOneWithStatus(TransactionStatus status) {
		for (FinancialTransaction transaction : this)
		{
			if (transaction.getStatus().equals(status))
				return true;
		}
		return false;
	}

}
