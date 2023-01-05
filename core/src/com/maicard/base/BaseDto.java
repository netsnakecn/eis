package com.maicard.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public abstract  class BaseDto implements Serializable {
    /**
     * @ignore
     */
    protected  long id;
    /**
     * @ignore
     */
    protected int currentStatus;
}
