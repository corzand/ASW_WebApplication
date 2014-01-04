function signUpViewModelDefinition() {
    var self = this;
    self.firstName = ko.observable('');
    self.lastName = ko.observable('');
    self.email = ko.observable('');
    self.username = ko.observable('');
    self.password = ko.observable('');
    self.confirmPassword = ko.observable('');
    
    self.services = {
        "signUp": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/users/signup/';
                rSettings.requestData = JSON.stringify(self.services.signUp.requestData());
                rSettings.successCallback = self.services.signUp.callback;
                return sendRequest(rSettings);
            },
            "requestData": function() {
                return {
                    firstName: self.firstName(),
                    lastName: self.lastName(),
                    email: self.email(),
                    username: self.username(),
                    password: self.password()
                };
            },
            "callback": function(data) {
                if (!data.error) {
                    window.location.href = "/application/login";
                } else {
                    alert(data.errorMessage);
                }
            }
        }
    };
    self.actions = new function() {
        var actions = this;
        actions.signUp = function() {
            //validazione prima di request if (validate) then request
            if (self.utils.validate()) self.services.signUp.request();
            else {
                alert("Ricontrollare i campi!");
            }
        };
    };
    
    self.utils = new function() {
        var utils = this;
        
        utils.validate = function() {
            if (self.firstName() === "" || self.lastName() ===  "" ||
                    self.email() ===  "" || self.username() ===  "" ||
                    self.password() ===  "" || self.confirmPassword() ===  "" ||
                    self.password() !== self.confirmPassword()){
                return false;
            }
            else {
                return true;
            }
        };
    };
}


$(document).ready(function() {
//init view model and stuff
    var signUpViewModel = new signUpViewModelDefinition();
    ko.applyBindings(signUpViewModel, $(".signUpDiv")[0]);
});
