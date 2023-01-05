package com.maicard.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maicard.utils.JsonUtils;


/**
 * 查找标签中指定名字的对象，把该对象以JSON格式输出到页面，以方便JS进行处理
 * 支持JsonFilterView过滤
 *
 *
 * @author NetSnake
 * @date 2017-02-19
 *
 */
public class JsonOutputTag implements BodyTag  {


	protected static final Logger logger = LoggerFactory.getLogger(JsonOutputTag.class);

	private PageContext pageContext;
	@SuppressWarnings("unused")
	private Tag tag;
	private BodyContent bc;
	
	
	private boolean noDefaultValueOutput = true;



	@Override
	public int doEndTag() throws JspException {
		if(this.bc == null || StringUtils.isBlank(this.bc.getString())){
			logger.warn("空的标签内容");
			return BodyTag.SKIP_BODY;
		}
		String target = this.bc.getString();
		
		Object o = this.pageContext.getAttribute(target,PageContext.REQUEST_SCOPE);
		logger.debug("处理标签内容:" + target + "，对应对象是:" + (o == null ? "空" : o.getClass().getName()));
		JspWriter w = pageContext.getOut();
		if (w instanceof BodyContent) {
			w = ((BodyContent) w).getEnclosingWriter();
		}
		//ObjectMapper om = null;
		
		/*if(noDefaultValueOutput){
			om = JsonUtils.getNoDefaultValueInstance();
		} else {
			om = JsonUtils.getInstance();
		}*/
		String out = JsonUtils.toStringApi(o);
		
		if(out != null && out.length() > 50){
			logger.debug("输出:" + out.substring(0,49) + "...");
		} else {
			logger.debug("输出:" + out);
		}
		try {
			w.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	//必须返回2，否则不会执行中间的过程
	@Override
	public int doStartTag() throws JspException {
		//logger.debug("开始doStartTag" + this.bc);
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	@Override
	public Tag getParent() {
		return null;
	}

	@Override
	public void release() {

	}

	@Override
	public void setPageContext(PageContext arg0) {
		//logger.debug("开始setPageContext");
		this.pageContext = arg0;

	}

	@Override
	public void setParent(Tag arg0) {
		//logger.debug("开始setParent设置tag");
		this.tag = arg0;

	}


	@Override
	public int doAfterBody() throws JspException {
		//logger.debug("开始doAfterBody设置:" + this.bc);
		return 0;

	}

	@Override
	public void doInitBody() throws JspException {
		//logger.debug("开始doInitBody设置:" + 		this.bc.getString());

	}

	@Override
	public void setBodyContent(BodyContent arg0) {
		//logger.debug("开始setBodyContent设置");
		this.bc = arg0;

	}

	public boolean isNoDefaultValueOutput() {
		return noDefaultValueOutput;
	}

	public void setNoDefaultValueOutput(boolean noDefaultValueOutput) {
		this.noDefaultValueOutput = noDefaultValueOutput;
	}


}
