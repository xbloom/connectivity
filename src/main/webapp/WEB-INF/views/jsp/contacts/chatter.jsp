<%--
  Created by IntelliJ IDEA.
  User: liusijin
  Date: 16/3/2
  Time: 上午9:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title></title>
</head>
<body>

</body>
<script src="<c:url value='/resources/lib/mqttws31.js'/>"></script>
<script src="<c:url value='/resources/js/chatter.js' />"></script>
<script src="//cdn.bootcss.com/jquery/2.2.1/jquery.min.js"></script>

<script>
  var chatter = Chatter.init("1001",{
    showText:function(msg){},
    showPic:function(){},
    showURL:function(){},
    comingStranger:function(){},
    newFriend:function(){}
  });
  console.log('start!')
  chatter.done(function(){
    console.log("start talking");
    console.log("me:",chatter.me)
    console.log("my contacts:",chatter.contacts)

    chatter.findNew(10000004,function(he){
      he.talkTo('new world!!');//我说
      chatter.me.beYourFriend(he);//加你
      chatter.agreeFriend(chatter.me,function(result){//同意
        console.log(result);
        he.talkTo("should be a good friend!");//我说
        chatter.me.talkTo('yes,wish');//他说
        he.talkTo(Chatter.MSG.typePicUrl,"https://ss1.baidu.com/6ONXsjip0QIZ8tyhnq/it/u=2317553821,2441103228&fm=58");//给你发个图
        he.talkTo(Chatter.MSG.typeUrl,"http://news.baidu.com");//给你发个新闻
      });
    });
  })



</script>
</html>
