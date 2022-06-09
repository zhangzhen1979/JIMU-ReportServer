/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.util;

import java.util.UUID;

/**
 *
 * @author Administrator
 */
public class IDUtils {
    public static String createId(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        // sb.append('-');
        // sb.append(String.format("%x", System.currentTimeMillis()));
        sb.append('-');
        sb.append(UUID.randomUUID().toString());
        
        return sb.toString();
    }
}
