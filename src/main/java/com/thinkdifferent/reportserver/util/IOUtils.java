package com.thinkdifferent.reportserver.util;


import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mosmith
 */
public class IOUtils {
    public static String readAsString(InputStream is, String charset) throws UnsupportedEncodingException, IOException {
        
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        byte[] buffer=new byte[8192];
        
        int read;
        while((read=is.read(buffer))>=0) {
            bos.write(buffer);
        }
        
        byte[] bytes=bos.toByteArray();
        String result=new String(bytes, charset);
        return result;
    }
    
    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer=new byte[8192];
        
        int read;
        while((read=is.read(buffer))>=0) {
            os.write(buffer, 0, read);
        }
    }
    
    public static void close(Closeable closeable) {
        if(closeable==null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
