<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
<link href="//cdn.bootcss.com/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
<link href="<c:url value='/resources/css/app.css' />" rel="stylesheet"/>

</head>
<body>
</body>
<div class="generic-container container-fluid">
    <div class="panel panel-default col-md-4 contactlist">
        <!-- Default panel contents -->
        <div class="panel-heading">
            <span class="lead">好友</span>
            <span id="status"></span>
        </div>
        <div class="tablecontainer">
            <table class="table table-hover">
                <thead>
                <tr>
                    <th>ID.</th>
                    <th>Name</th>
                    <th>Org</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach items="${friends}" var="guy">
                    <tr onclick="activeForTalking(this,${guy.id},'${guy.name}','${guy.subject}')">
                        <td><span>${guy.id}</span></td>
                        <td><span>${guy.name}</span></td>
                        <td><span>${guy.orgType}</span></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <div class="panel panel-default col-md-8 chat">
        <div class="panel-heading"><span class="lead" id="talking_name"></span></div>
        <div >
            <div class="msg_log">
                <table class="table table-hover">
                </table>
            </div>
        </div>
        <div class="formcontainer">
            <form name="input_text" class="form-horizontal">
                <div class="row">
                    <div class="form-group">
                        <label class="col-md-2" for="user_words">输入:</label>
                        <div class="col-md-4">
                            <input type="text" id="user_words" class="form-control input-sm" placeholder="Enter your words"/>
                        </div>
                        <div class="form-actions">
                            <input type="button" value="send" class="btn btn-primary btn-sm" id="sendTrigger" >
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>

</div>
<script src="<c:url value='/resources/lib/mqttws31.js' />" type="text/javascript"></script>
<script src="//cdn.bootcss.com/jquery/2.2.1/jquery.min.js"></script>
<script type="text/javascript">
    host = "${serverIp}"; // hostname or IP address
    port = ${serverPort};
    topic = "${me.subject}";  // topic to subscribe to
    useTLS = false;
    username = null;
    password = null;
    cleansession = false;
    var mqtt;
    var reconnectTimeout = 10000;

    function MQTTconnect() {
        mqtt = new Paho.MQTT.Client(
                host,
                port,
                "/mqtt",
                "${me.id}");
        var options = {
            timeout: 3,
            useSSL: useTLS,
            cleanSession: cleansession,
            onSuccess: onConnect,
            onFailure: function (message) {
                $('#status').text("Connection failed: " + message.errorMessage + "Retrying");
                setTimeout(MQTTconnect, reconnectTimeout);
            }
        };

        mqtt.onConnectionLost = onConnectionLost;
        mqtt.onMessageArrived = onMessageArrived;

        if (username != null) {
            options.userName = username;
            options.password = password;
        }
        console.log("Host="+ host + ", port=" + port + " TLS = " + useTLS + " username=" + username + " password=" + password);
        $('#status').text("连接中......");
        mqtt.startTrace();
        mqtt.connect(options);
    }

    function onConnect() {
        $('#status').text('已连接到 ' + host + ':' + port+':'+topic);
        // Connection succeeded; subscribe to our topic
        mqtt.subscribe(topic, {qos: 1,
            onSuccess:function(a){
                console.log(JSON.stringify(a))}});
    }

    function onConnectionLost(response) {
        setTimeout(MQTTconnect, reconnectTimeout);
        $('#status').text("连接丢失: " + response.errorMessage + ". 准备重新连接中......");

    };

    function onMessageArrived(message) {
        addReceiveLog(message);
    };


    var talkingContact = {}

    function activeForTalking(ele,id,name,subject){
        $(ele).css('active')
        if(!talkingContact[subject])
            talkingContact[subject]={id:id,name:name,subject:subject};
        talkingContact['active'] = talkingContact[subject];
        $('#talking_name').text(name);
    }

    $('#sendTrigger').click(function(){
        sendContent = $('#user_words').val();
        if(!talkingContact['active']){
            $('#talking_name').text("请选择聊天的用户！");
            return;
        }
        if(sendContent){
            message = new Paho.MQTT.Message(sendContent);
            message.destinationName = talkingContact['active'].subject;
            message.qos=1;
            addSendLog(message);
            mqtt.send(message);
        }
    })

    function addSendLog(message){
        var subject = message.destinationName;
        var payload = message.payloadString;
        $('.msg_log table').append('<tr><td><div class="send_line floatRight"><span></span>你说:<br>&nbsp;&nbsp; ' + payload+'</div></td></tr>');
    }

    function addReceiveLog(message){
        var msg = JSON.parse(message.payloadString)
        if(msg.header)
            $('.msg_log table').append('<tr><td><div class="receive_line"><b>' + msg.header.from + '</b>说:<br>&nbsp;&nbsp;' + JSON.stringify(msg.body)+'</div></td></tr>');
    }

    $(function(){
        MQTTconnect();
    });
</script>
</html>
