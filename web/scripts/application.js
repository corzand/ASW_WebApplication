/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var loggedUser;

function applicationViewModelDefinition() {
    var self = this;

    self.User = new function() {
        var user = this;
        user.firstName = ko.observable(loggedUser.firstName);
        user.lastName = ko.observable(loggedUser.lastName);
        user.password = ko.observable(loggedUser.password);
        user.username = ko.observable(loggedUser.username);
        user.email = ko.observable(loggedUser.email);
        
        if (loggedUser.picture !== '') {
            user.picture = ko.observable(loggedUser.picture);
        } else {
            user.picture = ko.observable('/style/images/user50.png');         
        }
    };

    self.Application = new function() {
        var app = this;

        app.logout = function() {
            var rSettings = new requestSettings();
            rSettings.url = '/application/logout/';
            rSettings.successCallback = app.logoutCallback;
            sendRequest(rSettings);
        };

        app.logoutCallback = function(data) {
            if (!data.error) {
                window.location.href = "/application/login";
            } else {
                alert(data.errorMessage);
            }
        };
    };
}

$(document).ready(function() {
    //init view model and stuff
    var applicationViewModel = new applicationViewModelDefinition();
    ko.applyBindings(applicationViewModel, $(".navigation > .items")[0]);
});



