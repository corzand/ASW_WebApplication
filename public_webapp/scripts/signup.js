/*
 * Costruttore del ViewModel da bindare alla View di SignUp.
 * I campi observable sono quelli che verranno compilati dall'utente che si vuole
 * registrare, il binding 2-vie è gestito automaticamente da knockout
 */
function SignUpViewModelDefinition() {
    var self = this;
    self.firstName = ko.observable('');
    self.lastName = ko.observable('');
    self.email = ko.observable('');
    self.username = ko.observable('');
    self.password = ko.observable('');
    self.confirmPassword = ko.observable('');

    /*
    * L'oggetto services contiene, per ogni servizio che è possibile richiamare,
    * un oggetto strutturato in questo modo
    * {
    *      request: function(){},
    *      requestData: function(){},
    *      callback: function(){} 
    * }
    * 
    * dove request esegue la funzione di chiamata del servizio, requestData 
    * ritorna i dati da mandare al server, e la callback è invocata in caso di successo
    */
    self.services = {
        /*
         * Chiamata al servizio di signup
         */
        "signUp": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/users/signup/';
                rSettings.requestData = JSON.stringify(self.services.signUp.requestData());
                rSettings.successCallback = self.services.signUp.callback;
                return sendRequest(rSettings);
            },
            "requestData": function() {
                //Vengono passati al server tutti i dati dell'utente che si vuole registrare.
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
                        window.location.href = context + "/application/login";
                    }, 2000);
                } else {
                    showNegativeFeedback(data.errorMessage);
                }
            }
        }
    };
    
    /*
    * L'oggetto actions contiene gli handler degli eventi che possono essere scatenati
    * dall'utente premendo i vari button presenti nella pagina. Alcuni di essi sono bindati attraverso
    * il click binding di knockout
    */
    self.actions = new function() {
        var actions = this;
        /*
         * Event handler dell'evento di click sul bottone di signup
         */
        actions.signUp = function() {
            //validazione dei campi
            if ($("#sign-up-form").validate().form()) {
                self.services.signUp.request();
            }
        };
    };

    self.domUtils = new function() {
        var domUtils = this;

        /*
         * Inizializzazione del form che contiene i campi da validare, vengono 
         * spercificati i campi obbligatori e quelli che devono avere un formato 
         * particolare. La funzione di gestione dell'errore di validazione è comune
         * e situata in utility.js
         */
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
    
    //Inizializzazione del viewModel e applicazione del binding
    var signUpViewModel = new SignUpViewModelDefinition();
    ko.applyBindings(signUpViewModel, $(".signUpDiv")[0]);

    //Inizializzazione del validator jquery
    signUpViewModel.domUtils.initValidation();
});
