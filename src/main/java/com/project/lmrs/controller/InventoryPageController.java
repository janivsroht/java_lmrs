package com.project.lmrs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class InventoryPageController {

    @GetMapping("/inventory")
    public String inventory() {
        return "dashboard/inventory";
    }
}
