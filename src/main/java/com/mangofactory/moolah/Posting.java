package com.mangofactory.moolah;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Immutable;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.mangofactory.moolah.persistence.AbstractPersistentLedger;

@Entity
public class Posting {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private Money value;
	
	@ManyToOne(targetEntity=AbstractPersistentLedger.class)
	private Ledger ledger;
	
	public Posting(Money value, Ledger ledger)
	{
		this.value = value;
		this.ledger = ledger;
	}
	@SuppressWarnings("unused")
	private Posting() {} 
	@ManyToOne @Immutable
	private FinancialTransaction transaction;

	public FinancialTransaction getTransaction() {
		return transaction;
	}

	public void setTransaction(FinancialTransaction transaction) {
		if (this.transaction != null)
			throw new IllegalStateException("Transaction cannot be changed once set");
		
		this.transaction = transaction;
	}

	public Money getValue() {
		return value;
	}

	public Ledger getLedger() {
		return ledger;
	}

	public TransactionStatus getTransactionStatus() {
		return transaction.getStatus();
	}

	public CurrencyUnit getCurrencyUnit() {
		return value.getCurrencyUnit();
	}
	
	public TransactionStatus hold()
	{
		return ledger.hold(this);
	}
	public void rollback()
	{
		ledger.rollback(this);
	}
	public TransactionStatus commit()
	{
		return ledger.commit(this);
	}
	public void rollbackIfPossible()
	{
		if (ledger.canRollback(this))
		{
			ledger.rollback(this);
		}
	}
}
