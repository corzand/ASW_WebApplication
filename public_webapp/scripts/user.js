

function userViewModelDefinition() {

    var self = this;
    self.firstName = ko.observable(loggedUser.firstName);
    self.lastName = ko.observable(loggedUser.lastName);
    self.username = loggedUser.username;
    self.email = ko.observable(loggedUser.email);
    self.userPicture = ko.observable(loggedUser.picture);
    self.newPicture = "";
    self.picture = ko.computed(function() {
        if (self.userPicture() !== '') {
            return self.userPicture();
        } else {
            return '/style-sheets/images/user50.png';
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
                    username: self.username,
                    oldPassword: self.oldPassword(),
                    newPassword: self.newPassword(),
                    picture: self.newPicture
                };
            },
            "callback": function(data) {
                if (!data.error) {
                    ShowPositiveFeedback("Utente aggiornato correttamente.");
                    setTimeout(function() {
                        window.location.href = "/application/tasks";
                    }, 2000);
                } else {
                    ShowNegativeFeedback(data.errorMessage);
                }
            }
        }
    };
    self.actions = new function() {
        var actions = this;
        actions.editUser = function() {
            if ($(".edit-user").validate().form()) {
                self.services.editUserData.request();
            }
        };
    };

    self.utils = new function() {
        var utils = this;

        utils.initValidation = function() {
            $(".edit-user").validate({
                rules: {
                    firstName: "required",
                    lastName: "required",
                    email: {
                        required: true,
                        email: true
                    },
                    oldPassword: {
                        required: true
                    },
                    confirmNewPassword: {
                        equalTo: function() {
                            if (self.newPassword() !== "") {
                                return "#newPassword";
                            }
                            else
                            {
                                return "#confirmNewPassword";
                            }
                        }
                    }
                },
                messages: {
                    firstName: "Inserisci il nome",
                    lastName: "Inserisci il cognome",
                    email: {
                        required: "Inserisci un email",
                        email: "L'email deve avere il seguente formato nome@dominio.com"
                    },
                    oldPassword: {
                        required: "Inserisci la tua password attuale"
                    },
                    confirmNewPassword: {
                        equalTo: "Le due password devono coincidere"
                    }
                },
                errorPlacement: function(error, element) {
                },
                invalidHandler: customInvalidHandler
            });
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
    ko.applyBindings(userViewModel, $(".edit-user")[0]);

    userViewModel.utils.initValidation();
});
