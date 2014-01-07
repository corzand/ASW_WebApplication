function requestSettings() {
    var self = this;
    self.url = "";
    self.type = "POST";
    self.dataType = 'json';
    self.requestData = null;
    self.contentType = "application/json; charset=utf-8";
    self.successCallback = null;
    self.errorCallback = null;
    self.callbackParameter = null;
}

function sendRequest(settings) {
    return $.ajax({
        url: settings.url,
        type: settings.type,
        contentType: settings.contentType,
        dataType: settings.dataType,
        data: settings.requestData,
        success: function(data, textStatus, jqXHR) {
            if (settings.successCallback) {
                settings.successCallback(data, settings.callbackParameter);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
            if (settings.errorCallback) {
                settings.errorCallback(settings.callbackParameter);
            }
        }
    });

}

function dateDiff(d1, d2) {
    return parseInt((d2 - d1) / (24 * 3600 * 1000));
}

function getMidnightDate(d){
    return new Date(d.setHours(0,0,0,0));
}
    

function compareDate(d1, d2) {

    if (d1.getDate() === d2.getDate() && d1.getMonth() === d2.getMonth() && d1.getFullYear() === d2.getFullYear()) {
        return true;
    } else {
        return false;
    }

}