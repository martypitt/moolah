package com.mangofactory.moolah;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static com.mangofactory.moolah.TestHelpers.*;
import org.junit.Test;

public class PostingTests {

	@Test
	public void debitAndCreditConstructorsAreAppliedCorrectly()
	{
		Ledger ledger = mock(Ledger.class);
		LedgerPost posting = LedgerPost.debitOf(AUD(1), ledger);
		assertThat(posting.isCredit(), is(false));
		assertThat(posting.isDebit(), is(true));

		posting = LedgerPost.creditOf(AUD(1), ledger);
		assertThat(posting.isCredit(), is(true));
		assertThat(posting.isDebit(), is(false));
	}
}
