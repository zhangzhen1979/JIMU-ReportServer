/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.sharescript;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author mosmith
 */
@ResponseStatus(code=HttpStatus.NOT_FOUND, reason = "Template not found")
public class ShareScriptNotFoundException extends RuntimeException {

    public ShareScriptNotFoundException(String message) {
        super(message);
    }

    public ShareScriptNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
