package com.maicard.site.iface;

import org.springframework.scheduling.annotation.Async;


import com.maicard.site.entity.Document;

public interface DocumentPostProcessor {
	
	int process(Document document, String mode) throws Exception;
	
	@Async
	public void asyncProcess(Document document, String mode);


}
