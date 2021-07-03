package org.skellig.performance.runner.service.controller

import org.skellig.teststep.runner.context.SkelligTestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping

@RestController
class SkelligPerformanceController {

//    @Autowired
    private var context: SkelligTestContext? = null

    @GetMapping("/")
    fun home(): String {
        return context?.toString() ?: "error"
    }

    @GetMapping("/report")
    fun report(): String {
        return "report"
    }
}