package com.vsvz.connectivity.webmvc.impl;

import com.vsvz.connectivity.config.IntegrationConfiguration;
import com.vsvz.connectivity.model.Contact;
import com.vsvz.connectivity.model.JSONMessage;
import com.vsvz.connectivity.service.IContactService;
import com.vsvz.connectivity.webmvc.IMessageCtrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by liusijin on 16/3/8.
 */
@Controller
public class MessageCtrlImpl implements IMessageCtrl {

    private static final Logger logger = LoggerFactory.getLogger(MessageCtrlImpl.class);

    @Autowired
    Environment env;

    @Autowired
    IContactService contactSrv;

    @Autowired
    IntegrationConfiguration.MyGateway getway;

    @Override
    @RequestMapping("msg/to/{id}/{content}")
    @ResponseBody
    public String publicTextMsg(@PathVariable String id, @PathVariable String content){
        Contact c = contactSrv.getContact(id);

        JSONMessage<String> msg = new JSONMessage<String>();
        JSONMessage.Header header = msg.new Header();
        header.setType(JSONMessage.typeText);
        header.setFrom("OPERATION");
        header.setTo(c.getSubject());
        header.setClient("SYSTEM");
        msg.setHeader(header);
        msg.setBody(content);
        getway.sendMsg(msg);
        return "ok";
    }
}
