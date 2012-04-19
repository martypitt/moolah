package com.mangofactory.moolah;

public enum TransactionStatus {

	NOT_STARTED(false),
	ENQUIRY_ONLY(false),
	HELD(false),
	REJECTED_INSUFFICIENT_FUNDS(true),
	INVALID(true),
	INTERNAL_ERROR(true),
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
