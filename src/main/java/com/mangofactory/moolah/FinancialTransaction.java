package com.mangofactory.moolah;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import org.apache.commons.lang.NotImplementedException;
import org.hibernate.annotations.Immutable;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableSet;
import com.mangofactory.moolah.persistence.AbstractPersistentLedger;

@Entity
public class FinancialTransaction {
	
	@Id
	private String transactionId;
	@Transient // For now
	private TreeSet<TransactionStatusRecord> statuses = new TreeSet<TransactionStatusRecord>();
	
	@OneToMany(mappedBy="transaction",fetch=FetchType.EAGER)
	@Immutable
	private Set<Posting> postings;
	
	private Money value;
	
	public FinancialTransaction(String transactionId, PostingSet postings, TransactionStatus status)
	{
		this.transactionId = transactionId;
		setPostings(postings);
		setStatus(status);
		this.value = postings.sumCreditsOnly();
	}
	private void setPostings(Set<Posting> postings) {
		this.postings = postings;
		for (Posting posting : postings) {
			posting.setTransaction(this);
		}
	}
	protected FinancialTransaction() {} // For Persistence

	@SuppressWarnings("unused")
	@PrePersist
	private void beforePersist()
	{
		assertTransactionBalances();
	}
	
	public Posting getPostingFor(Ledger leger)
	{
		for (Posting posting : postings)
		{
			if (posting.getLedger().equals(leger))
				return posting;
		}
		return null;
	}
	private void assertTransactionBalances() {
		throw new NotImplementedException();
	}
	
	@SuppressWarnings("unused") // For JPA
	private void setTransactionId(String value)
	{
		this.transactionId = value;
	}
	@Id
	public String getTransactionId() {
		return transactionId;
	}
	@Transient
	public TransactionStatus getStatus() {
		TransactionStatusRecord statusRecord = statuses.last();
		return (statusRecord != null) ? statusRecord.getTransactionStatus() : null;
	}
	@Transient
	private Integer getLastStatusOrdinal()
	{
		if (statuses.size() == 0)
			return -1;
		return statuses.last().getOrdinal();
	}
	public Set<Posting> getPostings()
	{
		return Collections.unmodifiableSet(postings);
	}
	public void setStatus(TransactionStatus status) {
		setStatus(status,DateTime.now());
		assert getStatus().equals(status);
	}
	public void setStatus(TransactionStatus status, DateTime effectiveDate)
	{
		assertIsValidNextStatus(status);
		Integer ordinal = getLastStatusOrdinal() + 1;
		TransactionStatusRecord statusRecord = new TransactionStatusRecord(status, effectiveDate, ordinal);
		statuses.add(statusRecord);
	}
	@Transient
//	@OneToMany(fetch=FetchType.EAGER,cascade=CascadeType.ALL)
	protected Set<TransactionStatusRecord> getStatuses()
	{
		return statuses;
	}
	protected void setStatuses(Set<TransactionStatusRecord> value)
	{
		this.statuses = new TreeSet<TransactionStatusRecord>(value);
	}
	private void assertIsValidNextStatus(TransactionStatus newStatus) {
		// TODO Auto-generated method stub
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((transactionId == null) ? 0 : transactionId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FinancialTransaction other = (FinancialTransaction) obj;
		if (transactionId == null) {
			if (other.transactionId != null)
				return false;
		} else if (!transactionId.equals(other.transactionId))
			return false;
		return true;
	}
}
