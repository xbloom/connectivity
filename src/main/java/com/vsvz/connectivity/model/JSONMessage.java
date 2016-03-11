package com.vsvz.connectivity.model;

/**
 * Created by liusijin on 16/3/8.
 */
public class JSONMessage<T> {
    public static String typeOnline="on";
    public static String typeOff="off";
    public static String typeAck="ack";

    public static String typeText="txt";
    public static String typePicUrl="pic_base64";
    public static String typeUrl="url";

    public static String typeReqFirend="req_friend";
    public static String typeNewFriend="new_friend";


    Header header;
    T body;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public class Header{
        private String type;
        private String from;
        private String to;
        private String client;
        private Long timestamp;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
