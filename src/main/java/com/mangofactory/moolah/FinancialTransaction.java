package com.mangofactory.moolah;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Immutable;
import org.joda.money.Money;
import org.joda.time.DateTime;

@Entity
public class FinancialTransaction implements Transactable {
	
	private String transactionId;
//	@Transient // For now
	private TreeSet<TransactionStatusRecord> statuses = new TreeSet<TransactionStatusRecord>();
	
	private Set<LedgerPost> postings;
	@Getter @Setter(AccessLevel.PRIVATE)
	private Money value;
	
	@Getter @Setter(AccessLevel.PRIVATE)
	private String description;
	@Getter @Setter(AccessLevel.PRIVATE)
	private DateTime transactionDate;
	
	public FinancialTransaction(String transactionId, PostingSet postings, TransactionStatus status, DateTime transactionDate, String description)
	{
		this.transactionId = transactionId;
		this.transactionDate = transactionDate;
		this.description = description;
		setPostings(postings);
		setStatus(status);
		this.value = postings.sumCreditsOnly();
	}
	private void setPostings(Set<LedgerPost> postings) {
		this.postings = postings;
		for (LedgerPost posting : postings) {
			posting.setTransaction(this);
		}
	}
	protected FinancialTransaction() {
		this.postings = new HashSet<LedgerPost>();
	} // For Persistence

	public LedgerPost getPostingFor(Ledger leger)
	{
		for (LedgerPost posting : postings)
		{
			if (posting.getLedger().equals(leger))
				return posting;
		}
		return null;
	}
	public LedgerPost getPostingFor(Account account)
	{
		return getPostingFor(account.getLedger());
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
	
	@Transient
	public Set<LedgerPost> getLedgerPosts()
	{
		return Collections.unmodifiableSet(postings);
	}
	@SuppressWarnings("unused") // for JPA
	@Immutable
	@OneToMany(mappedBy="transaction",fetch=FetchType.EAGER,targetEntity=LedgerPost.class, cascade=CascadeType.ALL)
	private Set<LedgerPost> getPersistentLedgerPosts()
	{
		return postings;
	}
	@SuppressWarnings("unused") // for JPA
	private void setPersistentLedgerPosts(Set<LedgerPost> value)
	{
		postings = value;
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
	@Override
	@Transient
	public FinancialTransaction getTransaction() {
		return this;
	}
}
