package com.mangofactory.moolah;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.joda.money.Money;

import com.mangofactory.entity.identity.IdentityGenerator;

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
		// TODO...
		return errors;
	}
	public FinancialTransaction build()
	{
		if (!isValid())
		{
			throw new IllegalStateException("The builder is not valid.  Check isValid or getValidationErrors before attempting to build");
		}
		Set<Posting> postings = createPostings();
		return new FinancialTransaction(getId(), postings, TransactionStatus.NOT_STARTED);
	}
	Set<Posting> createPostings() {
		Set<Posting> postings = new HashSet<Posting>();
		Posting debitPosting = new Posting(amount.negated(),debitEntity.getLedger());
		Posting creditPosting = new Posting(amount,creditEntity.getLedger());
		postings.add(debitPosting);
		postings.add(creditPosting);
		return postings;
	}
	private String getId() {
		if (transactionId != null)
			return transactionId;
		return identityGenerator.getNextIdentity();
	}
	
	
}
