//Variabile globale contenente le informazioni sull'utente loggato
var loggedUser; 

/*
 * Costruttore dell'oggetto applicationViewModel bindato alla topbar presente in tutte le pagine.
 * Contiene le informazioni sull'utente loggato e la action di logout, anch'essa bindata 
 * direttamente sulla view con l'utilizzo di knockout.
 */
function ApplicationViewModelDefinition() {
    var self = this;

    //Oggetto che rappresenta il viewModel dell'Utente, contiene le informazioni
    //bindate direttamente in modo dichiarativo nella topbar
    self.User = new function() {
        
        var user = this;
        
        if (loggedUser !== undefined) {
            user.firstName = ko.observable(loggedUser.firstName);
            user.lastName = ko.observable(loggedUser.lastName);
            user.username = loggedUser.username;

            //Se la picture non è presente, si visualizza una picture di default.
            if (loggedUser.picture !== '') {
                user.picture = ko.observable(loggedUser.picture);
            } else {
                user.picture = ko.observable('/style-sheets/images/user50.png');
            }
        }
    };

    /*
     * Viewmodel dell'oggetto application, contiene le action e i servizi bindati 
     * alla topbar e che possono essere richiamati dall'utente.
     */
    self.Application = new function() {
        var app = this;

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
        app.services = {
            /*
             * Servizio attraverso il quale l'utente richiede al server il logout 
             */
            "logout": {
                "request": function() {
                    var rSettings = new requestSettings();
                    rSettings.url = '/users/logout/';
                    rSettings.successCallback = app.services.logout.callback;
                    sendRequest(rSettings);
                },
                "requestData": function() {
                    return null;
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
        
        /*
        * L'oggetto actions contiene gli handler degli eventi che possono essere scatenati
        * dall'utente premendo i vari button presenti nella pagina. Alcuni di essi sono bindati attraverso
        * il click binding di knockout
        */
        app.actions = new function() {
            var actions = this;
            /*
             * Action collegata all'evento di click del button logout
             */
            actions.logout = function() {
                app.services.logout.request();
            };
        };
    };
}

$(document).ready(function() {
    //Inizializzazione del viewmodel e del binding via knockout
    var applicationViewModel = new ApplicationViewModelDefinition();
    ko.applyBindings(applicationViewModel, $(".navigation > .items")[0]);
});



