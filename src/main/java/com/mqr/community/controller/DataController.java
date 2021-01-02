package com.mqr.community.controller;

import com.mqr.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.crypto.Data;
import java.util.Date;

/**
 * uv  dav
 */
@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    @RequestMapping(value = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String dataPage() {
        return "data";
    }


    @RequestMapping(value = "/uv",method = RequestMethod.POST)
    public String uv(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate ,
                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        long uvData = dataService.findUvData(startDate, endDate);
        model.addAttribute("uvData", uvData);
        model.addAttribute("uvStartDate", startDate);
        model.addAttribute("uvEndDate", endDate);

        return "forward:/data";
    }

    @RequestMapping(value = "/dau",method = RequestMethod.POST)
    public String dav(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate ,
                     @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        long dauData = dataService.findDau(startDate, endDate);
        model.addAttribute("dauData", dauData);
        model.addAttribute("dauStartDate", startDate);
        model.addAttribute("dauEndDate", endDate);

        return "forward:/data";
    }

}
