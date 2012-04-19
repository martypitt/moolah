package com.mangofactory.moolah.exception;

public class UnbalancedTransactionException extends RuntimeException {

	public UnbalancedTransactionException()
	{
		super("The transaction is not balanced.  The sum of debits and sum of credits must match");
	}
}
