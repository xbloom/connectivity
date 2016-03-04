package com.vsvz.connectivity.webmvc;

import com.vsvz.connectivity.model.Contact;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by liusijin on 16/2/28.
 */

@Controller
public class ContactIndex {

    Contact guy = new Contact("tom","100000001","","");

//    @RequestMapping("contacts/{id}")
    String index(@PathVariable Long id,Model model){
        model.addAttribute("me",guy);
        return "contacts/index";
    }

}
