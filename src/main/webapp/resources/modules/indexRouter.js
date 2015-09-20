define(function(require){

	"use strict";

	$				=require('jquery'),
	Backbone		=require('backbone');

	return Backbone.Router.extend({

		initialize:function(){
			console.log("web socket demo!");
		},
		showIdAppSpace:function(){
			$('#idAppSpace').html(_.template(require("text!chat/tpl/tplBodyLayout.html")));
		},
		showUserList:function(view){
			$("#idUserslistDiv").html(view.render().el);
		},
		showChatLayout:function(view){
			$("#idChatDiv").html(view.render().el);
		},
		showUserChat:function(view){
			$("#idChatList").html(view.render().el);
		},
		showUserFiles:function(view){
			$("#idFilesDiv").html(view.render().el);
		},

		routes:{
			""				:"showUsersList",
			"logout"		:"logout"
		},
		routeInitialContent:function(){
			var _this= this;
			require(["settings/model/modeCurrentUser"],function(modelUser){
				_this.modelCurrentUser = new modelUser.ModelCurrentUser();
				_this.modelCurrentUser.fetch({
					success:function(){
						_this.showIdAppSpace();
						setTimeout(function(){
							_this.openWebSocketConnection();
						},100);
					}
				});
			});
			
		},
		logout:function(){
			var Logout = Backbone.Model.extend({url:"logout"});
			var logout = new Logout();
			logout.save({},{
				success:function(model,response,options){
					
				},
				error:function(model,response,options){
					window.location.reload();
				}
			});
			
			
		},

		showUsersList:function(){
			this.routeInitialContent();
			var _this= this;
			require(["chat/model/modelUserlist","chat/view/viewUserlist"],function(modelUserlist,viewUserList){
				_this.modelUsersList = new modelUserlist.ModelUsersList();
				_this.modelUsersList.fetch({
					success:function(collection,response){
						_this.viewUsersList = new viewUserList.ViewUsersList({collection:_this.modelUsersList});
						_this.showUserList(_this.viewUsersList);
					}
				});
			});
		},
		
		showChat:function(id){
			var _this=this;
			this.toUser=id;
			require(["chat/model/modelChatList","chat/model/modelFileList","chat/view/viewChat"],
					function(modelChatList,modelFileList,viewChat){
				_this.viewChatLayout = new viewChat.ViewUserChat();
				_this.showChatLayout(_this.viewChatLayout);
				_this.modelUserChatList = new modelChatList.ModelChatList();
				_this.modelUserChatList.fromUser=id;
				_this.modelUserChatList.fetch({
					success:function(model,response){
						_this.viewUserChatList = new viewChat.ViewUserChatList({collection:_this.modelUserChatList});
						if(response.success!=="false"){
							_this.showUserChat(_this.viewUserChatList);
							$("#idChatList")[0].scrollTop = $("#idChatList")[0].scrollHeight;
						}else{
							$("#idChatList").html("<section class='text-muted h4' style='text-align: center;margin-top: 150px;'>"+
									response.message+"</section>");
						}
					}
				});
				_this.modelFiles = new modelFileList.ModelFiles();
				_this.modelFiles.fromUser=_this.toUser;
				_this.modelFiles.fetch({
					success:function(model,response,options){
						_this.viewFilesList = new viewChat.ViewFilesList({collection:_this.modelFiles});
						if(response.success!="false"){
							_this.showUserFiles(_this.viewFilesList);
						}else{
							$("#idFilesDiv").html("<section class='text-muted h5' style='text-align: center;margin-top: 150px;'>"+
									response.message+"</section>");
						}
					}
				});
			});
		},
		openWebSocketConnection:function(){
			var _this= this;
			//open webSocket
			if(this.stompClient==null){
				require(["../lib/socket/sock","../lib/socket/stomp"],function(sock,stomp){
					var socket = new SockJS(_this.modelCurrentUser.attributes.websocketPath);
					_this.stompClient = Stomp.over(socket);
					_this.stompClient.connect({}, function(frame)  {
						_this.stompClient.subscribe('/user/websocket/message', function(greeting){
							var newMessage = JSON.parse(greeting.body);
							if(newMessage.markRead==="TRUE"){
								if(indexRouter.modelUserChatList){
									if(indexRouter.modelUserChatList.at(0).attributes.fromUser == newMessage.fromUser 
											|| indexRouter.modelUserChatList.at(0).attributes.toUser == newMessage.fromUser){
										console.log("marking read");
										var unReadMessageModel = indexRouter.modelUserChatList.where({messageRead:false});
										_.each(unReadMessageModel,function(model){
											console.log("marked read");
											model.set("messageRead",true);
										});
									}
									return;
								}
							}
							if(indexRouter.modelUserChatList !=null || indexRouter.modelUserChatList!=undefined){
								//match the first msg's fromUser or sentUser in the messages list with new msg the to update 
								//the userslist or current chat accordingly.
								if(indexRouter.modelUserChatList.at(0).attributes.fromUser == newMessage.fromUser 
										|| indexRouter.modelUserChatList.at(0).attributes.toUser == newMessage.fromUser){
									console.log('here1');
									if(indexRouter.modelUserChatList.at(0).attributes.success=="false"){
										console.log("here 1.1");
										//when current user has no message from the user whose chat window is open
										//and then he gets a msg.
										require(["chat/view/viewChat"],function(viewChat){
											indexRouter.modelUserChatList.reset(newMessage);
											_this.viewUserChatList = new viewChat.ViewUserChatList({collection:indexRouter.modelUserChatList});
											_this.showUserChat(_this.viewUserChatList);
											$("#idChatList")[0].scrollTop = $("#idChatList")[0].scrollHeight;
										});
									}
									else{
										//when the current user has msg from the user he is currently chating with, so add the msg
										//to the current chat.
										console.log("here 1.2");
										indexRouter.modelUserChatList.add(newMessage);
									}
									indexRouter.stompClient.send("/messages/read", {}, JSON.stringify({ "fromUser": newMessage.fromUser,
										"toUser": indexRouter.modelCurrentUser.attributes.username}));
									//by sending a frame we can mark all the messages as read, sent to the current user from the other
									//user in the current chat.
								}
								else{
									//when the current user is chating to some other user under messages tab, Update count on
									//messages tab and in users list.
									console.log('here2');
									var messageFrom = indexRouter.modelUsersList.findWhere({username:newMessage.fromUser});
									messageFrom.set("unReadMessages",Number(messageFrom.get('unReadMessages'))+1);
								}
							}else{
								//when user is not under messages tab and not chating with anyone. Just update messages tab
								console.log('here3');
								if(indexRouter.modelUsersList !=null || indexRouter.modelUsersList!=undefined){
									var messageFrom = indexRouter.modelUsersList.findWhere({username:newMessage.fromUser});
									messageFrom.set("unReadMessages",Number(messageFrom.get('unReadMessages'))+1);
								}
								if(indexRouter.undreadMessageCount){
									indexRouter.undreadMessageCount.set("count",indexRouter.permissions.userDetails.unReadMessage);
									indexRouter.viewUnreadMessageCount.render();
								}
							}
						});	
						indexRouter.stompClient.subscribe("/user/websocket/file",function(frame){
							var message = JSON.parse(frame.body);
							if(indexRouter.modelFiles){
								if(indexRouter.modelFiles.at(0).attributes.success==undefined){
									indexRouter.modelFiles.add(message);
								}else if(indexRouter.modelFiles.at(0).attributes.success=="false"){
									indexRouter.modelFiles.reset(message);
									var viewFilesList = require("chat/view/viewChat");
									$("#idFilesDiv").html(new viewFilesList.ViewFilesList({collection:indexRouter.modelFiles}).render().el);
								}
							}
							var messageFrom = indexRouter.modelUsersList.findWhere({username:message.fromUser});
							messageFrom.set("unReadMessages",Number(messageFrom.get('unReadMessages'))+1);
						});
						
					});
				});
			}
		}

	});

});