package com.mangofactory.moolah;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.joda.money.Money;
import org.joda.time.DateTime;

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
	
	private DateTime transactionDate;
	private String description;
	private String invoiceReference;
	private String authorisationCode;
	
	private TransactionBuilder()
	{
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
	public TransactionBuilder withDescription(String description)
	{
		this.description = description;
		return this;
	}
	public TransactionBuilder withInvoiceDescription(String invoiceReference) {
		this.invoiceReference = invoiceReference;
		return this;
	}
	public TransactionBuilder on(DateTime transactionDate)
	{
		this.transactionDate = transactionDate;
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
		if (transactionDate == null)
			transactionDate = DateTime.now();
		return new FinancialTransaction(postings, TransactionStatus.NOT_STARTED, transactionDate, description, invoiceReference, authorisationCode);
	}
	PostingSet createPostings() {
		PostingSet postings = new PostingSet(amount.getCurrencyUnit());
		postings.add(LedgerPost.debitOf(amount,debitEntity.getLedger()));
		postings.add(LedgerPost.creditOf(amount,creditEntity.getLedger()));
		return postings;
	}
	public TransactionBuilder withAuthorisationCode(String authorisationCode) {
		this.authorisationCode = authorisationCode;
		return this;
	}
	
	
	
}
