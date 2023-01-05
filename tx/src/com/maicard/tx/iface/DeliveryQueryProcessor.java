package com.maicard.tx.iface;

import com.maicard.core.entity.EisMessage;
import com.maicard.security.entity.User;

public interface DeliveryQueryProcessor {
    public EisMessage query(String tid, String companey, User user);
}
