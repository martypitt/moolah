package com.mangofactory.moolah;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Index;
import org.joda.money.Money;
import org.joda.time.DateTime;

@Entity @EqualsAndHashCode(of={"value","transactionUuid"})
public class FinancialTransaction implements Transactable  {
	@Id
	@TableGenerator(name="tg", table="pk_table",
			pkColumnName="name", valueColumnName="value", initialValue=1000,
			allocationSize=20
			)
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Getter 
	private Long transactionId;

	@Getter
	private String transactionUuid = UUID.randomUUID().toString();

	@Getter
	@Access(AccessType.FIELD)
	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	@Getter
	@Access(AccessType.FIELD)
	private DateTime statusDate;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="transaction")
	private Set<LedgerPost> postings;
	
	@Getter @Setter(AccessLevel.PRIVATE)
	private Money value;
	
	@Getter @Setter(AccessLevel.PRIVATE)
	private String description;
	@Getter @Setter(AccessLevel.PRIVATE)
	private DateTime transactionDate;
	
	@Getter @Setter
	private String errorMessage;
	/**
	 * Provided by us - sent to a gateway - when making a deposit or withdrawal.
	 * Serves as an id that is generated before the TransactionID is known.
	 */
	@Getter @Column(nullable=true, unique=true)
	@Index(name="idxInvoiceReference")
	private String invoiceReference;
	
	/**
	 * Authorisation code returned from a payment gateway to us.
	 * Only populated for transactions that go through the payement gateway (eg., deposits & withdrawals);
	 */
	@Getter 
	private String authorisationCode;
	
	public FinancialTransaction(PostingSet postings, TransactionStatus status, DateTime transactionDate, String description)
	{
		this.transactionDate = transactionDate;
		this.description = description;
		setPostings(postings);
		setStatus(status);
		this.value = postings.sumCreditsOnly();
	}
	public FinancialTransaction(PostingSet postings, TransactionStatus status, DateTime transactionDate, String description, String invoiceReference, String authCode)
	{
		this.transactionDate = transactionDate;
		this.description = description;
		this.invoiceReference = invoiceReference;
		this.authorisationCode = authCode;
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
		this.status = status;
		this.statusDate = effectiveDate;
	}
	@Override
	@Transient
	public FinancialTransaction getTransaction() {
		return this;
	}
}
