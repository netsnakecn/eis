package com.maicard.tx.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.tx.entity.Invoice;
 


public interface InvoiceService extends GlobalSyncService<Invoice> {
	

	public Invoice select(String invoiceCode);



}
