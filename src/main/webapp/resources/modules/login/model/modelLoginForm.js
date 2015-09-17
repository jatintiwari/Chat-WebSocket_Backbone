
define(function (require) {
  "use strict";
  Backbone            = require('backbone');
  
var ModelLoginForm= Backbone.Model.extend({

url:"j_spring_security_check",

defaults:{
	j_username:"",
	j_password:""
},
validation:{
	j_username: [{
      required: true,
      msg: 'Useranme is required'
    }],
    j_password:{
		required:true,
		msg:"Password is requried"
	}
}

});

return{
  ModelLoginForm : ModelLoginForm
};
});
