package com.thinkdifferent.reportserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/5/13 14:15
 */
@RestController
public class IndexController {

    @GetMapping
    public String index() {
        return "启动成功";
    }
}
