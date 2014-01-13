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
                    showPositiveFeedback("Utente aggiunto correttamente");
                    setTimeout(function() {
                        window.location.href = "/application/login";
                    }, 2000);
                } else {
                    showNegativeFeedback(data.errorMessage);
                }
            }
        }
    };
    self.actions = new function() {
        var actions = this;
        actions.signUp = function() {
            if ($("#sign-up-form").validate().form()) {
                self.services.signUp.request();
            }
        };
    };

    self.domUtils = new function() {
        var domUtils = this;

        domUtils.initValidation = function() {
            $("#sign-up-form").validate({
                rules: {
                    firstName: "required",
                    lastName: "required",
                    email: {
                        required: true,
                        email: true
                    },
                    username: {
                        required: true,
                        minlength: 3
                    },
                    password: {
                        required: true,
                        minlength: 6
                    },
                    confirmPassword: {
                        equalTo: "#password"
                    }
                },
                messages: {
                    firstName: "Inserisci il nome",
                    lastName: "Inserisci il cognome",
                    email: {
                        required: "Inserisci l'email",
                        email: "L'email deve avere il seguente formato nome@dominio.com"
                    },
                    username: {
                        required: "Inserisci l'username che vuoi utilizzare",
                        minlength: "L'username dev'essere lungo almeno 3 caratteri"
                    },
                    password: {
                        required: "Scegli una password",
                        minlength: "La password dev'essere lungo almeno 6 caratteri"
                    },
                    confirmPassword: {
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
    var signUpViewModel = new signUpViewModelDefinition();
    ko.applyBindings(signUpViewModel, $(".signUpDiv")[0]);

    signUpViewModel.domUtils.initValidation();
});
