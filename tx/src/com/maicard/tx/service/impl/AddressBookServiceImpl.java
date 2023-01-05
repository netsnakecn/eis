package com.maicard.tx.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.maicard.core.service.abs.AbsGlobalSyncService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.maicard.base.BaseService;
import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.CacheNames;
import com.maicard.core.service.ConfigService;
import com.maicard.core.service.DataDefineService;
import com.maicard.core.service.GlobalUniqueService;
import com.maicard.tx.dao.mapper.AddressBookMapper;
import com.maicard.tx.entity.AddressBook;
import com.maicard.tx.service.AddressBookService;
 


@Service
public class AddressBookServiceImpl extends AbsGlobalSyncService<AddressBook,AddressBookMapper> implements AddressBookService {




	@Override
	public int setDefaultAdd(AddressBook addressBook)
	{
		return mapper.setDefaultAdd(addressBook);
	}
}
