package com.mangofactory.moolah.persistence;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.joda.money.CurrencyUnit;

import com.mangofactory.moolah.Account;
import com.mangofactory.moolah.BaseLedger;

@Entity(name="Ledger")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="ledgerType",discriminatorType=DiscriminatorType.STRING)
abstract public class AbstractPersistentLedger<T extends Account> extends BaseLedger {

	
	public AbstractPersistentLedger(CurrencyUnit currency, T account) {
		super(currency, account);
	}

	private Long id;
	
	@Transient
	public T getAccount()
	{
		return (T) super.getAccount();
	}
	
	@Column(updatable=false)
	@Override
	public CurrencyUnit getCurrency()
	{
		return super.getCurrency();
	}
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
