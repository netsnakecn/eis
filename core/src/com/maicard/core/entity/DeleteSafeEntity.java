package com.maicard.core.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 删除时不真的删除数据，只将deleted==1
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeleteSafeEntity extends BaseEntity{

    protected int deleted;
}
