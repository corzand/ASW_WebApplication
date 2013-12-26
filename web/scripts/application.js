/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var loggedUser;

function applicationViewModelDefinition(){
    var self = this;
    
    self.User = new function() {
        var user = this;       
        user.firstName = ko.observable(loggedUser.firstName);
        user.lastName = ko.observable(loggedUser.lastName);
        user.picture = ko.observable(loggedUser.picture);
    };
    
    self.Application = new function(){
        var app = this;
        app.logout = function(){        
            $.ajax({
            url: '/application/logout/',
            type: 'POST',
            contentType: 'application/json; charset=utf-8',
            success: function(data, textStatus, jqXHR) {                
                if(!data.error){
                    
                    window.location.href = "/application/login";
                }else {
                    alert('Error');
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                //error
                alert('xhr error');
            }
        });
        };
    };
}

$(document).ready(function (){
    //init view model and stuff
    var applicationViewModel = new applicationViewModelDefinition();
    ko.applyBindings(applicationViewModel, $(".navigation > .items")[0]);
});



