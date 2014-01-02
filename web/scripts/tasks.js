/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function TasksViewModelDefinition() {

    var self = this;
    //Task object Constructor
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

        task.visible = ko.computed(function() {
            var categoryVisible = false;
            $.each(self.Categories(), function(index, category) {
                if (category.id() === task.Category().id()) {
                    categoryVisible = category.state();
                }
            });
            if (categoryVisible) {
                if (self.personal()) {
                    return task.userId() === loggedUser.id
                            || (task.AssignedUser() && task.AssignedUser().id() === loggedUser.id);
                } else {
                    return task.userId() === loggedUser.id
                            || (task.AssignedUser() && task.AssignedUser().id() === loggedUser.id)
                            || !task.personal();
                }
            } else {
                return false;
            }
        });
        task.assigned = ko.computed(function() {
            return task.AssignedUser();
        });
    };

    self.personal = ko.observable(true);
    self.Categories = ko.observableArray([]);
    self.Users = ko.observableArray([]);
    self.Days = ko.observableArray([]);
    self.startDate = new Date();
    self.endDate = new Date();
    self.NewTask = new function() {
        var newTask = this;
        newTask.title = ko.observable("");
        newTask.description = ko.observable("");
        newTask.date = getMidnightDate(new Date());
        newTask.done = ko.observable(false);
        newTask.personal = ko.observable(true);
        newTask.latitude = ko.observable(0);
        newTask.longitude = ko.observable(0);
        newTask.AssignedUser = ko.observable();
        newTask.Category = ko.observable();
        newTask.attachment = ko.observable("");
    };
    self.services = {
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
                for (i = 0; i < data.users.length; i++) {
                    self.Users.push({
                        id: ko.observable(data.users[i].id),
                        username: ko.observable(data.users[i].username),
                        picture: ko.observable(data.users[i].picture)
                    });
                }
            }
        },
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
                        id: ko.observable(data.categories[i].id),
                        title: ko.observable(data.categories[i].title),
                        state: ko.observable(data.categories[i].state),
                        color: ko.observable(data.categories[i].color)
                    });
                }
            }
        },
        "polling": {
            "request": function(requestData) {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/polling/';
                rSettings.requestData = requestData;
                rSettings.callbackParameter = requestData;
                rSettings.successCallback = self.services.search.callback;
                return sendRequest(rSettings);
            },
            "callback": function(data, requestData) {
                self.services.polling.request(requestData);
                if (!data.error) {
                    if (data.isNew) {
                        //add
                    } else {
                        //edit
                    }
                }
            }
        },
        "search": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/search/';
                rSettings.requestData = JSON.stringify(self.services.search.requestData());
                rSettings.successCallback = self.services.search.callback;
                rSettings.callbackParameter = rSettings.requestData;
                return sendRequest(rSettings);
            },
            "requestData": function() {
                return {
                    startDate: self.startDate,
                    endDate: self.endDate,
                    userId: loggedUser.id,
                    //categories: self.utils.getActiveCategories(),
                    //personal: self.personal()
                };
            },
            "callback": function(data, pollingRequestData) {
                if (!data.error) {

                    //self.services.polling.request(pollingRequestData);

                    self.Days.removeAll();
                    var currentDay = new Date(self.startDate);
                    var range = dateDiff(self.startDate, self.endDate);
                    for (var i = 0; i < range; i++) {
                        self.Days.push({
                            day: new Date(currentDay),
                            Tasks: ko.observableArray([])
                        });
                        currentDay.setDate(currentDay.getDate() + 1);
                    }

                    for (i = 0; i < data.tasks.length; i++) {
                        var pushed = false;
                        for (var j = 0; j < self.Days().length && !pushed; j++) {
                            if (compareDate(new Date(data.tasks[i].date), self.Days()[j].day)) {
                                self.Days()[j].Tasks.push(
                                        new self.Task({
                                            id: data.tasks[i].id,
                                            title: data.tasks[i].title,
                                            description: data.tasks[i].description,
                                            date: data.tasks[i].date,
                                            done: data.tasks[i].done,
                                            personal: data.tasks[i].personal,
                                            userId: data.tasks[i].userId,
                                            assignedUserId: data.tasks[i].assignedUserId,
                                            categoryId: data.tasks[i].categoryId,
                                            latitude: data.tasks[i].latitude,
                                            longitude: data.tasks[i].longitude,
                                            attachment: data.tasks[i].attachment
                                        }));
                                pushed = true;
                            }
                        }
                    }
                }
            }
        },
        "add": {
            "request": function($dialog) {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/add/';
                rSettings.requestData = JSON.stringify(self.services.add.requestData());
                rSettings.successCallback = self.services.add.callback;
                if ($dialog) {
                    rSettings.callbackParameter = $dialog;
                }
                return sendRequest(rSettings);
            },
            "requestData": function() {
                return {
                    title: self.NewTask.title(),
                    description: self.NewTask.description(),
                    date: self.NewTask.date,
                    done: self.NewTask.done(),
                    personal: self.NewTask.personal(),
                    userId: loggedUser.id,
                    assignedUserId: self.NewTask.AssignedUser() ? self.NewTask.AssignedUser().id() : -1,
                    latitude: self.NewTask.latitude(),
                    longitude: self.NewTask.longitude(),
                    categoryId: self.NewTask.Category().id(),
                    attachment: self.NewTask.attachment()
                };
            },
            "callback": function(data, $dialog) {
                if (!data.error) {
                    var pushed = false;
                    for (var i = 0; i < self.Days().length && !pushed; i++) {
                        if (compareDate(new Date(data.task.date), self.Days()[i].day)) {
                            self.Days()[i].Tasks.push(
                                    new self.Task({
                                        id: data.task.id,
                                        title: data.task.title,
                                        description: data.task.description,
                                        date: data.task.date,
                                        done: data.task.done,
                                        personal: data.task.personal,
                                        userId: data.task.userId,
                                        assignedUserId: data.task.assignedUserId,
                                        categoryId: data.task.categoryId,
                                        latitude: data.task.latitude,
                                        longitude: data.task.longitude,
                                        attachment: data.task.attachment
                                    }));
                            pushed = true;
                        }
                    }

                    if ($dialog) {
                        $dialog.dialog("close");
                    }

                    self.utils.resetNewTask();
                } else {
                    alert(data.errorMessage);
                }
            }
        },
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
                    categoryId: task.Category().id(),
                    attachment: task.attachment()
                };
            },
            "callback": function(data, params) {
                task = params.data;
                if (!data.error) {
                    task.title(data.task.title);
                    task.description(data.tasks.description);
                    task.date = new Date(data.task.date);
                    task.done(data.task.done);
                    task.personal(data.task.personal);
                    task.userId(data.task.userId);
                    task.AssignedUser(self.utils.getUserById(data.task.assignedUserId));
                    task.latitude(data.task.latitude);
                    task.longitude(data.task.longitude);
                    task.Category(self.utils.getCategoryById(data.task.categoryId));
                    task.attachment(data.task.attachment);

                    //TODO: Gestire task con data modificata! (spostarlo)
                    //se la data nuova è diversa dalla data vecchia del task, lo tolgo dall'array del giorno
                    //lo metto nell'array del nuovo giorno
                    if (params.$dialog) {
                        $dialog.dialog("close");
                    }
                } else {
                    alert(data.errorMessage);
                }
            }
        },
        "delete" : {
            request : function(taskId){
                
            },
            requestData : function(){
                
            },
            callback : function(data){
                if(!data.error){
                    
                }
            }
        }
    };
    self.actions = new function() {
        var actions = this;
        actions.edit = function(taskToEdit) {
            self.domUtils.openDialog(taskToEdit);
        };
        actions.addFast = function() {
            if(self.utils.validateAdd()){
                self.services.add.request();
            }
            else {
                alert("Il nuovo task deve avere un titolo!");
            }
        };
        actions.addDialog = function($dialog){
          if(self.utils.validateAdd()){
              self.services.add.request($dialog);
          }
          else {
              alert("Il nuovo task deve avere un titolo!");
          }
        };
        actions.search = function() {
            self.services.search.request();
        };
        actions.markTask = function(taskToMark) {
            self.services.edit.request(taskToMark);
        };
    };
    self.domUtils = new function() {
        var domUtils = this;
        domUtils.openDialog = function(task) {
            var $dialog = $("#edit-task-popup");
            $dialog.dialog({
                autoOpen: true,
                height: 300,
                width: 350,
                modal: true,
                open: function(event, ui) {
                    if (task.id) {
                        //Hide add
                        $dialog.parent().find(".ui-dialog-buttonpane .addButton").hide();
                    }else {
                        //Hide edit e delete button
                        $dialog.parent().find(".ui-dialog-buttonpane .editButton").hide();
                        $dialog.parent().find(".ui-dialog-buttonpane .deleteButton").hide();
                    }
                    var boundTask = $.extend(true, {}, task);
                    $.extend(boundTask, {
                        Categories: self.Categories(),
                        Users: self.Users()
                    });
                    ko.applyBindings(boundTask, $dialog[0]);
                },
                buttons: [
                    {
                        text: "Aggiungi",
                        class : "addButton",
                        click: function() {
                            self.actions.addDialog($dialog);
                        }
                    },
                    {
                        text: "Modifica",
                        class : "editButton",
                        click: function() {
                            self.services.edit.request(task, boundTask, $dialog);
                        }
                    }, 
                    {
                        text: "Elimina",
                        class : "deleteButton",
                        click: function() {
                            self.services.delete.request(task.id());
                        }
                    }
                ],
                close: function(event, ui) {
                    $dialog.dialog("destroy");
                    ko.cleanNode($dialog[0]);
                }
            });
        };
        domUtils.initDatePickers = function() {
            $("#fastAddDate").datepicker({
                showOn: "button",
                buttonImage: "/style/images/calendar.png",
                buttonImageOnly: true,
                onSelect: function() {
                    self.NewTask.date = $("#fastAddDate").datepicker("getDate");
                    $("#taskDate").datepicker("setDate", self.NewTask.date);
                }
            });
            $("#fastAddDate").datepicker("setDate", self.NewTask.date);
            $("#taskDate").datepicker({
                showOn: "button",
                buttonImage: "/style/images/calendar.png",
                buttonImageOnly: true,
                onSelect: function() {
                    self.NewTask.date = $("#taskDate").datepicker("getDate");
                    $("#fastAddDate").datepicker("setDate", self.NewTask.date);
                }
            });
            $("#taskDate").datepicker("setDate", self.NewTask.date);

            $("#startDate").datepicker({
                showOn: "button",
                buttonImage: "/style/images/calendar.png",
                buttonImageOnly: true,
                onSelect: function() {
                    self.startDate = $("#startDate").datepicker("getDate");
                }
            });
            $("#startDate").datepicker("setDate", self.startDate);
            $("#endDate").datepicker({
                showOn: "button",
                buttonImage: "/style/images/calendar.png",
                buttonImageOnly: true,
                onSelect: function() {
                    self.endDate = $("#endDate").datepicker("getDate");
                }
            });
            $("#endDate").datepicker("setDate", self.endDate);
        };
    };
    self.utils = new function() {
        var utils = this;
        utils.initDates = function() {
            self.startDate = getMidnightDate(new Date(self.startDate.setDate(self.startDate.getDate() - 1)));
            self.endDate = getMidnightDate(new Date(self.endDate.setDate(self.endDate.getDate() + 10)));
        };
//        utils.getActiveCategories = function() {
//            var activeCategories = [];
//            for (var i = 0; i < self.Categories().length; i++) {
//                if (self.Categories()[i].state() === true) {
//                    activeCategories.push(ko.toJS(self.Categories()[i])); //bisogna passare un oggetto che non abbia observables?
//                }
//            }
//            return activeCategories;
//        };
        utils.getUserById = function(id) {
            for (var i = 0; i < self.Users().length; i++) {
                if (self.Users()[i].id() === id) {
                    return self.Users()[i];
                }
            }
            return null;
        };
        utils.getCategoryById = function(id) {
            for (var i = 0; i < self.Categories().length; i++) {
                if (self.Categories()[i].id() === id) {
                    return self.Categories()[i];
                }
            }
            return null;
        };

        utils.resetNewTask = function() {
            self.NewTask.title("");
            self.NewTask.description("");
            self.NewTask.date = getMidnightDate(new Date());
            self.NewTask.done(false);
            self.NewTask.personal(true);
            self.NewTask.latitude(0);
            self.NewTask.longitude(0);
            self.NewTask.AssignedUser();
            self.NewTask.Category();
            self.NewTask.attachment("");
        };
        
        utils.validateAdd = function() {
            if (self.NewTask.title() !== "") return true;
            else false;
        };

//        utils.isTaskAssigned = function(task) {
//            return task.AssignedUser() !== null;
//        };

//        utils.isTaskVisible = function(task) {
//            if (utils.isActiveCategory(task.Category().id())) {
//                if (self.personal()) {
//                    return task.userId() === loggedUser.id
//                            || task.AssignedUser().id() === loggedUser.id;
//                } else {
//                    return task.userId() === loggedUser.id
//                            || task.AssignedUser().id() === loggedUser.id
//                            || !task.personal();
//                }
//            } else {
//                return false;
//            }
//        };
//        utils.isActiveCategory = function(id) {
//            for (var i = 0; i < self.Categories().length; i++) {
//                if (id === self.Categories()[i].id()) {
//                    return self.Categories()[i].state();
//                }
//            }
//        };
    };
}


$(document).ready(function() {
//init view model and stuff
    var tasksViewModel = new TasksViewModelDefinition();
    $.when(tasksViewModel.services.getCategories.request(), tasksViewModel.services.getUsers.request()).done(function() {
        ko.applyBindings(tasksViewModel, $(".container")[0]);
        tasksViewModel.utils.initDates();
        tasksViewModel.domUtils.initDatePickers();
        tasksViewModel.services.search.request();
    });
});



