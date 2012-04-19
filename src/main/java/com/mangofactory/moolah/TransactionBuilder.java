package com.mangofactory.moolah;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.joda.money.Money;

import com.mangofactory.entity.identity.IdentityGenerator;
import com.mangofactory.moolah.exception.UnbalancedTransactionException;

public class TransactionBuilder {

	public static TransactionBuilder newTransaction()
	{
		return new TransactionBuilder();
	}
	private Account debitEntity;
	private Account creditEntity;
	private Money amount;
	private IdentityGenerator identityGenerator;
	private String transactionId;
	
	private TransactionBuilder()
	{
		identityGenerator = new IdentityGenerator() {
			private Random random = new Random();
			@Override
			public String getNextIdentity() {
				return "" + random.nextLong();
			}
		};
	}
	public TransactionBuilder debit(Account entity)
	{
		this.debitEntity = entity;
		return this;
	}
	public TransactionBuilder credit(Account entity)
	{
		this.creditEntity = entity;
		return this;
	}
	public TransactionBuilder amount(Money amount)
	{
		this.amount = amount;
		return this;
	}
	public TransactionBuilder withId(String transactionId)
	{
		this.transactionId = transactionId;
		return this;
	}
	public TransactionBuilder withIdentityGenerator(IdentityGenerator identityGenerator)
	{
		this.identityGenerator = identityGenerator;
		return this;
	}
	public boolean isValid()
	{
		return getValidationErrors().size() == 0;
	}
	public Collection<TransactionValidationError> getValidationErrors()
	{
		return validate();
	}
	private Collection<TransactionValidationError> validate()
	{
		ArrayList<TransactionValidationError> errors = new ArrayList<TransactionValidationError>();
		validateTransactionsAreInSameCurrency(errors);
		validateHasDebitAndCredit(errors);
		return errors;
	}
	private void validateHasDebitAndCredit(
			ArrayList<TransactionValidationError> errors) {
		// TODO Auto-generated method stub
		
	}
	private void validateTransactionsAreInSameCurrency(
			ArrayList<TransactionValidationError> errors) {
		// TODO Auto-generated method stub
		
	}
	public FinancialTransaction build()
	{
		if (!isValid())
		{
			throw new IllegalStateException("The builder is not valid.  Check isValid or getValidationErrors before attempting to build");
		}
		PostingSet postings = createPostings();
		if (!postings.isBalanced())
		{
			throw new UnbalancedTransactionException();
		}
					
		return new FinancialTransaction(getId(), postings, TransactionStatus.NOT_STARTED);
	}
	PostingSet createPostings() {
		PostingSet postings = new PostingSet(amount.getCurrencyUnit());
		postings.add(Posting.debitOf(amount,debitEntity.getLedger()));
		postings.add(Posting.creditOf(amount,creditEntity.getLedger()));
		return postings;
	}
	private String getId() {
		if (transactionId != null)
			return transactionId;
		return identityGenerator.getNextIdentity();
	}
	
	
}
