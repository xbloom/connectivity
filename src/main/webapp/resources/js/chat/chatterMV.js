var app = angular.module('App', [])
// app.config(['$compileProvider', function ($compileProvider) {
//   $compileProvider.debugInfoEnabled(false);
// }]);

app.controller('contactCtrl',function($q,$scope) {
    var z = this;

    z.statusDesc
    z.searchText = '';
    z.contacts;
    z.target;
    z.words;
    z.sendPic;

    z.queryContact = queryContact ;
    z.talkTo = talkTo;
    z.sendTxt = sendTxt;
    z.sendIMG = sendIMG;

    var chat = 	Chatter.init(userID,{
        onNotifyStatus:function(msg){
            $scope.$apply(function(){
                z.statusDesc = msg;
            })
        },
        onTxt:function(msg){
            var contact = chat.findInContact(msg.header.from);
            contact.messages = contact.messages||[]
            $scope.$apply(function(){
                contact.messages.push(msg);
            })
        },
        onPic:function(msg){
            var contact = chat.findInContact(msg.header.from);
            contact.messages = contact.messages||[]
            $scope.$apply(function(){
                contact.messages.push(msg);
            })
        },
        onUrl:function(msg){console.log(JSON.stringify(msg))},
        onStranger:function(msg){console.log(JSON.stringify(msg))},
        onNewFriend:function(msg){console.log(JSON.stringify(msg))},
        onMsg:function(msg){
            //dont handler message content,
            // all the message comes here include control packet that business never should know
            var index,contact;
            for(id in z.contacts){
                if(z.contacts[id].subject==msg.header.from){
                    index=id;
                    contact = z.contacts[id];
                    $scope.$apply(function(){
                        var t = z.contacts.splice(index,1)[0]
                        z.contacts.unshift(t);
                    })
                    break;
                }
            }
            if(z.target!=contact){
                $scope.$apply(function(){
                    contact.unread=true;
                })
            }
        }
    })

    $q.when(chat,function(data){
            z.contacts = chat.contacts
            z.me = chat.me
        }
    )


    function queryContact() {
        if (z.searchText) {
            chat.findNew(z.searchText,function(ct){
                console.log(ct);
            })
        }
    };

    function talkTo(subject){
        for(id in z.contacts){
            if(z.contacts[id].subject==subject){
                z.target = z.contacts[id];
                z.target.unread=false;
                if(!(z.target.messages && z.target.messages.length)){
                    z.target.messages=[];
                }
                return;
            }
        }
    }

    function sendTxt(){
        if(z.words && z.target){
            var m = z.me.say(z.words).to(z.target);
            m.mine = true
            z.target.messages.push(m);
            z.words="";
        }
    }
    function sendIMG(base64,event){
        if(z.target && base64){
            var m = z.me.say(base64,Chatter.MSG.typePicUrl).to(z.target);
            m.mine = true
            z.target.messages.push(m);
        }
    }
});

app.directive('ngScrollBottom', function ($timeout) {
    return {
        scope: {
            ngScrollBottom: "="
        },
        link: function (scope, element) {
            scope.$watchCollection('ngScrollBottom', function (newValue) {
                if (newValue&&newValue.length){
                    $timeout(function(){
                        $(element).scrollTop(element[0].scrollHeight);
                    },0,false)
                }
            });
        }
    }
})

app.directive('fileReader', function() {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            return;
            var handler = scope.$eval(attrs.fileReader);
            element.bind("change", function (changeEvent) {
                var reader = new FileReader();
                reader.onload = function (e) {
                    scope.$apply(function () {
                        handler(e.target.result,e)
                    });
                }
                reader.readAsDataURL(changeEvent.target.files[0]);
            });
        }
    };
});

app.directive('imgReader', function() {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var handler = scope.$eval(attrs.imgReader);
            var image = document.createElement('img');
            var canvas = document.createElement('canvas');
            element.bind("change", function (changeEvent) {
                if(changeEvent.target.files && changeEvent.target.files.length){
                    var reader = new FileReader();
                    reader.onload = function (e) {
                        scope.$apply(function () {
                            image.src = e.target.result;
                            var ctx = canvas.getContext('2d');
                            ctx.drawImage(image, 0, 0);

                            var MAX_WIDTH = 400;
                            var MAX_HEIGHT = 400;
                            var width = image.width;
                            var height = image.height;

                            if (width > height) {
                                if (width > MAX_WIDTH) {
                                    height *= MAX_WIDTH / width;
                                    width = MAX_WIDTH;
                                }
                            } else {
                                if (height > MAX_HEIGHT) {
                                    width *= MAX_HEIGHT / height;
                                    height = MAX_HEIGHT;
                                }
                            }
                            canvas.width = width;
                            canvas.height = height;

                            var ctx = canvas.getContext("2d");
                            ctx.drawImage(image, 0, 0, width, height);
                            var shrinked = canvas.toDataURL('image/jpeg');
                            handler(shrinked,e);
                        });
                        changeEvent.target.value="";
                    }
                    reader.readAsDataURL(changeEvent.target.files[0]);
                }
            });
        }
    };
});

