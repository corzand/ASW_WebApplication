/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function signUpViewModelDefinition(){
    var self = this;
    
    self.firstName = ko.observable('Gianni');
    self.lastName = ko.observable('Drudi');
    self.email = ko.observable('');
    self.username = ko.observable('');
    self.password = ko.observable('');
    self.confirmPassword = ko.observable('');
    
    self.signUpData = function(){
        return {
            firstName : self.firstName(),
            lastName : self.lastName(),
            email : self.email(),
            username : self.email(),
            password : self.password()
        };
    };
    
    self.signUp = function(){
        //Ajax call
        console.log(self.firstName());
        console.log(self.lastName());
        
        $.ajax({
            url: 'http://localhost:8080/ASW_WebApplication/application/signup',
            type: 'POST',
            data: JSON.stringify(self.signUpData()),
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            success: function(data, textStatus, jqXHR) {
                //ok
                if(typeof data !== 'Object'){
                    var data = JSON.parse(data);
                }
                
                if(!data.error){
                    alert('ok');
                }else {
                    alert('error');
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                //error
                alert('xhr error');
            }
        });
    };
}

$(document).ready(function (){
    //init view model and stuff
    var signUpViewModel = signUpViewModelDefinition();
    ko.applyBindings(signUpViewModel, $("#signUpDiv")[0]);
});
