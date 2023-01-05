package com.maicard.nio;

public interface NioServer {
	public void run() throws InterruptedException;

	public void stop();
}
