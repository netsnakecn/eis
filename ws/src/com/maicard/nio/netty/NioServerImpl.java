package com.maicard.nio.netty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.maicard.nio.NioServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Service
public class NioServerImpl implements NioServer  {

	@Value(value="${nioServerPort}")
	private int portNumber;
	
	@Value(value="${nioStandalone}")
	private int standalone;


	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private NioServerInitializer cloudServerInitializer;
	
	
	private ServerBootstrap bootstrap;
	private ChannelFuture channel;
	
	
	EventLoopGroup bossGroup = new NioEventLoopGroup();
	EventLoopGroup workerGroup = new NioEventLoopGroup();

	@PostConstruct
	public void init() throws InterruptedException {
		if(standalone == 0) {
			run();
		}
	}
	public void run() throws InterruptedException{
		logger.info("Preparing NIO server @{}, stand alone={}", portNumber, standalone);
		//EventLoopGroup bossGroup = new NioEventLoopGroup();
		//EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(cloudServerInitializer);

			// 服务器绑定端口监听
			channel = bootstrap.bind(portNumber).sync();
			logger.info("NIO server started success @{}", portNumber);
			
			if(standalone == 1) {
				channel.channel().closeFuture().sync();
			}
			// 监听服务器关闭监听
			//  f.channel().closeFuture().sync();

			// 可以简写为
			/* b.bind(portNumber).sync().channel().closeFuture().sync(); */
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            
        }
	}
	
	@PreDestroy
	@Override
	public void stop() {
		if(channel != null) {
			logger.info("Stopping NIO server");
			/*
			 * try { channel.channel().closeFuture().sync(); bossGroup.shutdownGracefully();
			 * workerGroup.shutdownGracefully();
			 * logger.info("NIO server stopped success on:" + portNumber); } catch
			 * (InterruptedException e) { e.printStackTrace(); }
			 */

		} else {
			logger.warn("No NIO server instance to stop");
		}
	}
 
}
