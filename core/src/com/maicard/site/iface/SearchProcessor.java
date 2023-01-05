package com.maicard.site.iface;

import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SearchProcessor {
    String search(HttpServletRequest request, HttpServletResponse response, ModelMap map );

}
