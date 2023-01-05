package com.maicard.tx.iface;

import com.maicard.core.entity.BaseEntity;

public interface NotifyProcessor {
	
	String sendNotifySync(BaseEntity item);

	void sendNotifyAsync(BaseEntity item);
}
