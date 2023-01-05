package com.maicard.tx.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.springframework.stereotype.Service;

import com.maicard.base.CriteriaMap;
import com.maicard.tx.dao.mapper.InvoiceMapper;
import com.maicard.tx.entity.Invoice;
import com.maicard.tx.service.InvoiceService;





@Service
public class InvoiceServiceImpl  extends AbsGlobalSyncService<Invoice, InvoiceMapper> implements InvoiceService{

	@Resource
	private InvoiceMapper invoiceMapper;

	@Override
	public Invoice select(String invoiceCode) {
		CriteriaMap invoiceCriteria = new CriteriaMap();
		invoiceCriteria.put("invoiceCode",invoiceCode);
		List<Invoice> invoiceList= list(invoiceCriteria);
		logger.debug("[" + invoiceCode + "]得到的发票数量是:" + (invoiceList == null ? -1 : invoiceList.size()));
		if(invoiceList == null || invoiceList.size() != 1){
			return null;
		}	
		return invoiceList.get(0);
	}




}
