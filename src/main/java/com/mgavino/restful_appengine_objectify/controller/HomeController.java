package com.mgavino.restful_appengine_objectify.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @RequestMapping("/")
    public String grettings() {
        return "Greetings!";
    }

}
