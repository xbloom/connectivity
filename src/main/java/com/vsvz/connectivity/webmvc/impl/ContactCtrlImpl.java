package com.vsvz.connectivity.webmvc.impl;

import com.vsvz.connectivity.model.Contact;
import com.vsvz.connectivity.service.IContactService;
import com.vsvz.connectivity.webmvc.IContactCtrl;
import com.vsvz.connectivity.webmvc.OPERATION;
import com.vsvz.connectivity.webmvc.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created by liusijin on 16/2/27.
 */
@Controller
public class ContactCtrlImpl implements IContactCtrl {
    private static final Logger logger = LoggerFactory.getLogger(ContactCtrlImpl.class);

    @Autowired
    Environment env;

    @Autowired
    IContactService contactSrv;

    /*
    * 测试页面用
    * */
    @RequestMapping("index/{id}")
    public String index(@PathVariable String id,Model model){
        Contact me = contactSrv.getContact(id);
        List<Contact> myfriends = contactSrv.getContactListFromContact(me.getId());
        model.addAttribute("me",me);
        model.addAttribute("friends",myfriends);
        model.addAttribute("serverIp", env.getProperty("client_message_server_ip_websocket"));
        model.addAttribute("serverPort",env.getProperty("client_message_server_port_websocket"));
        return "contacts/index";
    }


    /**
     * @param userId 用户ID
     * @return 返回聊天页面
     */
    @Override
    @RequestMapping("chatter/{userId}")
    public String chatRoom(@PathVariable Long userId) {
        return "contacts/chatter";
    }

    /**
     * 查询用户联系名片
     * @param userId
     * @return 用户名片
     */
    @Override
    @RequestMapping("user/{userId}/contact")
    @ResponseBody
    public ResponseResult<Contact> contactOfUser(@PathVariable Long userId) {
        Contact contactOfUser = contactSrv.getContactOfUser(userId);
        ResponseResult result = new ResponseResult();
        result.setOp(OPERATION.SUCCESS);
        result.setResult(contactOfUser);
        return result;
    }

    /**
     * 获取用户联系人列表
     * @param id 联系人ID
     * @return
     */
    @Override
    @RequestMapping("contact/{id}/contacts")
    @ResponseBody
    public ResponseResult<List<Contact>> contactListOfContact(@PathVariable String id) {
        ResponseResult result =  new ResponseResult();
        List<Contact> list = contactSrv.getContactListFromContact(id);
        result.setResult(list);
        result.setOp(OPERATION.SUCCESS);
        return result;
    }

    /**
     * 获取联系人信息
     * @param contactId 联系人ID
     * @return
     */
    @Override
    @RequestMapping("contact/{contactId}")
    @ResponseBody
    public ResponseResult<Contact> contact(@PathVariable String contactId) {
        ResponseResult result =  new ResponseResult();
        Contact c = contactSrv.getContact(contactId);
        result.setResult(c);
        result.setOp(OPERATION.SUCCESS);
        return result;
    }

    @RequestMapping("contact/{id}/channel")
    @ResponseBody
    public ResponseResult<List<Contact>> contactChannel(Contact contact) {
        ResponseResult result =  new ResponseResult();
        Map<String, String> contactChannel = contactSrv.getContactChannel(contact.getId());
        result.setResult(contactChannel);
        result.setOp(OPERATION.SUCCESS);
        return result;
    }


    @Override
    @RequestMapping("contact/{one}/connect/{target}")
    @ResponseBody
    public ResponseResult connectContact(Contact one, Contact target) {
        boolean op = contactSrv.connectContact(one, target);
        ResponseResult result =  new ResponseResult();
        result.setOp(op ? OPERATION.SUCCESS : OPERATION.FAILED);
        return result;
    }


    @Override
    public ResponseResult disconnectContact(Contact main, Contact toConnect) {
        return null;
    }
}
