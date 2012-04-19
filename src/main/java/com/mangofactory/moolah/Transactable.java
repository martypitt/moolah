package com.mangofactory.moolah;

/**
 * A Transactable class is a thing that
 * has a transaction associated with it.
 * @author martypitt
 *
 */
public interface Transactable {

	FinancialTransaction getTransaction();
}
