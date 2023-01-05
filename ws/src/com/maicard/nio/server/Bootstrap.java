package com.maicard.nio.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.maicard.nio.NioServer;

public class Bootstrap {
	protected static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	static ClassPathXmlApplicationContext context = null;

	public static void main(String[] argv){
		
		String contextFile = null;
		if(argv.length > 0){
			contextFile = argv[0].trim();
		} else {
			System.out.println("请指定应用程序配置文件");
			System.exit(1);
		}
		context =  new ClassPathXmlApplicationContext(new String[]{contextFile});

		logger.info("7完成Netty服务器环境加载" + contextFile + ":" + context.getApplicationName());
		
		NioServer nioServer = context.getBean(NioServer.class);

		try {
			nioServer.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			//nioServer.stop();
		}
		
		
	}
}
/*

String[] paths = System.getProperty("java.class.path").split(File.separator);

URL[] urls = new URL[paths.length];



URLEMallocClassLoader cl = new URLEMallocClassLoader(urls);
NativeEMallocClassLoader cl2 = new NativeEMallocClassLoader();

Thread.currentThread().setContextClassLoader(cl2);
//context = new ClassPathXmlApplicationContext(contextFile);
/*context = new ClassPathXmlApplicationContext(contextFile) {

    protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
        super.initBeanDefinitionReader(reader);
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
        reader.setBeanClassLoader(cl);
        setClassLoader(cl);
    }
};
context =  new ClassPathXmlApplicationContext(new String[]{contextFile},false);
context.setClassLoader(cl2);
context.refresh();
//logger.info("设置类加载器:" +cl);
DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
beanFactory.setBeanClassLoader(cl2);
//context.refresh();*/