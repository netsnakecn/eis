package com.maicard.site.iface;

import com.maicard.base.CriteriaMap;
import com.maicard.site.entity.Captcha;

public interface CaptchaGenerator {
	public Captcha generate(CriteriaMap patchcaCriteria);
}
