package com.maicard.tx.dao.mapper;

import java.util.List;

import com.maicard.base.IDao;
import org.springframework.dao.DataAccessException;

import com.maicard.base.CriteriaMap;
import com.maicard.tx.entity.AddressBook;
 

public interface AddressBookMapper extends IDao<AddressBook> {
	int setDefaultAdd(AddressBook addressBook) throws DataAccessException;
}
