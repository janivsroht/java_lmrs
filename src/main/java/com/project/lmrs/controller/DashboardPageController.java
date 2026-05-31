package com.project.lmrs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardPageController {

    @GetMapping
    public String dashboard() {
        return "dashboard/dashboard";
    }

    @GetMapping("/profile")
    public String profile() {
        return "dashboard/profile";
    }
}
