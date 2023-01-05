package com.maicard.tx.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.tx.entity.AddressBook;


public interface AddressBookService extends GlobalSyncService<AddressBook> {


	int setDefaultAdd(AddressBook addressBook); 
}
