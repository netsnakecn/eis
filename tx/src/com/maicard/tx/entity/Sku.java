package com.maicard.tx.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maicard.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sku extends BaseEntity {

    private long objectId;

    private float stock;

    private String name;

    private long parentId;

    private String value;

    private String url;


}
