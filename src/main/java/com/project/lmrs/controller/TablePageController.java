package com.project.lmrs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class TablePageController {

    @GetMapping("/tables")
    public String tables() {
        return "dashboard/tables";
    }

    @GetMapping("/table-reservations")
    public String tableReservations() {
        return "dashboard/table-reservations";
    }
}
