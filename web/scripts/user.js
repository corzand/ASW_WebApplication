function userViewModelDefinition() {

    var self = this;
    self.firstName = ko.observable(loggedUser.firstName);
    self.lastName = ko.observable(loggedUser.lastName);
    self.username = ko.observable(loggedUser.username);
    self.email = ko.observable(loggedUser.email);
    self.userPicture = ko.observable(loggedUser.picture);
    self.newPicture = "";
    self.picture = ko.computed(function() {
        if (self.userPicture() !== '') {
            return self.userPicture();
        } else {
            return '/style/images/user50.png';
        }
    });

    self.oldPassword = ko.observable("");
    self.newPassword = ko.observable("");
    self.confirmNewPassword = ko.observable("");

    self.services = {
        "editUserData": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/users/edituser/';
                rSettings.requestData = JSON.stringify(self.services.editUserData.requestData());
                rSettings.successCallback = self.services.editUserData.callback;
                sendRequest(rSettings);
            },
            "requestData": function() {
                return {
                    firstName: self.firstName(),
                    lastName: self.lastName(),
                    email: self.email(),
                    username: self.username(),
                    password: self.newPassword(),
                    picture: self.newPicture
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
            if (self.utils.validate()) {
                self.services.editUserData.request();
            }
            else {
                alert("Ricontrollare i campi!");
            }
        };
    };

    self.utils = new function() {
        var utils = this;

        utils.validate = function() {
            if (self.oldPassword() === loggedUser.password && self.firstName() !== "" && self.lastName() !== "" && self.email() !== "" && utils.checkMail(self.email())) {
                if (self.newPassword() !== "") {
                    if(self.newPassword() === self.confirmNewPassword()){
                        return true;
                    }else{
                        return false;
                    }
                    return true;
                }
            } else {
                return false;
            }
        };

        utils.checkMail = function(email) {

            return  /^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_.-])+.([a-zA-Z])+([a-zA-Z])+/.test(email);

        };

    };
}

$(document).ready(function() {
    //init view model and stuff
    $('#pictureButton').change(function(evt) {
        var file = evt.target.files[0];
        var reader = new FileReader();

        reader.onload = function(e) {
            userViewModel.newPicture = reader.result;
            userViewModel.userPicture(reader.result);
        };

        reader.readAsDataURL(file);
    });
    var userViewModel = new userViewModelDefinition();
    ko.applyBindings(userViewModel, $(".editUserDiv")[0]);
});






