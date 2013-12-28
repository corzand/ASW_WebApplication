function userViewModelDefinition() {
    var user = this;
    user.firstName = ko.observable(loggedUser.firstName);
    user.lastName = ko.observable(loggedUser.lastName);
    user.username = ko.observable(loggedUser.username);
    user.email = ko.observable(loggedUser.email);

    if (loggedUser.picture !== '') {
        user.picture = ko.observable(loggedUser.picture);
    } else {
        user.picture = ko.observable('/style/images/user50.png');
    }

    user.oldPassword = ko.observable("");
    user.newPassword = ko.observable("");
    user.confirmNewPassword = ko.observable("");

    user.editUserData = function() {
        return {
            firstName: user.firstName(),
            lastName: user.lastName(),
            email: user.email(),
            username: user.username(),
            password: user.newPassword(),
            picture: user.picture()
        };
    };

    user.validate = function() {
        if (user.oldPassword() === loggedUser.password
                && user.newPassword() === user.confirmNewPassword()) {
            return true;
        } else {
            return false;
        }
    };

    user.editUser = function() {

        if (user.validate()) {
            var rSettings = new requestSettings();
            rSettings.url = '/users/edituser/';
            rSettings.requestData = JSON.stringify(user.editUserData());
            rSettings.successCallback = user.editUserCallback;
            sendRequest(rSettings);
        } else {
            alert("Validazione Fallita!");
        }
    };

    user.editUserCallback = function(data) {
        if (!data.error) {
            window.location.href = "/application/index";
        } else {
            alert(data.errorMessage);
        }
    };
}
;


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






