package com.mangofactory.moolah;

/**
 * An AccountOwner is typically an entity
 * that has an account - ie., can be associated
 * with financial transactions, but is not directly
 * an account
 * 
 * @author martypitt
 *
 */
public interface AccountOwner {
	public Account getAccount();
}
