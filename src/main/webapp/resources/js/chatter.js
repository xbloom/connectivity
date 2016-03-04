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
        typeText:"txt",typePicUrl:"pic",typeUrl:'url',
        typeReqFirend:'req_friend',typeNewFriend:'new_friend',
    }

    var reconnectTimeout = 10000;
    var cleansession = true;
    var useTLS = false;

    var worker = {
        mqtt:null,
        reportTo:function(handler){
            this._handler = handler
        },
        observe:function(message){
            if(message.type==MSG.typeText)
                this._handler.listener.showText(message.body)//TODO BE_REGIST
            else if(message.type==MSG.typePicUrl)
                this._handler.listener.showPic("data:image/png;base64,"+message.body)//TODO BE_REGIST
            else if(message.type==MSG.typeUrl)
                this._handler.listener.showURL(message.body)//TODO BE_REGIST
            else if(message.type==MSG.typeReqFirend){
                stranger = new Contact(message.body);
                this._handler.listener.comingStranger(stranger); //TODO BE_REGIST
            } else if(message.type==MSG.typeNewFriend){
                var newbee = new Contact(JSON.parse(message.body).body);
                this._handler.addNewContact(newbee);
                this._handler.listener.newFriend(newbee);//TODO BE_REGIST
            }
        },
        _connect:function(serverConfig,user_id,subjects){
            var wk = this;
            var df = $.Deferred();
            var MQTTconnect = function () {
                wk.mqtt = new Paho.MQTT.Client(
                    serverConfig.channelIP,
                    parseInt(serverConfig.channelPort),
                    "/mqtt",
                    user_id);
                var mqttOption = {
                    timeout: 10,
                    useSSL: useTLS,
                    cleanSession: cleansession,
                    onSuccess: function(){
                        console.log("connect succeed");
                        wk.mqtt.subscribe(subjects,
                            {
                                qos: 1,
                                onSuccess:function(){
                                    df.resolve("connect succeed");
                                }
                            });

                    },
                    onFailure: function (message) {
                        //TODO 重连机制，阻止发消息，状态更新等
                        df.notify( "try again later;"+message.errorMessage );
                        setTimeout(MQTTconnect, reconnectTimeout);
                    }
                };

                wk.mqtt.onConnectionLost = function(){
                    setTimeout(MQTTconnect, reconnectTimeout);
                };
                wk.mqtt.onMessageArrived = wk.observe;
                wk.mqtt.startTrace();
                var status = wk.mqtt.connect(mqttOption);
                console.log('try connecting')
            }
            MQTTconnect();
            return df.promise();
        },
        _sendText:function(dest,content){
            var message = new Paho.MQTT.Message(JSON.stringify(content));
            message.destinationName = dest.subject;
            this.mqtt.send(message);
        },
        _sendPicUrl:function(dest,imgURL){
            var t = this.mqtt
            convertToDataURLviaCanvas(imgURL,function(base64Img){
                var body = {type:MSG.typePicUrl,body:base64Img}
                var message = new Paho.MQTT.Message(JSON.stringify(body));
                message.destinationName = dest.subject;
                t.send(message);
            })
        },
        _sendUrl:function(dest,url){
            var body = {type:MSG.typeUrl,body:url}
            var message = new Paho.MQTT.Message(JSON.stringify(body));
            message.destinationName = dest.subject;
            this.mqtt.send(message);
        },
        _sendReqFirend:function(dest,toAddContact){
            var body = {type:MSG.typeReqFirend,body:dest}
            var message = new Paho.MQTT.Message(JSON.stringify(body));
            message.destinationName = toAddContact.subject;
            this.mqtt.send(message);
        },
        _send:function(dest,type,object){
            if(type==MSG.typePicUrl)
                this._sendPicUrl(dest,object)
            if(type==MSG.typeUrl)
                this._sendUrl(dest,object)
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
        talkTo:function(type,object){
            var dest = this;
            if(arguments.length==1)
                worker._sendText(dest,type);
            else
                worker._send(dest,type,object);
        },
        say : function(word,type){
            return {
                to:function(dest){
                    worker._send(dest,word,type);
                }
            }
        }
    }

    var Chatter = {};
    Chatter.MSG = MSG
    Chatter.Contact = _Contact;
    Chatter.init = function(userID,options){
        var _me,_contacts,_serverConfig;
        //负责与后台API交互
        var initHandler = {
            me:null,
            contacts : null,
            findNew:function(newID,func){
                $.getJSON("/contact/{0}".format(newID)).done(function(data){
                    func(new Chatter.Contact(data.result));
                });
            },
            addNewContact:function(newbee){
                this.contacts.unshift(newbee);
            },
            agreeFriend:function(toBeFriend,func){
                $.getJSON("/contact/{0}/connect/{1}".format(this.me.id,toBeFriend.id))
                    .done(function(data){func(data)});
            },
            getGroupMember:function(groupContact){
                http_request_group_contact_list = (function(id){})(groupContact.id) //TODO need backgroud API
            }
        }
        var defer = $.Deferred();
        defer.promise(initHandler)
        $.when(
            $.getJSON("/user/{0}/contact".format(userID)),
            $.getJSON("/contact/{0}/contacts".format(userID)),
            $.getJSON("/contact/{0}/channel".format(userID))
        ).done(
            function(contactOfUser, contactList, mqttServer){
                initHandler.me = new Chatter.Contact(contactOfUser[0].result);
                initHandler.contacts=[];
                for(var one in contactList[0].result)
                    initHandler.contacts.push(new Chatter.Contact(one));
                worker
                    ._connect(mqttServer[0].result,initHandler.me.id,initHandler.me.subject)
                    .progress(function(message){console.log("Connection failed: " + message + "Retrying")})
                    .done(function(){
                        defer.resolve("channel ready to work");
                    });

            }
        )
        return initHandler;
    }


    if(window&&!window.Chatter)
        window.Chatter=Chatter;
})(window)
