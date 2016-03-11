package com.vsvz.connectivity.service.impl;

import com.vsvz.connectivity.model.Contact;
import com.vsvz.connectivity.service.IContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by liusijin on 16/2/28.
 */

@Service
public class ContactServiceImpl implements IContactService {

    @Autowired
    Environment env;

    //@TODO get contact data from db
    private Contact me = new Contact("10000001","tom","A","opr/org_id/10000001");


    Map<String, Contact> testMap = new HashMap<String, Contact>() {{
        put("10000001", new Contact("10000001","tom","A","opr/org_id/10000001"));
        put("10000002", new Contact("10000002","张三","B","biz/org_id/10000002"));
        put("10000003", new Contact("10000003","李四","C","cus/org_id/10000003"));
        put("10000004", new Contact("10000004","王二","C","cus/org_id/10000004"));
        put("10000005", new Contact("10000005","隔壁老王","A","opr/org_id/10000005"));
    }};


    @Override
    public Contact getContact(String contactId) {
        if(!StringUtils.isEmpty(contactId)) {
            Contact tmp = testMap.get(contactId);
            if(tmp!=null)
                return tmp;
        }
        return me;
    }

    /**
     * 查找所有关联的联系人
     * @TODO 修改为数据库查询
     * @param contactId
     * @return
     */
    @Override
    public List<Contact> getContactListFromContact(String contactId) {
        return new ArrayList<Contact>(testMap.values());
    }

    /**
     * 根据系统用户ID，查找自己名片
     * @TODO 改成查找数据库
     * @param userId
     * @return
     */
    @Override
    public Contact getContactOfUser(String userId) {
        if(!StringUtils.isEmpty(userId)) {
            Contact tmp = testMap.get(userId);
            if(tmp!=null)
                return tmp;
        }
        return me;
    }

    @Override
    public boolean connectContact(Contact one, Contact target) {
        return false;
    }

    /**
     * @TODO 根据用户hash服务器位置,定制关注的主题
     *       系统推送，新闻，组织定义topic等
     * @param contactID
     * @return
     */
    @Override
    public Map<String, String> getContactChannel(String contactID) {
        Map map = new HashMap<String,String>();
        map.put("channelIP",env.getProperty("client_message_server_ip_websocket"));
        map.put("channelPort",env.getProperty("client_message_server_port_websocket"));
        map.put("biz_subject","system");
        return map;
    }
}
