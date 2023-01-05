package com.maicard.site.service;

import java.util.List;

import com.maicard.base.CriteriaMap;
import com.maicard.base.GlobalSyncService;
import com.maicard.core.entity.SiteDomainRelation;
import com.maicard.site.entity.Document;

public interface SiteDomainRelationService extends GlobalSyncService<SiteDomainRelation> {



	boolean isValidOwnerAccess(String serverName, long ownerId);

	SiteDomainRelation getByHostName(String hostName);


}
