package com.mangofactory.moolah.processing;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mangofactory.moolah.FinancialTransaction;
import com.mangofactory.moolah.LedgerPost;
import com.mangofactory.moolah.Transactable;
import com.mangofactory.moolah.TransactionBuilder;
import com.mangofactory.moolah.TransactionStatus;

public class FinancialTransactionController {

	private static final Logger log = LoggerFactory.getLogger(FinancialTransactionController.class);
	/**
	 * Commits a transaction, using two-phases - HOLD and COMMIT.
	 * 
	 * If the transaction has already been previously held, 
	 * (by calling hold()) then
	 * only a commit is performed.
	 * 
	 * If the transaction fails during processing, it is rolled back.
	 * The transaction's status will reflect the reason for the failure.
	 * 
	 * @param transaction
	 */
	public void commit(FinancialTransaction transaction)
	{
		synchronized (transaction) {
			if (transaction.getStatus().isErrorState())
			{
				throw new FailedTransactionException(transaction.getStatus());
			}
			
			if (transaction.getStatus().equals(TransactionStatus.NOT_STARTED))
				hold(transaction);
			
			if (transaction.getStatus().isErrorState())
				return;
			
			internalCommit(transaction);	
		}
		return;
	}
	public void commit(Collection<? extends Transactable> transactables)
	{
		for (Transactable transactable : transactables) {
			commit(transactable);
		}
	}
	public void commit(Transactable transactable)
	{
		commit(transactable.getTransaction());
	}
	
	public FinancialTransaction commit(TransactionBuilder builder)
	{
		FinancialTransaction transaction = builder.build();
		commit(transaction);
		return transaction;
	}
	
	
	/**
	 * Applies the transaction to the two ledgers,
	 * placing the funds into a 'held' state.
	 * 
	 * The transaction has not been committed at this stage.
	 * 
	 * If the transaction fails on either side (debit or credit)
	 * it is rolled back before this method returns, and the failing
	 * status is returned.
	 * 
	 */
	public void hold(FinancialTransaction transaction) {
		TransactionStatus transactionStatus = transaction.getStatus();
		for (LedgerPost posting : transaction.getLedgerPosts())
		{
			try
			{
				transactionStatus = posting.hold();
			} catch (Exception e)
			{
				log.error("Error thrown when processing hold",e);
				transactionStatus = TransactionStatus.INTERNAL_ERROR;
			}
			if (transactionStatus.isErrorState())
			{
				transaction.setStatus(transactionStatus);
				rollback(transaction);
				break;
			}
		}
		transaction.setStatus(transactionStatus);
	}

	public FinancialTransaction hold(TransactionBuilder builder) {
		FinancialTransaction transaction = builder.build();
		hold(transaction);
		return transaction;
	}
	
	/**
	 * Commits a held transaction to the two ledgers.
	 * 
	 * If the transaction fails on either side (debit or credit)
	 * it is rolled back before this method returns, and the failing
	 * status is returned.
	 * 
	 * If the transaction has not been held before calling commit,
	 * then an exception is thrown.
	 * 
	 * @param transaction
	 */
	private void internalCommit(FinancialTransaction transaction) {
		if (!transaction.getStatus().equals(TransactionStatus.HELD))
			throw new IllegalStateException("Transaction must be held before committing");
		TransactionStatus status = transaction.getStatus();
		for (LedgerPost posting : transaction.getLedgerPosts())
		{
			try
			{
				status = posting.commit();
			} catch (Exception e)
			{
				log.error("Error thrown when processing debit commit",e);
				status = TransactionStatus.INTERNAL_ERROR;
			}
			if (status.isErrorState())
			{
				transaction.setStatus(status);
				rollback(transaction);
				break;
			}
		}
		transaction.setStatus(status);
	}


	private void rollback(FinancialTransaction transaction)
	{
		if (transaction.getStatus().equals(TransactionStatus.COMPLETED))
			throw new IllegalStateException("Cannot rollback a completed transaction");
		if (transaction.getStatus().equals(TransactionStatus.NOT_STARTED))
			return;
		
		for (LedgerPost posting : transaction.getLedgerPosts())
		{
			posting.rollbackIfPossible();
		}
	}

	private enum TransactionSide
	{
		DEBIT_ONLY,CREDIT_ONLY,BOTH;
	}
}
