package com.project.lmrs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class ReservationPageController {

    @GetMapping("/reservations")
    public String reservations() {
        return "dashboard/reservations";
    }
}
