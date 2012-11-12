package com.mangofactory.moolah.processing;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.mangofactory.moolah.FinancialTransaction;
import com.mangofactory.moolah.TransactionStatus;

@RunWith(MockitoJUnitRunner.class)
public class FinancialTransactionControllerTests {

	FinancialTransactionController controller;

	@Mock
	private FinancialTransaction transaction;
	
	@Before
	public void setup()
	{
		controller = new FinancialTransactionController();
	}
	@Test
	public void whenAnTransactionInErrorStateIsPassedToCommit_that_noActionIsTaken()
	{
		when(transaction.getStatus()).thenReturn(TransactionStatus.INTERNAL_ERROR);
		controller.commit(transaction);
	}
	
}
