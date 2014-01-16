/*
 * File javascript contenente le funzioni comuni che possono essere utilizzate in tutti gli scope javascript
 * dell'applicazione
 */

/*
 * Restituisce un oggetto contenente i campi utilizzati per inviare una $.ajax request al server
 */
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

/*
 * Funzione che, sulla base del parametro settings, prepara una $.ajax request e ritorna
 * l'oggetto xhr.
 */
function sendRequest(settings) {
    return $.ajax({
        url: context + settings.url,
        type: settings.type,
        contentType: settings.contentType,
        dataType: settings.dataType,
        data: settings.requestData,
        success: function(data, textStatus, jqXHR) {
            //Se viene passata una callback, questa callback viene eseguita passando
            //come parametro la risposta del server, unita ad un oggetto che è possibile
            //inserire nei settings
            if (settings.successCallback) {
                settings.successCallback(data, settings.callbackParameter);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            //L'abort della request (chiamato manualmente), nel nostro caso non è considerato 
            //errore, di conseguenza il feedback negativo viene mostrato soltanto negli altri casi 
            if (textStatus !== "abort" && errorThrown !== "") {
                showNegativeFeedback(errorThrown);
                if (settings.errorCallback) {
                    settings.errorCallback(settings.callbackParameter);
                }
            }
        }
    });

}

/*
 * Calcola la differenza (in giorni) tra due date javascript 
 */
function dateDiff(d1, d2) {
    return parseInt((d2 - d1) / (24 * 3600 * 1000));
}

/* 
 * Data in ingresso una data javascript, ritorna una data nello stesso giorno,
 * ma settata alle 0:00:00
 */
function getMidnightDate(d) {
    return new Date(d.setHours(0, 0, 0, 0));
}

/*
 * Compara due date e ritorna true se giorno/mese/anno coincidono
 */
function compareDate(d1, d2) {

    if (d1.getDate() === d2.getDate() && d1.getMonth() === d2.getMonth() && d1.getFullYear() === d2.getFullYear()) {
        return true;
    } else {
        return false;
    }

}

/*
 * Funzione utilizzata per decodificare un'HTML entity (ad esempio &nbsp;)
 * nel corrispettivo carattere testuale 
 */
function decodeHtmlEntity(str) {
    return str.replace(/&#(\d+)/g, function(match, dec) {
        return String.fromCharCode(dec);
    });
}

/*
 * Funzione utilizzata per mostrare gli eventuali errori (in popup) ogni volta
 * che è necessaria una validazione client-side
 */
function customInvalidHandler(event, validator) {    
    $("#validation-dialog").empty();

    for (var i = 0; i < validator.errorList.length; i++) {
        var $error = $("<span>").text(validator.errorList[i].message);
        $("#validation-dialog").append($error);
    }

    //La popup è costruita attraverso il componente dialog di jquery UI
    $("#validation-dialog").dialog({
        dialogClass: 'alert',
        autoOpen: true,
        modal: true,
        close: function() {
            $("#validation-dialog").dialog("destroy");
        }
    });
}

/*
 * Funzione che mostra nella parte superiore della viewport un box 
 * (colore verde) contenente il messaggio passato come parametro
 */
function showPositiveFeedback(message) {
    var $div = $("<div>", {class: "fade-in-box positive-feedback-box"});
    $div.append($("<span>").text(message));
    $("body").append($div);
    $div.fadeIn("slow", function() {
        setTimeout(function() {
            $div.fadeOut("slow", function() {
                $div.remove();
            });
        }, 2000);
    });
}

/*
 * Funzione che mostra nella parte superiore della viewport un box 
 * (colore rosso) contenente il messaggio passato come parametro
 */
function showNegativeFeedback(message) {
    var $div = $("<div>", {class: "fade-in-box negative-feedback-box"});
    $div.append($("<span>").text(message));
    $("body").append($div);
    $div.fadeIn("slow", function() {
        setTimeout(function() {
            $div.fadeOut("slow", function() {
                $div.remove();
            });
        }, 2000);
    });
}