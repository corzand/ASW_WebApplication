var loggedUser;
var applicationViewModel;

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

        app.services = {
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

        app.actions = new function() {
            var actions = this;
            actions.logout = function() {
                app.services.logout.request();
            };
        };
    };
}

$(document).ready(function() {
    //init view model and stuff
    applicationViewModel = new applicationViewModelDefinition();
    ko.applyBindings(applicationViewModel, $(".navigation > .items")[0]);
});



