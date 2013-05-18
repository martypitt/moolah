package com.mangofactory.moolah;

public enum TransactionStatus {

	NOT_STARTED(false),
	VALID_ENQUIRY(false),
	HELD(false),
	REJECTED_INSUFFICIENT_FUNDS(true),
	INVALID(true),
	INTERNAL_ERROR(true),
	ROLLED_BACK(false),
	COMPLETED(false);

	private boolean isError;
	private TransactionStatus(boolean isError)
	{
		this.isError = isError; 
	}
	public boolean isErrorState() {
		return isError;
	}
}
