/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.filter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author mosmith
 */
@Component
@ConfigurationProperties(prefix="org.mosmith.tools.report.website.storage.filter.entry-filter")
@WebFilter("/*")
public class EntryFilter extends HttpFilter {

    private Set<String> entries = new HashSet<String>();
    
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String uri=request.getRequestURI();
        if(entries.contains(uri)) {
            response.setHeader("Cache-Control", "no-cache,must-revalidate");
            response.setHeader("Progma", "no-cache");
            response.setHeader("Expires", "0");
        }
        
        super.doFilter(request, response, chain);
    }

    public Set<String> getEntries() {
        return entries;
    }

    public void setEntries(Set<String> entries) {
        this.entries = entries;
    }
    
}
