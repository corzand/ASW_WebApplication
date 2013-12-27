/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function signUpViewModelDefinition() {
    var self = this;

    self.firstName = ko.observable('');
    self.lastName = ko.observable('');
    self.email = ko.observable('');
    self.username = ko.observable('');
    self.password = ko.observable('');
    self.confirmPassword = ko.observable('');

    self.signUpData = function() {
        return {
            firstName: self.firstName(),
            lastName: self.lastName(),
            email: self.email(),
            username: self.username(),
            password: self.password()
        };
    };

    self.signUp = function() {
        var rSettings = new requestSettings();
        rSettings.url = '/application/signup/';
        rSettings.requestData = JSON.stringify(self.signUpData());
        rSettings.successCallback = self.signUpCallback;

        sendRequest(rSettings);
    };

    self.signUpCallback = function(data) {
        if (!data.error) {
            window.location.href = "/application/login";
        } else {
            alert(data.errorMessage);
        }
    };
}

$(document).ready(function() {
    //init view model and stuff
    var signUpViewModel = new signUpViewModelDefinition();
    ko.applyBindings(signUpViewModel, $(".signUpDiv")[0]);
});
