package com.vsvz.connectivity.service;

import com.vsvz.connectivity.model.Contact;

import java.util.List;
import java.util.Map;

/**
 * Created by liusijin on 16/2/28.
 */
public interface IContactService {

    Contact getContact(String contactId);

    List<Contact> getContactListFromContact(String contactId);

    Contact getContactOfUser(Long userId);

    boolean connectContact(Contact one, Contact target);

    Map<String,String> getContactChannel(String id);
}
