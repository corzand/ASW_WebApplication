/*
 * 
 * @type TasksViewModelDefinition viewModel della pagina tasks
 */
var tasksViewModel;
/*
 * 
 * Enum utilizzato per discriminare i vari tipi di notifica inviati dal server
 */
var notificationType = {
    addedTask: 0,
    editedTask: 1,
    deletedTask: 2
};

/*
 * Costruttore del ViewModel della View Task, richiamato nell'evento $(document).ready (fondo pagina)
 * Oltre alle properties che rappresentano il model, sia sotto forma di ko.observable,
 * sia sotto forma di oggetti semplici javascript, sono presenti: 
 * il costruttore dell'oggetto Task;
 * il costruttore dell'oggetto Notification; 
 * 4 oggetti contenenti tutte le funzioni utilizzate a livello infrastrutturall: services, actions, utils, domUtils
 *  
 * @returns 
 */
function TasksViewModelDefinition() {

    var self = this;
    
    /*
     * Costrutture dell'oggetto Task.
     * Oltre alle properties standard vengono utilizzate anche delle properties
     * calcolate (computed) sfruttate nel binding
     * @param {type} params oggetto chiave/valore con tutti i campi del task
     * 
     */
    self.Task = function(params) {
        var task = this;
        task.id = ko.observable(params.id);
        task.title = ko.observable(params.title);
        task.description = ko.observable(params.description);
        task.date = new Date(params.date);
        task.done = ko.observable(params.done);
        task.personal = ko.observable(params.personal);
        task.userId = ko.observable(params.userId);
        task.AssignedUser = ko.observable(self.utils.getUserById(params.assignedUserId));
        task.Category = ko.observable(self.utils.getCategoryById(params.categoryId));
        task.latitude = ko.observable(params.latitude);
        task.longitude = ko.observable(params.longitude);
        task.attachment = ko.observable(params.attachment);
        task.timeStamp = params.timeStamp;

        //Utilizzata per visualizzare o meno il task nella timeline, sulla base dei filtri
        //selezionati dall'utente.
        task.visible = ko.computed(function() {
            var visible = false;
            
            //Categoria
            $.each(self.Categories(), function(index, category) {
                if (category.id() === task.Category().id()) {
                    visible = category.state();
                }
            });

            //Filtro pubblico/privato
            if (self.personal() && visible) {
                visible = visible && task.userId() === loggedUser.id
                        || (task.AssignedUser() && task.AssignedUser().id() === loggedUser.id);
            } else if (!self.personal() && visible) {
                visible = visible && task.userId() === loggedUser.id
                        || (task.AssignedUser() && task.AssignedUser().id() === loggedUser.id)
                        || !task.personal();
            }

            //Filtro task fatti
            if (task.done() && visible) {
                visible = self.done();
            }
            
            //Filtro task to-do
            if (!task.done() && visible) {
                visible = self.todo();
            }

            return visible;
        });
        
        /*
         * Utilizzata per sapere se il task è accessibile dall'utente o meno.
         * Principalmente utile nel caso si riceva una notifica comet relativa 
         * ad una  modifica che rende inaccessibile un task che l'utente poteva 
         * inizialmente vedere.
         */
        task.accessible = ko.computed(function() {
            var accessible = true;
            if (task.userId() === loggedUser.id) {
                accessible = accessible && true;
            } else if (task.personal() === false) {
                accessible = accessible && true;
            } else if (task.AssignedUser() && task.AssignedUser().id() === loggedUser.id) {
                accessible = accessible && true;
            } else {
                accessible = accessible && false;
            }
            
            return accessible;
        });
        
        //ritorna true se la data del task è < della data odierna ed il task
        //è segnato come "to-do"
        task.expired = ko.computed(function() {
            return !task.done() && (task.date.getTime() < self.today.getTime());
        });
        
        //ritorna true se il task è già stato assegnato ad un utente
        task.assigned = ko.computed(function() {
            return task.AssignedUser();
        });
    };

    /*
     * Costruttore dell'oggetto notification, utilizzato nel binding 
     * con il relativo div dove è mostrata la notifica
     */
    self.Notification = function() {
        var not = this;
        not.title = ko.observable();
        not.description = ko.observable();
        not.task = null;
        not.show = function() {
            self.actions.showNotificationTask(not.task.id);
        };
    };

    //ViewModel properties/objects bindati alla view
    self.personal = ko.observable(false);
    self.todo = ko.observable(true);
    self.done = ko.observable(true);
    //Le categorie disponibili
    self.Categories = ko.observableArray([]);
    //Gli utenti registrati
    self.Users = ko.observableArray([]);
    //L'array dei giorni, con i quali si costruirà la timeline dopo ogni search
    self.Days = ko.observableArray([]);
    self.startDate = new Date();
    self.endDate = new Date();
    self.today = getMidnightDate(new Date());
    
    //Oggetto javascript che rappresenta il nuovo task che si sta aggiungendo
    self.NewTask = new function() {
        var newTask = this;
        newTask.title = ko.observable("");
        newTask.description = ko.observable("");
        newTask.date = getMidnightDate(new Date());
        newTask.done = ko.observable(false);
        newTask.personal = ko.observable(false);
        newTask.latitude = ko.observable(0);
        newTask.longitude = ko.observable(0);
        newTask.AssignedUser = ko.observable();
        newTask.Category = ko.observable();
        newTask.attachment = ko.observable("");
    };

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
         * Chiamata al servizio per recuperare la lista degli utenti registrati sul sito.
         */
        "getUsers": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/users/users/';
                rSettings.successCallback = self.services.getUsers.callback;
                return sendRequest(rSettings);
            },
            "requestData": function() {
                return null;
            },
            "callback": function(data) {
                //Per ogni utente inviato dal server, viene creato e pushato 
                //un nuovo oggetto utente  nell'array degli utenti sul model.
                for (i = 0; i < data.users.length; i++) { 
                    self.Users.push({
                        id: ko.observable(data.users[i].id),
                        username: ko.observable(data.users[i].username),
                        picture: ko.observable(data.users[i].picture)
                    });
                }
            }
        },
        /*
         * Chiamata al servizio per recuperare la lista delle categorie disponibili per i task.
         */
        "getCategories": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/categories/';
                rSettings.successCallback = self.services.getCategories.callback;
                return sendRequest(rSettings);
            },
            "requestData": function() {
                return null;
            },
            "callback": function(data) {
                for (var i = 0; i < data.categories.length; i++) {
                    self.Categories.push({
                        //Per ogni categoria inviata dal server, viene creato e pushato 
                        //un nuovo oggetto categoria nell'array Categories sul model.
                        id: ko.observable(data.categories[i].id),
                        title: ko.observable(data.categories[i].title),
                        state: ko.observable(data.categories[i].state),
                        color: ko.observable(data.categories[i].color)
                    });
                }
            }
        },
        /*
         * Chiamata al servizio per recuperare i task visibili di una determinata fascia temporale
         */
        "search": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/search/';
                rSettings.requestData = JSON.stringify(self.services.search.requestData());
                rSettings.successCallback = self.services.search.callback;
                return sendRequest(rSettings);
            },
            "requestData": function() {
                //Restituisce un oggetto contenente data inizio, 
                //data fine e utente loggato da inviare al server per ricercare i task
                return {
                    startDate: self.startDate,
                    endDate: self.endDate,
                    userId: loggedUser.id
                };
            },
            "callback": function(data) {
                if (!data.error) {
                    //Si svuota l'array dei giorni e si ricostruisce per intero 
                    //la struttura, inserendo in fase iniziale tutti gli oggetti 
                    //Day { Day : {}, Tasks : [] } che vengono esplicitamente 
                    //selezionati dai filtri da-a.
                    
                    self.Days.removeAll();                    
                    var currentDay = new Date(self.startDate);
                    var range = dateDiff(self.startDate, self.endDate);
                    for (var i = 0; i <= range; i++) {
                        self.Days.push({
                            day: new Date(currentDay),
                            Tasks: ko.observableArray([])
                        });
                        currentDay.setDate(currentDay.getDate() + 1);
                    }

                    //Attraverso la funzione pushTask vengono inseriti i task nei
                    //giorni di loro competenza
                    for (i = 0; i < data.tasks.length; i++) {
                        self.utils.pushTask(data.tasks[i]);
                    }

                    //Si fa partire una nuova richiesta di polling, in attesa di 
                    //notifiche comet 
                    self.services.polling.request();
                }
            }
        },
        /*
         * Chiamata al servizio per ricevere notifiche relative ai task ai quali sono sottoscritto ed eventuali
         * altri task che ricadrebbero nella fascia temporale. Utilizzato il pattern COMET HTTP long polling.
         * Diversamente dagli altri, questo oggetto ha anche un campo che contiene l'oggetto xhr relativo all'ultima
         * richiesta di polling effettuata, perchè in determinate circostanze deve essere richiamato $.abort() della richiesta
         */
        "polling": {
            "lastPollingRequest": null,
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/polling/';
                rSettings.requestData = JSON.stringify(self.services.polling.requestData());
                rSettings.successCallback = self.services.polling.callback;
                
                //Se c'è una richiesta polling non ancora completata, la si fa 
                //abortire.
                if (self.services.polling.lastPollingRequest) {
                    self.services.polling.lastPollingRequest.abort();
                }
                self.services.polling.lastPollingRequest = sendRequest(rSettings);
            },
            "requestData": function() {                
                var taskIds = [];
                for (var i = 0; i < self.Days().length; i++) {
                    for (var j = 0; j < self.Days()[i].Tasks().length; j++) {
                        taskIds.push(self.Days()[i].Tasks()[j].id());
                    }
                }
                
                //L'oggetto contiene sia l'ultimo viewModel di ricerca esplicitamente
                //inviato al server, sia gli id dei task ai quali ci si è sottoscritti e 
                //sui quali si vuole ricevere un'eventuale notifica.
                return {
                    searchRequestViewModel: self.services.search.requestData(),
                    taskIds: taskIds
                };
            },
            "callback": function(data) {
                //Reset del campo lastPolling request
                self.services.polling.lastPollingRequest = null;

                if (!data.error) {
                    //Sulla base della notifica ricevuta, effettuo l'aggiornamento 
                    //del viewModel, aggiornando o eliminando il task del quale si è
                    //ricevuta la notifica
                    switch (data.operation) {
                        case notificationType.addedTask:
                        case notificationType.editedTask:                            
                            //In caso di aggiunta/modifica, mostro il relativo box 
                            //di notifica non basandomi sulla notifica in sè,
                            //ma sull'effettiva precedente presenza o meno del task
                            //tra quelli bindati nel mio viewmodel.
                            var task = self.utils.getTaskById(data.task.id);
                            if (task === null) {
                                //Non era presente sul mio viewModel, lo aggiungo.
                                self.utils.pushTask(data.task);
                                self.domUtils.showNotification(data.task, notificationType.addedTask);
                            } else {
                                //Era presente sul mio viewModel, lo aggiorno.
                                if (task.date.getTime() !== new Date(data.task.date).getTime()) {
                                    self.utils.removeTask(data.task);
                                    self.utils.pushTask(data.task);
                                } else {
                                    task.title(data.task.title);
                                    task.description(data.task.description);
                                    task.date = new Date(data.task.date);
                                    task.done(data.task.done);
                                    task.personal(data.task.personal);
                                    task.userId(data.task.userId);
                                    task.AssignedUser(self.utils.getUserById(data.task.assignedUserId));
                                    task.latitude(data.task.latitude);
                                    task.longitude(data.task.longitude);
                                    task.Category(self.utils.getCategoryById(data.task.categoryId));
                                    task.attachment(data.task.attachment);
                                    task.timeStamp = data.task.timeStamp;
                                }
                                self.domUtils.showNotification(data.task, notificationType.editedTask);
                            }
                            break;
                        case notificationType.deletedTask:
                            //è stato eliminato, lo rimuovo dal viewModel.
                            self.utils.removeTask(data.task);
                            self.domUtils.showNotification(data.task, notificationType.deletedTask);

                            break;
                    }
                    
                    //Faccio una nuova richiesta long-polling COMET
                    self.services.polling.request();
                }
            }
        },
        /*
         * Chiamata al servizio per aggiungere un nuovo task 
         */
        "add": {
            "request": function($dialog, task) {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/add/';
                rSettings.requestData = JSON.stringify(self.services.add.requestData(task));
                rSettings.successCallback = self.services.add.callback;
                if ($dialog) {
                    rSettings.callbackParameter = $dialog;
                }
                return sendRequest(rSettings);
            },
            "requestData": function(task) {
                //Preparo per il server un oggetto JSON che contiene 
                //tutti i campi necessari per la creazione di un nuovo Task.
                return {
                    title: task.title(),
                    description: task.description(),
                    date: task.date,
                    done: task.done(),
                    personal: task.personal(),
                    userId: loggedUser.id,
                    assignedUserId: task.AssignedUser() ? task.AssignedUser().id() : -1,
                    latitude: task.latitude(),
                    longitude: task.longitude(),
                    categoryId: task.Category() ? task.Category().id() : 1,
                    attachment: task.attachment()
                };
            },
            "callback": function(data, $dialog) {
                if (!data.error) {
                    
                    //Se l'aggiunta è andata a buon fine, inserisco il task nel mio viewModel                    
                    self.utils.pushTask(data.task);
                    
                    //Chiudo la dialog (se è stata passata come parametro)
                    if ($dialog) {
                        $dialog.dialog("close");
                    }
                    
                    //Resetto i campi del nuovo task
                    self.utils.resetNewTask();

                    //Aggiorno la richiesta di polling perchè c'è un potenziale 
                    //task in più a cui sono sottoscritto
                    self.services.polling.request();

                    //Mostro un feedback positivo
                    showPositiveFeedback("Salvataggio effettuato correttamente.");
                } else {
                    showNegativeFeedback(data.errorMessage);
                }
            }
        },
        /*
         * Chiamata al servizio per modificare un task esistente
         */
        "edit": {
            "request": function(task, boundTask, $dialog) {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/edit/';
                rSettings.requestData = JSON.stringify(self.services.edit.requestData(boundTask));
                rSettings.successCallback = self.services.edit.callback;
                rSettings.callbackParameter = {
                    "task": task,
                    "$dialog": $dialog
                };
                return sendRequest(rSettings);
            },
            "requestData": function(task) {
                //Passo al server l'oggetto contenente tutti i campi del task 
                //modificato. 
                return {
                    id: task.id(),
                    title: task.title(),
                    description: task.description(),
                    date: task.date,
                    done: task.done(),
                    personal: task.personal(),
                    userId: loggedUser.id,
                    assignedUserId: task.AssignedUser() ? task.AssignedUser().id() : -1,
                    latitude: task.latitude(),
                    longitude: task.longitude(),
                    categoryId: task.Category().id(),
                    attachment: task.attachment(),
                    timeStamp: task.timeStamp
                };
            },
            "callback": function(data, params) {
                var task = params.task;
                
                //Se il server mi ha inviato il task, lo aggiorno
                if (data.task) {
                    //Se necessario lo riposiziono in base alla sua data
                    if (task.date.getTime() !== new Date(data.task.date).getTime()) {
                        self.utils.removeTask(data.task);
                        if (self.startDate <= new Date(data.task.date) <= self.endDate) {
                            self.utils.pushTask(data.task);
                        }
                    } else {
                        //Non lo riposiziono, ma aggiorno tutti i campi
                        task.title(data.task.title);
                        task.description(data.task.description);
                        task.date = new Date(data.task.date);
                        task.done(data.task.done);
                        task.personal(data.task.personal);
                        task.userId(data.task.userId);
                        task.AssignedUser(self.utils.getUserById(data.task.assignedUserId));
                        task.latitude(data.task.latitude);
                        task.longitude(data.task.longitude);
                        task.Category(self.utils.getCategoryById(data.task.categoryId));
                        task.attachment(data.task.attachment);
                        task.timeStamp = data.task.timeStamp;
                    }
                }

                if (data.error) {
                    showNegativeFeedback(data.errorMessage);
                } else {
                    showPositiveFeedback("Salvataggio effettuato correttamente.");
                }

                if (params.$dialog) {
                    params.$dialog.dialog("close");
                }
            }
        },
        /*
         * Chiamata al servizio per eliminare un task esistente
         */
        "delete": {
            request: function(task, $dialog) {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/delete/';
                rSettings.requestData = JSON.stringify(self.services.delete.requestData(task));
                rSettings.successCallback = self.services.delete.callback;
                rSettings.callbackParameter = $dialog;
                return sendRequest(rSettings);
            },
            requestData: function(task) {
                //Per l'eliminazione, è sufficiente l'id, il timestamp per 
                //verificare se il task è aggiornato e l'utente loggato
                return {
                    id: task.id(),
                    timeStamp: task.timeStamp,
                    userId: loggedUser.id
                };
            },
            callback: function(data, $dialog) {
                if (!data.error) {
                    //Se il server dice che l'eliminazione è andata a buon fine, 
                    //rimuovo il task
                    self.utils.removeTask(data.task);
                    if ($dialog) {
                        $dialog.dialog("close");
                    }

                    //Aggiorno la richiesta di polling, perchè c'è un task in meno a cui siamo
                    //sottoscritti
                    self.services.polling.request();
                    
                    showPositiveFeedback("Eliminazione effettuata correttamente.");
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
         * Handler dell'evento click del button edit in popup
         */
        actions.edit = function(taskToEdit) {
            self.domUtils.openDialog(taskToEdit);
        };

        /*
         * Handler dell'evento click del button di aggiunta veloce (non in popup)
         */
        actions.addFast = function() {
            //Validazione del titolo
            if ($("#add-bar-form").validate().form()) {
                self.services.add.request(null, self.NewTask);
            }
        };

        /*
         * Handler dell'evento click sul button Aggiungi in popup
         * @param {type} $dialog - la dialog che dovremo chiudere una volta completata la chiamata al servizio
         * @param {type} task - il task che andiamo ad aggiungere 
         */
        actions.addDialog = function($dialog, task) {
            //Validazione del titolo
            if ($("#edit-task-form").validate().form()) {
                //passaggio della dialog e del task alla request
                self.services.add.request($dialog, task);
            }
        };

        /*
         * Handler dell'evento click del button Salva in popup
         * @param {type} task - il task originale che dovremo aggiornare dopo la chiamata al servizio
         * @param {type} boundTask - il task (copiato) che abbiamo modificato in popup
         * @param {type} $dialog - la dialog che dovrà essere chiusa una volta completata la chiamata
         */
        actions.save = function(task, boundTask, $dialog) {
            //Validazione del titolo
            if ($("#edit-task-form").validate().form()) {
                self.services.edit.request(task, boundTask, $dialog);
            }
        };

        /*
         * Handler per l'evento click del pulsante Applica nei filtri della timeline
         */
        actions.search = function() {      
            //Non è necessaria validazione, i datepicker sono sempre correttamente impostati
            self.services.search.request();            
        };

        /*
         * Handler per l'evento click della checkbox di un determinato task
         * @param {type} taskToMark - il task da checkare/decheckare dopo la chiamata al servizio (return true)
         */
        actions.markTask = function(taskToMark) {
            self.services.edit.request(taskToMark, taskToMark);
            return true;
        };

        /*
         * Handler per l'evento click del pulsante posticipa di un determinato task.
         * Il task viene semplicemente posticipato di un giorno
         * @param {type} taskToDelay - il task che andiamo a ritardare, aggiungendo un giorno alla sua data
         */
        actions.delay = function(taskToDelay) {
            var taskDelayed = self.utils.cloneTask(taskToDelay);
            taskDelayed.date.setDate(new Date(taskToDelay.date.getDate() + 1));
            self.services.edit.request(taskToDelay, taskDelayed);
        };

        /*
         * Handler per l'evento click del pulsante Elimina in popup 
         * @param {type} taskToDelete - il task da eliminare
         * @param {type} $dialog - la dialog che deve essere chiusa una volta completata la chiamata al servizio
         */
        actions.delete = function(taskToDelete, $dialog) {
            self.services.delete.request(taskToDelete, $dialog);
        };

        /*
         * Handler per l'evento di click del pulsante che apre/chiude i filtri sulla timeline
         * @param {type} data - default jquery
         * @param {type} event - default jquery
         */
        actions.toggleFilters = function(data, event) {
            self.domUtils.toggleFilters(event.target);
        };

        /*
         * Handler per l'evento di click del pulsante che apre/chiude la sidebar laterale a sinistra
         * @param {type} data - default jquery
         * @param {type} event - default jquery
         */
        actions.toggleCategories = function(data, event) {
            self.domUtils.toggleCategories(event.target);
        };

        /*
         * Handler per l'evento di click su una notifica, che apre la dialog con il task aggiunto/modificato.
         * Viene sollevato solo se la notifica non riguarda un task eliminato
         * @param {type} id - id del task da aprire in dialog
         */
        actions.showNotificationTask = function(id) {
            self.domUtils.openDialog(self.utils.getTaskById(id));
        };
    };
    
    /*
     * L'oggetto domUtils contiene tutte le funzioni utilizzate che interagiscono direttamente con 
     * il DOM. Possono essere sia funzioni di inizializzazione
     */
    self.domUtils = new function() {
        var domUtils = this;

        /*
         * Apre la popup (o dialog) ed effettua il binding con un oggetto Task clonato
         * rispetto a quello passato come parametro. Questo per evitare che il binding a 
         * 2 vie utilizzato da knockout varii il task originale in caso di modifiche che 
         * non vengono confermate lato-server con opportuna chiamata al servizio.
         * @param {type} task  - Il task da aprire e bindare alla popup
         */
        domUtils.openDialog = function(task) {
            var $dialog = $("#edit-task-popup");
            var boundTask;
            $dialog.dialog({
                autoOpen: true,
                width: 400,
                modal: true,
                open: function(event, ui) {
                    //Vengono mostrati i bottoni corretti, a seconda che sia modifica o aggiunta
                    if (task.id) {                        
                        $dialog.parent().find(".ui-dialog-buttonpane .add-button").hide();
                    } else {
                        $dialog.parent().find(".ui-dialog-buttonpane .save-button").hide();
                        $dialog.parent().find(".ui-dialog-buttonpane .delete-button").hide();
                    }

                    //si clona il task originale
                    boundTask = self.utils.cloneTask(task);

                    //lo si estende con gli oggetti necessari a popolare le select
                    $.extend(boundTask, {
                        Categories: self.Categories(),
                        Users: self.Users()
                    });

                    //Inizializzazione della datepicker per il giorno del task
                    $("#taskDate").datepicker({
                        showOn: "button",
                        buttonImage: "style-sheets/images/calendar_dark.png",
                        buttonImageOnly: true,
                        onSelect: function() {
                            boundTask.date = $("#taskDate").datepicker("getDate");
                        }
                    });
                    $("#taskDate").datepicker("setDate", boundTask.date);

                    //Inizializzazione del form di Validazione (per il titolo)
                    $("#edit-task-form").validate({
                        rules: {
                            title: "required"
                        },
                        messages: {
                            title: "Il titolo è obbligatorio"
                        },
                        errorPlacement: function(error, element) {
                        },
                        //Funzione comune in utility.js
                        invalidHandler: customInvalidHandler
                    });

                    //Applicazione del binding del task alla popup
                    ko.applyBindings(boundTask, $dialog[0]);
                },
                //Le actions sono esplicitamente richiamate dall'evento di click sui buttons
                buttons: [
                    {
                        text: "Aggiungi",
                        class: "button add-button",
                        click: function() {
                            self.actions.addDialog($dialog, boundTask);
                        }
                    },
                    {
                        text: "Salva",
                        class: "button save-button",
                        click: function() {
                            self.actions.save(task, boundTask, $dialog);
                        }
                    },
                    {
                        text: "Elimina",
                        class: "button delete-button",
                        click: function() {
                            self.actions.delete(boundTask, $dialog);
                        }
                    }
                ],
                close: function(event, ui) {
                    //In chiusura, è necessario distruggere gli oggetti jquery-ui costruiti,
                    //la dialog e la datepicker per liberare memoria e non tenere nodi "morti" 
                    //nel DOM. Dopodichè si effettua la pulizia del binding precedentemente effettuato 
                    //con knockout, per evitare che il binding successivo crei problemi
                    $dialog.dialog("destroy");
                    $("#taskDate").datepicker("destroy");
                    ko.cleanNode($dialog[0]);
                }
            });
        };
        
        /*
         * Inizializza le datepicker utilizzate nella pagine, eccetto quella della popup.
         * Ad ogni evento di selezione viene aggiornata la relativa property del viewmodel associato.
         * Questo è un esempio di binding manuale, poichè non è possibile bindare direttamente
         * una datepicker con knockout js
         */
        domUtils.initDatePickers = function() {
            $("#fastAddDate").datepicker({
                showOn: "button",
                buttonImage: "style-sheets/images/calendar_dark.png",
                buttonImageOnly: true,
                onSelect: function() {
                    //Aggiornamento manuale del viewModel
                    self.NewTask.date = $("#fastAddDate").datepicker("getDate");
                }
            });
            $("#fastAddDate").datepicker("setDate", self.NewTask.date);

            $("#startDate").datepicker({
                showOn: "button",
                buttonImage: "style-sheets/images/calendar_light.png",
                buttonImageOnly: true,
                maxDate: self.endDate,
                onSelect: function() {
                    //Aggiornamento manuale viewmodel e modifica minDate della datepicker
                    //di dataFine
                    self.startDate = $("#startDate").datepicker("getDate");
                    $("#endDate").datepicker("option", "minDate", self.startDate);
                }
            });
            $("#startDate").datepicker("setDate", self.startDate);


            $("#endDate").datepicker({
                showOn: "button",
                buttonImage: "style-sheets/images/calendar_light.png",
                buttonImageOnly: true,
                minDate: self.startDate,
                onSelect: function() {
                    //Aggiornamento manuale viewmodel e modifica maxDate della datepicker
                    //di dataInizio
                    self.endDate = $("#endDate").datepicker("getDate");
                    $("#startDate").datepicker("option", "maxDate", self.endDate);
                }
            });
            $("#endDate").datepicker("setDate", self.endDate);
        };

        /*
         * Funzione utilizzata per gestire l'evento di toggle sulla freccia dei filtri della timeline.
         * L'effetto è ottenuto con una doppia animazione, la variazione dell'opacità e 
         * la variazione dell'altezza
         * @param {type} element - la freccia cliccata
         */
        domUtils.toggleFilters = function(element) {
            var $arrow = $(element);
            var hide = $arrow.hasClass("arrow-up");
            if (hide) {
                //Chiusura
                $(".filters-content").animate({opacity: 0}, "fast", function() {
                    $arrow.removeClass("arrow-up").addClass("arrow-down");
                    $(".filters-content").animate({height: "toggle"}, "slow", function() {
                        domUtils.setDragBounds();
                    });
                });
            } else {
                //Apertura
                $(".filters-content").animate({height: "toggle"}, "slow", function() {
                    $arrow.removeClass("arrow-down").addClass("arrow-up");
                    $(".filters-content").animate({opacity: 1}, "fast", function() {
                        domUtils.setDragBounds();
                    });
                });
            }
        };

        /*
         * Funzione utilizzata per gestire l'evento di toggle sulla freccia della sidebar laterale.
         * L'effetto è ottenuto con una doppia animazione, la variazione dell'opacità e 
         * la variazione della larghezza
         * @param {type} element - la freccia cliccata
         */
        domUtils.toggleCategories = function(element) {
            var $arrow = $(element);
            var hide = $arrow.hasClass("arrow-left");
            if (hide) {
                $(".categories-content").animate({opacity: 0}, "fast", function() {
                    $arrow.removeClass("arrow-left").addClass("arrow-right");
                    $(".categories-content").animate({width: "toggle"}, "slow", function() {
                        domUtils.setDragBounds();
                    });
                });
            } else {
                $(".categories-content").animate({width: "toggle"}, "slow", function() {
                    $arrow.removeClass("arrow-right").addClass("arrow-left");
                    $(".categories-content").animate({opacity: 1}, "fast", function() {
                        domUtils.setDragBounds();
                    });
                });
            }
        };

        /*
         * Funzione richiamata ogni volta che varia la dimensione della timeline nella 
         * viewport con le animazioni scatenate dal click sulle freccine.
         * Reimposta i margini entro i quali può essere draggata la card di un utente
         */
        domUtils.setDragBounds = function() {
            $(".draggable").draggable("option", "containment", [$('.days-list').offset().left,
                $('.days-list').offset().top,
                $('.days-list').offset().left + $('.days-list').width() - 60,
                $('.days-list').offset().top + $('.days-list').height() - 60
            ]);
        };
        
        /*
         * Funzione che inizializza le aree in cui può essere fatto drop di una card
         * utente, ovvero il riquadro alla sinistra di un task nella timeline.
         * @param {type} element - il riquadro sul quale verrà applicata la funzione
         * @param {type} task - il task associato a quel riquadro
         */
        domUtils.initDroppable = function(element, task) {
            $(element).droppable({
                activeClass: "droppable-hover",
                hoverClass: "droppable-active",
                accept: ".draggable",
                drop: function(event, ui) {
                    task.AssignedUser(self.utils.getUserById($(ui.helper).data('id')));
                    self.services.edit.request(task, task);
                }
            });
        };

        /*
         * Funzione che inizializza la funzione di drag per tutti i riquadri utenti
         * Purtroppo, data la complessità della struttura della pagina, è stato necessario 
         * implementare la gestione custom dell'evento di drag per far scrollare correttamente 
         * il div timeline (orizzontalmente) e i div contenenti le liste di task per ogni 
         * singolo giorno (verticalmente). Questa gestione crea un notevole calo di performance su IE11
         */
        domUtils.initDraggables = function() {

            var horizontalTriggerZone = 80;
            var verticalTriggerZone = 40;
            var verticalScrollSpeed = 2;
            var horizontalScrollSpeed = 4;

            $(".draggable").draggable({
                scroll: false,
                helper: "clone",
                zIndex: 100,
                drag: function(event, ui) {
                    //Gestione custom dell'evento di scroll verticale quando il puntatore 
                    //è vicino ai margini superiore o inferiore
                    $(".task-list").each(function() {
                        var $this = $(this);
                        var cOffset = $this.offset();
                        var bottomPos = cOffset.top + $this.height();
                        clearInterval($this.data('timerScroll'));
                        $this.data('timerScroll', false);
                        if (event.pageX >= cOffset.left && event.pageX <= cOffset.left + $this.width())
                        {
                            if (event.pageY >= bottomPos - verticalTriggerZone && event.pageY <= bottomPos)
                            {
                                var moveUp = function() {
                                    $this.scrollTop($this.scrollTop() + verticalScrollSpeed);
                                };
                                $this.data('timerScroll', setInterval(moveUp, 10));
                                moveUp();
                            }
                            if (event.pageY >= cOffset.top && event.pageY <= cOffset.top + verticalTriggerZone)
                            {
                                var moveDown = function() {
                                    $this.scrollTop($this.scrollTop() - verticalScrollSpeed);
                                };
                                $this.data('timerScroll', setInterval(moveDown, 10));
                                moveDown();
                            }
                        }
                    });
                    
                    //Gestione custom dell'evento di scroll orizzontale quando il puntatore è 
                    //vicino ai margini sinistro o destro
                    $(".timeline").each(function() {
                        var $this = $(this);
                        var cOffset = $this.offset();
                        var rightPos = cOffset.left + $this.width();
                        clearInterval($this.data('timerScroll'));
                        $this.data('timerScroll', false);
                        if (event.pageY >= cOffset.top && event.pageY <= cOffset.top + $this.height())
                        {
                            if (event.pageX >= rightPos - horizontalTriggerZone && event.pageX <= rightPos)
                            {
                                var moveLeft = function() {
                                    $this.scrollLeft($this.scrollLeft() + horizontalScrollSpeed);
                                };
                                $this.data('timerScroll', setInterval(moveLeft, 10));
                                moveLeft();
                            }
                            if (event.pageX >= cOffset.left && event.pageX <= cOffset.left + horizontalTriggerZone)
                            {
                                var moveRight = function() {
                                    $this.scrollLeft($this.scrollLeft() - horizontalScrollSpeed);
                                };
                                $this.data('timerScroll', setInterval(moveRight, 10));
                                moveRight();
                            }
                        }
                    });
                },
                stop: function() {
                    //Vengono resettati tutti i timer per effettuare lo scroll custom                    
                    $(".task-list").each(function() {
                        clearInterval($(this).data('timerScroll'));
                        $(this).data('timerScroll', false);
                    });
                    $(".timeline").each(function() {
                        clearInterval($(this).data('timerScroll'));
                        $(this).data('timerScroll', false);
                    });
                },
                refreshPositions: true
            });
            
            //Vengono aggiornati i margini di drag
            domUtils.setDragBounds();
        };

        /*
         * Questa funzione è richiamata nel momento in cui si vuole mostrare una notifica
         * COMET arrivata dal server. Sulla base del tipo di notifica e del task, costruisce
         * un box posizionato in modo assoluto all centro della viewport (in alto)
         */
        domUtils.showNotification = function(task, type) {
            var notification = new self.Notification();
            notification.title("!");
            
            if (type === notificationType.addedTask) {
                notification.description("Task " + task.title + " aggiunto");
                notification.task = task;
            } else if (type === notificationType.editedTask) {
                notification.description("Task " + task.title + " modificato");
                
                //Se il task, dopo la modifica ricevuta è ancora accessibile,
                //lo aggiungo all'oggetto notification, di modo che l'utente possa
                //aprirlo cliccando sopra alla notifica
                if(self.utils.getTaskById(task.id).accessible()){
                    notification.task = task;
                }
            } else if (type === notificationType.deletedTask) {
                notification.description("Task " + task.title + " eliminato");
            }

            //Viene creato un div via jquery
            var $div = $("<div>", {class: "fade-in-box notification-box", "data-bind": notification.task !== null ? "click : show" : "style: { 'cursor' : 'default'}"});
            $div.append($("<h2>", {"data-bind": " text : title"}));
            $div.append($("<span>", {"data-bind": " text : description"}));
            $("body").append($div);
            //Appeso al dom, e lanciata l'animazione che dura 2 secondi.            
            $div.fadeIn("slow", function() {
                setTimeout(function() {
                    $div.fadeOut("slow", function() {
                        ko.cleanNode($div[0]);
                        $div.remove();
                    });
                }, 10000);
            });
            //Si effettua il binding della notifica al box
            ko.applyBindings(notification, $div[0]);
        };

        /*
         * Funzione per l'inizializzazione del form di validazione della barra
         * di aggiunta veloce. Viene controllato solo il titolo
         */
        domUtils.initValidation = function() {
            $("#add-bar-form").validate({
                rules: {
                    fastTitle: "required"
                },
                messages: {
                    fastTitle: "Il titolo è obbligatorio"
                },
                errorPlacement: function(error, element) {
                },
                invalidHandler: customInvalidHandler
            });

        };

        /*
         * Se si preme invio nella barra di aggiunta rapida,
         * mentre si sta scrivendo dentro alla input del titolo
         * viene richiamata la action di add task
         */
        domUtils.initAddOnEnterKey = function() {
            $("input[name='fastTitle']").bind('keypress', function(event) {
                if (event.keyCode === 13) {
                    self.actions.addFast();
                    return false;
                }
            });
        };

    };
    
    /*
     * Oggetto che contiene tutte le funzioni di utility utilizzate nell'applicazione
     */
    self.utils = new function() {
        var utils = this;

        /*
         * Inizializzazione delle date di filtro per la ricerca
         */
        utils.initDates = function() {
            self.startDate = getMidnightDate(new Date(self.startDate.setDate(self.startDate.getDate() - 1)));
            self.endDate = getMidnightDate(new Date(self.endDate.setDate(self.endDate.getDate() + 10)));
        };

        /*
         * Ritorna l'utente che corrisponde ad un determinato id
         */
        utils.getUserById = function(id) {
            for (var i = 0; i < self.Users().length; i++) {
                if (self.Users()[i].id() === id) {
                    return self.Users()[i];
                }
            }
            return null;
        };
        
        /*
         * Ritorna la categoria che corrisponde ad un determinato id
         */
        utils.getCategoryById = function(id) {
            for (var i = 0; i < self.Categories().length; i++) {
                if (self.Categories()[i].id() === id) {
                    return self.Categories()[i];
                }
            }
            return null;
        };
        
        /*
         * Aggiorna i campi dell'oggetto di viewModel NewTask,
         * azzaerandoli dopo l'aggiunta di un nuovo task.
         */
        utils.resetNewTask = function() {
            self.NewTask.title("");
            self.NewTask.description("");
            self.NewTask.date = getMidnightDate(new Date());
            self.NewTask.done(false);
            self.NewTask.personal(false);
            self.NewTask.latitude(0);
            self.NewTask.longitude(0);
            self.NewTask.AssignedUser();
            self.NewTask.Category();
            self.NewTask.attachment("");
        };
        
        /*
         * Ritorna il task che corrisponde ad un determinato id (se presente)
         */
        utils.getTaskById = function(id) {
            for (var i = 0; i < self.Days().length; i++) {
                for (var j = 0; j < self.Days()[i].Tasks().length; j++) {
                    if (id === self.Days()[i].Tasks()[j].id()) {
                        return self.Days()[i].Tasks()[j];
                    }
                }
            }
            return null;
        };
        
        /*
         * Rimuove il task dall'array di task nell' oggetto Day in cui era inserito
         */
        utils.removeTask = function(task) {
            var removed = false;
            for (var i = 0; i < self.Days().length; i++) {
                for (var j = 0; j < self.Days()[i].Tasks().length; j++) {
                    //var task = self.Days()[i].tasks()[j];
                    if (task.id === self.Days()[i].Tasks()[j].id()) {
                        return self.Days()[i].Tasks.splice(j, 1)[0];
                    }
                }
            }
        };
        
        /*
         * Cerca il giorno in cui inserire il task ed effettua il push nell'array
         * di task per quel determinato giorno
         */
        utils.pushTask = function(task) {
            var pushed = false;
            var taskToPush;
            for (var i = 0; i < self.Days().length && !pushed; i++) {
                if (compareDate(new Date(task.date), self.Days()[i].day)) {
                    taskToPush = new self.Task({
                        id: task.id,
                        title: task.title,
                        description: task.description,
                        date: task.date,
                        done: task.done,
                        personal: task.personal,
                        userId: task.userId,
                        assignedUserId: task.assignedUserId,
                        categoryId: task.categoryId,
                        latitude: task.latitude,
                        longitude: task.longitude,
                        attachment: task.attachment,
                        timeStamp: task.timeStamp
                    });
                    self.Days()[i].Tasks.push(taskToPush);
                    pushed = true;
                }
            }
            
            if(taskToPush){
                //Ogni volta che viene pushato un task, si crea la relativa area di drop
                self.domUtils.initDroppable($(".task[data-id='" + taskToPush.id() + "'] .user"), taskToPush);
            }
            return taskToPush;
        };
        
        /*
         * Restituisce la stringa di un determinato giorno secondo il formato 
         * italiano impostato sulla datepicker
         */
        utils.getDayHeader = function(day) {
            return decodeHtmlEntity($.datepicker.formatDate("DD d MM", day.day));
        };

        /*
         * Clona il task passato come parametro e lo ritorna in uscita
         */
        utils.cloneTask = function(task) {
            return new self.Task({
                id: task.id ? task.id() : -1,
                title: task.title(),
                description: task.description(),
                date: task.date,
                done: task.done(),
                personal: task.personal(),
                userId: task.userId ? task.userId() : loggedUser.id,
                assignedUserId: task.AssignedUser() ? task.AssignedUser().id() : -1,
                categoryId: task.Category() ? task.Category().id() : 1,
                latitude: task.latitude(),
                longitude: task.longitude(),
                attachment: task.attachment(),
                timeStamp: task.timeStamp ? task.timeStamp : -1
            });
        };
    };
}

/*
 * funzione eseguita al caricamento della pagina
 * Viene inizializzato il viewModel e vengono richiamate le funzioni di inizializzazione della pagina
 */
$(document).ready(function() {
    tasksViewModel = new TasksViewModelDefinition();
    
    /*
     * $.when prende in input due oggetti xhr ritornati dalle $.ajax e aspetta 
     * che siano entrambe completate, prima di eseguire la callback di done.
     * In questo caso, aspettiamo di ricevere dal server le categorie e gli utenti, dopodichè
     * facciamo la prima request di search, per avere i task da inserire nella timeline
     */
    $.when(tasksViewModel.services.getCategories.request(), tasksViewModel.services.getUsers.request()).done(function() {
        ko.applyBindings(tasksViewModel, $(".container")[0]);
        
        //Setting dei default regional settings per la datepicker
        $.datepicker.setDefaults($.datepicker.regional["it"]);

        //Vengono richiamate tutte le inizializzazioni per il corretto 
        //funzionamento dei componenti nella pagina
        tasksViewModel.utils.initDates();
        tasksViewModel.domUtils.initDraggables();
        tasksViewModel.domUtils.initDatePickers();
        tasksViewModel.domUtils.initValidation();
        tasksViewModel.domUtils.initAddOnEnterKey();
        tasksViewModel.services.search.request();
    });
});

$(window).resize(function() {
    //Aggiorno i bounds dell'area in cui posso draggare
    tasksViewModel.domUtils.setDragBounds();
});