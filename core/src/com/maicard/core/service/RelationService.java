package com.maicard.core.service;

import com.maicard.base.GlobalSyncService;
import com.maicard.core.entity.Relation;
import com.maicard.utils.StringTools;

import java.sql.Array;
import java.util.List;


public interface RelationService extends GlobalSyncService<Relation> {


    static long[] getIds(List<Relation> relationList) {
        StringBuffer sb = new StringBuffer();
        relationList.forEach(relation -> {
            sb.append(relation.getId()).append(",");
        });
        return StringTools.str2long(sb.toString());
    }


    static long[] getFromIds(List<Relation> relationList) {
        StringBuffer sb = new StringBuffer();
        relationList.forEach(relation -> {
            sb.append(relation.getFromId()).append(",");
        });
        return StringTools.str2long(sb.toString());
    }
}
