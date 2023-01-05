package com.maicard.site.iface;

import org.springframework.ui.ModelMap;

import com.maicard.security.entity.User;

/**
 * 负责处理管理后台公告板即首页信息的处理器
 * @author GHOST
 * @date 2019-09-09
 *
 */
public interface BoardProcessor {

	void writeBoard(ModelMap map, User partner);

}
