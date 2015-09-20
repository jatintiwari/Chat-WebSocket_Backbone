define(function(require){

	"use strict";

	$ 				=require('jquery'),
	Backbone		=require('backbone');


	var ViewUserChat = Backbone.View.extend({
		initialize:function(){
			this.template= _.template(require("text!chat/tpl/tplChatLayout.html"));
			this.payLoad={};
		},

		events:{
			"click #send":"send",
			"keyup #message":"message",
			"focus #message":"message",
			"click #uploadImage":"image",
			"change #image":"uploadImage"
		},

		render:function(){
			this.$el.html(this.template());
			return this;
		},

		image:function(){
			$('#image').click();
		},

		uploadImage:function(e){
			var _this= this;
			var file =e.target.files[0];
			if(file.size<65000){
				var reader = new FileReader();
				reader.readAsDataURL(file);
				reader.onload= function(event){
					$('#fileName').html(file.name);
					require(["util","chat/model/modelFileList"],
							function(util,ModelFileList){
						_this.model = new ModelFileList.ModelFile();
						_this.model.set("toUser",indexRouter.toUser);
						_this.model.set("file",event.target.result);
						_this.model.set("displayTime",util.getCurrnentDateAndTime());
						_this.model.set(file);
						_.extend(_this.payLoad,_this.model);
						_this.url=_this.model.url;
					});
				};

			}
		},

		message:function(){
			var _this= this;
			var message = $.trim($("#message").val());
			if(message=="" || message==null){
				return;
			};
			require(["util","chat/model/modelChatList"],function(util,modelChatList){
				_this.model = new modelChatList.ModelChatMessage();	
				_this.model.set("message",message);
				_this.model.set("fromUser",indexRouter.modelCurrentUser.attributes.username);
				_this.model.set("toUser",indexRouter.toUser);
				_this.model.set("displayTime",util.getCurrnentDateAndTime());
				_.extend(_this.payLoad,({"message":_this.model.get("message"),
					"toUser":indexRouter.toUser}));
				_this.url=_this.model.url;
			});
		},

		send:function(){
			var _this =this;
			if(this.url==null){
				return;
			}
			indexRouter.stompClient.send(this.url,{},JSON.stringify(this.payLoad));
			if(this.url==="/message"){
				if(indexRouter.modelUserChatList.at(0).attributes.success==undefined){
					indexRouter.modelUserChatList.add(_this.model);
				}else if(indexRouter.modelUserChatList.at(0).attributes.success=="false"){
					indexRouter.modelUserChatList.reset(_this.model);
					$("#idChatList").html(new ViewUserChatList({collection:indexRouter.modelUserChatList}).render().el);
				}
				$("#message").val("");
			}else{
				if(indexRouter.modelFiles.at(0).attributes.success==undefined){
					var sameNameModel={};
					var i=0;
					while(sameNameModel!==undefined){
						sameNameModel =indexRouter.modelFiles.findWhere({name:_this.model.get("name")});
						var fileName = _this.model.get("name").split(".");
						if(sameNameModel!=undefined){
							_this.model.set("name",fileName[0].split("-")[0]+"-"+(++i)+"."+fileName[1]);	
						}
					}
					indexRouter.modelFiles.add(_this.model);
				}else if(indexRouter.modelFiles.at(0).attributes.success=="false"){
					indexRouter.modelFiles.reset(_this.model);
					$("#idFilesDiv").html(new ViewFilesList({collection:indexRouter.modelFiles}).render().el);
				}
				$('#fileName').empty();
				$("#image").val("");
			}
			this.url=null;
			this.payLoad={};
			return;
		}

	});

	var ViewUserChatList= Backbone.View.extend({

		initialize:function(){
			this.listenTo(this.collection,"reset",this.render);
			this.listenTo(this.collection,"add",this.addOne);
		},

		events:{

		},

		render:function(){
			this.$el.empty();
			var _this= this;
			_.each(this.collection.models,function(model){
				_this.addOne(model);
			});
			return this;
		},
		addOne:function(model){
			this.$el.append(new ViewUserChatListItem({model:model}).render().el);
			$("#idChatList")[0].scrollTop = $("#idChatList")[0].scrollHeight;
		}
	});

	var ViewUserChatListItem = Backbone.View.extend({

		initialize:function(){
			this.listenTo(this.model,"change",this.render);
			this.template = _.template(require("text!chat/tpl/tplChatListItem.html"));
		},
		events:{

		},
		render:function(){
			this.$el.html(this.template(this.model.toJSON()));
			return this;
		}

	});

	var ViewFilesList =  Backbone.View.extend({
		initialize:function(){
			this.listenTo(this.collection,"reset",this.render);
			this.listenTo(this.collection,"add",this.addOne);
		},

		events:{

		},

		render:function(){
			var _this=this;
			_.each(this.collection.models,function(file){
				_this.addOne(file);
			});
			return this;
		},
		addOne:function(file){
			this.$el.append(new ViewFilesListItem({model:file}).render().el);
		}

	});
	
	var ViewFilesListItem = Backbone.View.extend({
		className:"fileListItem",
		
		initialize:function(){
			this.template=_.template(require("text!chat/tpl/tplFilesListItem.html"));
		},
		
		events:{
			"click":"getFile"
		},
		getFile:function(){
			return;
		},
		render:function(){
			this.$el.html(this.template(this.model.toJSON()));
			return this;
		}
		
	});

	return{
		ViewUserChat:ViewUserChat,
		ViewUserChatList:ViewUserChatList,
		ViewUserChatListItem:ViewUserChatListItem,
		ViewFilesList:ViewFilesList,
	};


});