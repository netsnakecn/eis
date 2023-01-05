package com.maicard.core.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.maicard.core.entity.IndexableEntity;
 
public interface BaseIndexRepository extends ElasticsearchRepository<IndexableEntity,Long> {

}
