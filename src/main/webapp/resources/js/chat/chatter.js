/**
 * Created by liusijin on 16/3/1.
 */
String.prototype.format = function() {
    var str = this;
    for (var i = 0; i < arguments.length; i++) {
        var reg = new RegExp("\\{" + i + "\\}", "gm");
        str = str.replace(reg, arguments[i]);
    }
    return str;
};

(function(w){
    var MSG = {
        typeOnline:'on',typeOffLine:'off',typeAck:'ack',
        typeText:"txt",typePicUrl:"pic_base64",typeUrl:'url',
        typeReqFirend:'req_friend',typeNewFriend:'new_friend',
        from:null,
        n:function(type){
            var newMsg = {
                header:{type:type,from:MSG.from,to:null,client:"WS",timestamp:null},
                body:null
            };

            return {
                body:function(content){
                    newMsg.body=content;
                    return this;
                },
                to:function(someOne){
                    newMsg.header.to = someOne.subject;
                    newMsg.header.timestamp = +new Date;
                    return this;
                },
                build:function(){
                    var msg = new Paho.MQTT.Message(JSON.stringify(newMsg))
                    msg.destinationName = newMsg.header.to;
                    msg.qos=1;
                    return msg;
                },
                getMsg:function(){
                    return newMsg;
                }
            }
        },
        tansf:function(msg){
            if(msg.payloadString){
                var obj
                try{
                    return JSON.parse(msg.payloadString)
                }catch(err) {
                    console.error("msg解析JSON错误",msg.payloadString,err);
                    return msg.payloadString;
                }
            }
        }

    }

    var reconnectTimeout = 10000;
    var cleansession = false;
    var useTLS = false;

    var worker = {
        mqtt:null,
        _handler:null,
        bind:function(handler){
            this._handler = handler
        },
        observe:function(){
            var hd = this._handler;
            return function(message){
                var msg =MSG.tansf(message);
                if(msg&&msg.header){
                    if(msg.header.type==MSG.typeText)
                        hd.listener.onTxt(msg,msg.body)//TODO BE_REGIST
                    else if(msg.header.type==MSG.typePicUrl)
                        hd.listener.onPic(msg,msg.body)//TODO BE_REGIST
                    else if(msg.header.type==MSG.typeUrl)
                        hd.listener.onUrl(msg,msg.body)//TODO BE_REGIST
                    else if(msg.header.type==MSG.typeReqFirend){
                        stranger = new Contact(msg.body);
                        hd.listener.onStranger(msg,stranger); //TODO BE_REGIST
                    } else if(msg.header.type==MSG.typeNewFriend){
                        var newbee = new Contact(msg.body);
                        hd.addNewContact(newbee);
                        hd.listener.onNewFriend(msg,newbee);//TODO BE_REGIST
                    }
                    hd.listener.onMsg(msg,msg.body);
                }else if(msg&&typeof(msg)==='string'){
                    hd.listener.onMsg(msg);
                }
            }
        },

        /**
         * initHandler.me,initHandler.me.subject
         * @param serverConfig
         * @param user_id
         * @param subjects
         * @returns {*}
         * @private
         */
        _connect:function(serverConfig,user,notifyFunc){
            var wk = this;
            var df = $.Deferred();
            MSG.from = user.subject;
            var MQTTconnect = function () {
                wk.mqtt = new Paho.MQTT.Client(
                    serverConfig.channelIP,
                    parseInt(serverConfig.channelPort),
                    "/mqtt",
                    user.id);
                var mqttOption = {
                    timeout: 10,
                    useSSL: useTLS,
                    cleanSession: cleansession,
                    onSuccess: function(){
                        df.notify("connect succeed, wait for subscribing");
                        notifyFunc("已连接......");
                        wk.mqtt.subscribe(user.subject,
                            {
                                qos: 1,
                                onSuccess:function(){
                                    df.resolve("subscrib succeed");
                                    notifyFunc("在线");
                                }
                            });

                    },
                    onFailure: function (message) {
                        //TODO 重连机制，阻止发消息，状态更新等
                        df.notify( "try again later;"+message.errorMessage );
                        notifyFunc("连接失败");
                        setTimeout(MQTTconnect, reconnectTimeout);
                    }
                };

                wk.mqtt.onConnectionLost = function(e){
                    console.log(e);
                    var seconds = reconnectTimeout/1000;
                    var intv = setInterval(function(){
                        if(seconds)
                            notifyFunc("掉线了,准备"+(seconds--)+"秒后重连...")
                        else {
                            notifyFunc("go!!!!!!!!");
                            MQTTconnect();
                            clearInterval(intv);
                        }
                    },1000);
                };
                wk.mqtt.onMessageArrived = wk.observe();
                wk.mqtt.startTrace();
                var status = wk.mqtt.connect(mqttOption);
                notifyFunc("启动连接...");
            }
            MQTTconnect();
            return df.promise();
        },
        _sendText:function(dest,content){
            var m = MSG.n(MSG.typeText).body(content).to(dest);
            this.mqtt.send(m.build());
            return m.getMsg();
        },
        _sendPicUrl:function(dest,imgURL){
            var t = this.mqtt
            var m = MSG.n(MSG.typePicUrl).body(imgURL).to(dest);
            t.send(m.build());
            return m.getMsg();
            // convertToDataURLviaCanvas(imgURL,function(base64Img){
            // })
        },
        _sendUrl:function(dest,url){
            var m = MSG.n(MSG.typeUrl).body(url).to(dest);
            this.mqtt.send(m.build());
            return m.getMsg();
        },
        _sendReqFirend:function(requester,toAddContact){
            var m = MSG.n(MSG.typeReqFirend).body(requester).to(toAddContact);
            this.mqtt.send(m.build());
            return mm.getMsg();
        },
        _send:function(dest,object,type){
            var m;
            if(type==MSG.typePicUrl)
                m=this._sendPicUrl(dest,object);
            else if(type==MSG.typeUrl)
                m=this._sendUrl(dest,object);
            else if(type==MSG.typeText)
                m=this._sendText(dest,object);
            else if(!type)
                m=this._sendText(dest,object);

            return m;
        }
    }

    function convertToDataURLviaCanvas(url, callback, outputFormat){
        if(w.document){
            var img = new Image();
            img.crossOrigin = 'Anonymous';
            img.onload = function(){
                var canvas = document.createElement('CANVAS');
                var ctx = canvas.getContext('2d');
                var dataURL;
                canvas.height = this.height;
                canvas.width = this.width;
                ctx.drawImage(this, 0, 0);
                dataURL = canvas.toDataURL();
                callback(dataURL);
                canvas = null;
            };
            img.src = url;
        }
    }

    var _Contact = function(jsonContact){
        if(jsonContact){
            var obj = jsonContact;
            if(typeof(jsonContact)=='string'){
                try{
                    obj = JSON.parse(jsonContact)
                }catch(err) {
                    console.error("contact解析JSON错误",err);
                }
            }
            for(var name in obj)this[name] = obj[name];
        }
    }

    _Contact.prototype = {
        id:'uniqueID',//TODO
        name:null,
        subject:null,
        orgType:null,
        beYourFriend:function(someGuy){
            worker._sendReqFirend(this,someGuy);
        },
        say : function(word,type){
            return {
                to:function(dest){
                    return worker._send(dest,word,type);
                }
            }
        }
    }

    var Chatter = {};
    Chatter.MSG = MSG
    Chatter.Contact = _Contact;
    Chatter.init = function(userID,listener){
        var _me,_contacts,_serverConfig;
        //负责与后台API交互
        var initHandler = {
            me:null,
            contacts : null,
            listener:{
                onTxt:function(msg){},
                onPic:function(msg){},
                onUrl:function(msg){},
                onStranger:function(msg){},
                onNewFriend:function(msg){},
                onMsg:function(msg){},
                onNotifyStatus:function(info){}
            },
            findInContact:function(from){
                for(id in this.contacts){
                    if(this.contacts[id].subject==from)
                        return this.contacts[id]
                }
            },
            findNew:function(newID,func){
                $.getJSON("/contact/{0}".format(newID)).done(function(data){
                    func(new Chatter.Contact(data.result));
                });
            },
            addNewContact:function(newbee){
                this.contacts.unshift(newbee);
            },

            /**
             * 同意加好友，从服务器申请，服务器再分发双方
             * 服务器需要认证，可能失败
             * @param toBeFriend
             * @param func
             */
            agreeFriend:function(toBeFriend,func){
                $.getJSON("/contact/{0}/connect/{1}".format(this.me.id,toBeFriend.id))
                    .done(function(data){func(data)});
            },
            getGroupMember:function(groupContact){
                http_request_group_contact_list = (function(id){})(groupContact.id) //TODO need backgroud API
            }
        }
        for(name in listener){
            if(name&&name.substring(0,2)==='on')
                initHandler.listener[name] = listener[name];
        }
        worker.bind(initHandler);
        var defer = $.Deferred();
        defer.promise(initHandler)
        $.when(
            $.getJSON("http://localhost:8080/user/{0}/contact".format(userID)),
            $.getJSON("http://localhost:8080/contact/{0}/contacts".format(userID)),
            $.getJSON("http://localhost:8080/contact/{0}/channel".format(userID))
        ).done(
            function(contactOfUser, contactList, mqttServer){
                initHandler.me = new Chatter.Contact(contactOfUser[0].result);
                initHandler.contacts=[];
                for(var one in contactList[0].result)
                    initHandler.contacts.push(new Chatter.Contact(contactList[0].result[one]));
                worker
                    ._connect(mqttServer[0].result,initHandler.me,initHandler.listener.onNotifyStatus)
                    .progress(function(message){
                        initHandler.listener.onNotifyStatus(message)})
                    .done(function(msg){
                        initHandler.listener.onNotifyStatus(msg);
                        //trick part to active mosquitto to publish offline messages immediately
                        worker.mqtt.send(MSG.n(MSG.typeOnline).to(initHandler.me).build())
                        defer.resolve(msg+";channel ready to work");
                    });

            }
        )
        return initHandler;
    }

    if(window&&!window.Chatter)
        window.Chatter=Chatter;
})(window)