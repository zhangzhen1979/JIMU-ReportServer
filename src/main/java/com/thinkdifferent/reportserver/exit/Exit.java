package com.thinkdifferent.reportserver.exit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class Exit implements DisposableBean, ExitCodeGenerator{

    @Override
    public int getExitCode() {
        return 0;
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("============System Exit!===========");
    }
}
