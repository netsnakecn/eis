package com.maicard.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;

public class GzipUtils {

    public static String compress(byte[] src) {
      
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out) {
            	{	this.def.setLevel(Deflater.BEST_COMPRESSION); }
            };
            gzip.write(src);
            gzip.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("压缩前长度:" + src.length + "，压缩后长度:" + out.toByteArray().length);
        return Base64.encodeBase64String(out.toByteArray());
    }
 
    /**
     * 使用gzip进行解压缩
     */
    public static String uncompress(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;
        try {
            compressed = Base64.decodeBase64(compressedStr);// new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);
 
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            try {
                out.close();
            } catch (IOException e) {
            }
        }
        return decompressed;
    }
    
   
    
    public static void main(String[] argv) {
    	StringBuffer sb = new StringBuffer();
    	for(int i = 0; i < 100; i++) {
    		sb.append("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
    	}
    	String zip = GzipUtils.compress(sb.toString().getBytes());
    	System.out.println("压缩前:" + sb.toString().length() + "，压缩后:" + zip.length());
    }
}
