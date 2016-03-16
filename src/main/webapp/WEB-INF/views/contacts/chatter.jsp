
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title></title>
</head>
<body>
hello
</body>
<script src="<c:url value='/resources/lib/mqttws31.js'/>"></script>
<script src="<c:url value='/resources/js/chatter.js' />?ts=225"></script>
<script src="//cdn.bootcss.com/jquery/2.2.1/jquery.min.js"></script>

<script>
    var chatter = Chatter.init("10000002",{
        onTxt:function(msg){console.log(JSON.stringify(msg))},
        onPic:function(msg){console.log(JSON.stringify(msg))},
        onUrl:function(msg){console.log(JSON.stringify(msg))},
        onStranger:function(msg){console.log(JSON.stringify(msg))},
        onNewFriend:function(msg){console.log(JSON.stringify(msg))},
        onMsg:function(msg){console.log(JSON.stringify(msg))}
      });
    chatter.done(function(){
//        console.log("me:",chatter.me)
//        console.log("my contacts:",chatter.contacts)

        chatter.findNew(10000004,function(he){
            chatter.me.say('new world!!').to(he);//我说
            chatter.me.beYourFriend(he);//加你
            chatter.agreeFriend(chatter.me,function(result){//他同意
                chatter.me.say("should be a good friend!").to(he);//我说
//                he.say('yes,wish').to(chatter.me);
//                chatter.me.say("https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=2317553821,2441103228&fm=58",Chatter.MSG.typePicUrl).to(he);//给你发个图
                chatter.me.say("http://news.baidu.com",Chatter.MSG.typeUrl).to(he);//给你发个新闻
            });
        });
    });




</script>
</html>
