package com.maicard.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

public class IpUtils {
	public static String getClientIp(HttpServletRequest request){
		String realIp = null;
		try{
			realIp = request.getHeader("X-Real-IP");
		}catch(Exception e){}
		if(realIp == null){
			realIp = request.getRemoteAddr();
		}
		return realIp;
	}

	public static String getIpFromHost(String hostname){
		
		if(hostname == null){
			return null;
		}
		if(hostname.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")){
			return hostname;
		}
		InetAddress myServer = null;
		try {
			myServer = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if(myServer == null){
			return hostname;
		}
		return		myServer.getHostAddress();
	}
	
	public static String getComputerIp() {
        String localip = null;// 本地IP，如果没有配置外网IP则返回它
        String netip = null;// 外网IP
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean finded = false;// 是否找到外网IP
            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = (InetAddress) address.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
                        netip = ip.getHostAddress();
                        finded = true;
                        break;
                    } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
                        localip = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }
	
	public static void main(String[] argv){
	}

}
