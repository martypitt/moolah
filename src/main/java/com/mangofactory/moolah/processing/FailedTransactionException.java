package com.mangofactory.moolah.processing;

import com.mangofactory.moolah.TransactionStatus;

public class FailedTransactionException extends RuntimeException {

	private final TransactionStatus status;

	public FailedTransactionException(TransactionStatus status) {
		super(status.name());
		this.status = status;
	}

	public TransactionStatus getStatus() {
		return status;
	}

}
