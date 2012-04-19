package com.mangofactory.moolah;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.joda.time.DateTime;

@Entity
public class TransactionStatusRecord implements Comparable<TransactionStatusRecord> {
	@Enumerated(EnumType.STRING)
	private final TransactionStatus transactionStatus;
	private final DateTime statusDate;
	private final Integer ordinal;
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	public TransactionStatusRecord(TransactionStatus transactionStatus,
			DateTime statusDate, Integer ordinal) {
		super();
		this.transactionStatus = transactionStatus;
		this.statusDate = statusDate;
		this.ordinal = ordinal;
	}
	@SuppressWarnings("unused") // for JPA
	private TransactionStatusRecord()
	{
		transactionStatus = null;
		statusDate = null;
		ordinal = null;
	}
	public TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}
	public DateTime getStatusDate() {
		return statusDate;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((statusDate == null) ? 0 : statusDate.hashCode());
		result = prime
				* result
				+ ((transactionStatus == null) ? 0 : transactionStatus
						.hashCode());
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
		TransactionStatusRecord other = (TransactionStatusRecord) obj;
		if (statusDate == null) {
			if (other.statusDate != null)
				return false;
		} else if (!statusDate.equals(other.statusDate))
			return false;
		if (transactionStatus != other.transactionStatus)
			return false;
		return true;
	}
	@Override
	public int compareTo(TransactionStatusRecord o) {
		return this.ordinal.compareTo(o.ordinal);
	}
	public Integer getOrdinal() {
		return ordinal;
	}
}
