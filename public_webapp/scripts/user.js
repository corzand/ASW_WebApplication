/*
 * Costruttore del ViewModel da bindare alla View User.
 * I campi observable sono compilati con i dati dell'utente loggato
 * e il binding è gestito automaticamente da knockout. 
 * Ovviamente, per motivi di sicurezza, non viene automaticamente compilato il campo 
 * "vecchia password", sarà l'utente a dover inserire la password tutte le volte che 
 * vorrà effettuare la modifica dei suoi dati
 */
function UserViewModelDefinition() {

    var self = this;
    self.firstName = ko.observable(loggedUser.firstName);
    self.lastName = ko.observable(loggedUser.lastName);
    self.username = loggedUser.username;
    self.email = ko.observable(loggedUser.email);
    self.userPicture = ko.observable(loggedUser.picture);
    self.newPicture = "";
    
    //Se non è presente la foto, viene mostrata una foto di default.
    self.picture = ko.computed(function() {
        if (self.userPicture() !== '') {
            return self.userPicture();
        } else {
            return 'style-sheets/images/user_light.png';
        }
    });

    self.oldPassword = ko.observable("");
    self.newPassword = ko.observable("");
    self.confirmNewPassword = ko.observable("");

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
        "editUserData": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/users/edituser/';
                rSettings.requestData = JSON.stringify(self.services.editUserData.requestData());
                rSettings.successCallback = self.services.editUserData.callback;
                sendRequest(rSettings);
            },
            "requestData": function() {
                //Al server inviamo i dati modificati dell'utente
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
                    showPositiveFeedback("Utente aggiornato correttamente.");
                    setTimeout(function() {
                        window.location.href = context + "/application/tasks";
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
         * Handler di click sul bottone modifica utente
         */
        actions.editUser = function() {
            if ($(".edit-user").validate().form()) {
                self.services.editUserData.request();
            }
        };
        
        /*
         * Handler di click sul bottone read image
         */
        actions.readImage = function() {
            $('#pictureButton').click();
        };
    };

    self.domUtils = new function() {
        var domUtils = this;

        /*
         * Si inizializza l'handler di evento change dell' input[type="file"],
         * una volta che l'utente seleziona un file immagine, l'immagine viene 
         * letta utilizzando le api HTML5 di file read, e salvata la stringa di 
         * byte corrispondente
         */
        domUtils.initFileReader = function() {
            
            $('#pictureButton').change(function(evt) {
                var file = evt.target.files[0];
                var reader = new FileReader();

                reader.onload = function(e) {
                    self.newPicture = reader.result;
                    self.userPicture(reader.result);
                };

                reader.readAsDataURL(file);
            });
        };
        
        /*
         * Funzione per inizializzare il validator jquery del form.
         * Vengono esplicitati i campi obbligatori e i campi con formato
         * particolare. La gestione degli errori è comune ed è la funzione
         * customInvalidHandler nel file utility.js
         */
        domUtils.initValidation = function() {
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
                            } else {
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
    //Inizializzazione del viewmodel e applicazione del binding
    var userViewModel = new UserViewModelDefinition();
    ko.applyBindings(userViewModel, $(".edit-user")[0]);

    //inizializzazione del validator e dell'handler sul cambio immagine
    userViewModel.domUtils.initValidation();
    userViewModel.domUtils.initFileReader();
});
