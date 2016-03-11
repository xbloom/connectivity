package com.vsvz.connectivity.webmvc;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by liusijin on 16/3/8.
 */
public interface IMessageCtrl {

    String publicTextMsg(String id, String content);
}
