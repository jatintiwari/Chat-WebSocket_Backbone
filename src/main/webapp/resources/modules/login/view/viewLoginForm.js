define(function (require) {

	"use strict";

	var $               = require('jquery'),
	_                   = require('underscore'),
	Backbone            = require('backbone'),
	util            	= require('util');

var ViewLoginForm = Backbone.View.extend({
	initialize:function(){
		this.template = _.template(require('text!login/tpl/tplLoginForm.html'));
		util.bindValidation(this);
	},

	events:{
		'change input':'setModel',
		'submit':'submit',
	},

	render:function(){
		$('#formBody').empty();
		this.$el.html(this.template(this.model.toJSON()));
		return this;
	},

	submit:function(e){
		e.preventDefault();
		util.laddaLoadingId('#signInButton');
		Backbone.emulateJSON = true;
		this.model.save(null,{
			data:this.model.toJSON(),
			success:function(model,response,options){
				if(response.success === "true") {
					window.location = response.targetUrl;
				}
			}
		});
		Backbone.emulateJSON = false;
	},
	setModel:function(e){
		var id=e.target.id;
		this.model.set(id,e.currentTarget.value);
		this.model.validate;
	}
});

return {
	ViewLoginForm : ViewLoginForm
};

});
