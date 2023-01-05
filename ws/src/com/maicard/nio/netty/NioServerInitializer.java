package com.maicard.nio.netty;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

@Service
public class NioServerInitializer extends ChannelInitializer<SocketChannel> {
	@Autowired
	private NioServerHandler cloudServerHandler;
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	final Charset charset = Charset.forName("UTF-8");
	
	@Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 以("\n")为结尾分割的 解码器
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));

        // 字符串解码 和 编码
        pipeline.addLast("decoder", new StringDecoder(charset)); 
        pipeline.addLast("encoder", new StringEncoder(charset));

        // 自己的逻辑Handler
        pipeline.addLast("handler", cloudServerHandler);
	}
}
