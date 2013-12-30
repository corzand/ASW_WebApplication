function userViewModelDefinition() {

    var self = this;
    self.firstName = ko.observable(loggedUser.firstName);
    self.lastName = ko.observable(loggedUser.lastName);
    self.username = ko.observable(loggedUser.username);
    self.email = ko.observable(loggedUser.email);

    if (loggedUser.picture !== '') {
        self.picture = ko.observable(loggedUser.picture);
    } else {
        self.picture = ko.observable('/style/images/user50.png');
    }

    self.oldPassword = ko.observable("");
    self.newPassword = ko.observable("");
    self.confirmNewPassword = ko.observable("");

    self.services = {
        "editUserData": {
            "request": function() {
                if (self.utils.validate()) {
                    var rSettings = new requestSettings();
                    rSettings.url = '/users/edituser/';
                    rSettings.requestData = JSON.stringify(self.services.editUserData.requestData());
                    rSettings.successCallback = self.services.editUserData.callback;
                    sendRequest(rSettings);
                } else {
                    alert("Validazione Fallita!");
                }
            },
            "requestData": function() {
                return {
                    firstName: self.firstName(),
                    lastName: self.lastName(),
                    email: self.email(),
                    username: self.username(),
                    password: self.newPassword(),
                    picture: self.picture()
                };
            },
            "callback": function(data) {
                if (!data.error) {
                    window.location.href = "/application/tasks";
                } else {
                    alert(data.errorMessage);
                }
            }
        }
    };

    self.actions = new function() {
        var actions = this;
        actions.editUser = function() {
            self.services.editUserData.request();
        };
    };

    self.utils = new function() {
        var utils = this;
        utils.validate = function() {
            if (self.oldPassword() === loggedUser.password
                    && self.newPassword() === self.confirmNewPassword()) {
                return true;
            } else {
                return false;
            }
        };
    };
}

$(document).ready(function() {
    //init view model and stuff
    $('#pictureButton').change(function(evt) {
        var file = evt.target.files[0];
        var reader = new FileReader();

        reader.onload = function(e) {
            userViewModel.picture(reader.result);
        };

        reader.readAsDataURL(file);
    });
    var userViewModel = new userViewModelDefinition();
    ko.applyBindings(userViewModel, $(".editUserDiv")[0]);
});






