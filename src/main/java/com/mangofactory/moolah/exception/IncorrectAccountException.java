package com.mangofactory.moolah.exception;

public class IncorrectAccountException extends RuntimeException {
	private static final long serialVersionUID = 4944322581339283658L;

	public IncorrectAccountException()
	{
		super("The transaction does not apply to this account");
	}
}
