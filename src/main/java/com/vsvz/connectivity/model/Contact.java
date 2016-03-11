package com.vsvz.connectivity.model;

/**
 * Created by liusijin on 16/2/27.
 */
public class Contact {
    String name;
    String id;
    String orgType;
    String subject;//@TODO conside hash code


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {

        return name;
    }

    public String getId() {
        return id;
    }

    public Contact( String id,String name, String orgType, String connectSubject) {
        this.name = name;
        this.id = id;
        this.orgType = orgType;
        this.subject = connectSubject;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", orgType='" + orgType + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }

    public Contact() {}
}
