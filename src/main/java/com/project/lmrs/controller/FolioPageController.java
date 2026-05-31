package com.project.lmrs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class FolioPageController {

    @GetMapping("/folios")
    public String folios() {
        return "dashboard/folios";
    }
}
