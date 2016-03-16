package com.vsvz.connectivity.webmvc;

import com.vsvz.connectivity.model.Contact;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Created by liusijin on 16/2/28.
 */
public interface IContactCtrl {

    /*
    * 路由至聊天页面
    * */
    String ui(@PathVariable String userId, Model model);


    /**
     * 获取用户名片
     * @param userId
     * @return
     */
    ResponseResult<Contact> contactOfUser(@PathVariable String userId);

    /**
     * 查询名片
     * @param contactId
     * @return
     */
    ResponseResult<Contact> contact(@PathVariable String contactId);

    /**
     * 获取用户联系人列表
     * @param contactId
     * @return
     */
    ResponseResult<List<Contact>> contactListOfContact(@PathVariable String contactId);

    ResponseResult connectContact(Contact one,Contact target);

    ResponseResult disconnectContact(Contact one,Contact target);
}
