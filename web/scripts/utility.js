/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function requestSettings(){
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

function sendRequest(settings){
    $.ajax({
        url: settings.url,
        type: settings.type,
        contentType: settings.contentType,
        dataType : settings.dataType,
        data: settings.requestData,
        success: function(data, textStatus, jqXHR) {                
            if(settings.successCallback){
                settings.successCallback(data, settings.callbackParameter);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if(settings.errorCallback){
                settings.errorCallback(settings.callbackParameter);
            }
        }
    });
}