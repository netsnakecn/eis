package com.maicard.tx.dto;

import com.maicard.base.BaseDto;
import com.maicard.tx.entity.Item;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ItemDto extends BaseDto {

    private String code;
    private long productId;
    private float count;
    private String title;
    private float requestMoney;

    private float deliveryFee;
    private int freeDeliveryType;
    private int deliveryAreaType;



    public Item to() {
        Item item = new Item();
        BeanUtils.copyProperties(this, item);
        return item;

    }
}
