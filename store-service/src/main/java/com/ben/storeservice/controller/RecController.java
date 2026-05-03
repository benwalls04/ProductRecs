package com.ben.storeservice.controller;

import com.ben.storeservice.service.RecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rec")
public class RecController {

    @Autowired
    RecService recService;
}
