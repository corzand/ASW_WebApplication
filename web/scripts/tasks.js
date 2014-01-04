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
        task.timeStamp = params.timeStamp;

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
                rSettings.successCallback = self.services.polling.callback;
                return sendRequest(rSettings);
            },
            "callback": function(data, requestData) {
                self.services.polling.request(requestData);
                if (!data.error) {
                    switch (data.operation) {
                        case 0:
                            //Add
                            alert("Task aggiunto");
                            self.utils.pushTask(data.task);
                            break;
                        case 1:
                            //Edit
                            alert("Task modificato");
                            var task = self.utils.getTaskById(data.task.id);

                            if (task.date.getTime() !== new Date(data.task.date).getTime()) {
                                self.utils.removeTask(data.task);
                                //if (self.startDate <= new Date(data.task.date) <= self.endDate) {
                                self.utils.pushTask(data.task);
                                //}
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
                            break;
                        case 2:
                            //Delete
                            alert("Task eliminato");
                            self.utils.removeTask(data.task);
                            break;
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
                    userId: loggedUser.id
                };
            },
            "callback": function(data, pollingRequestData) {
                if (!data.error) {

                    self.services.polling.request(pollingRequestData);

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

                    for (i = 0; i < data.tasks.length; i++) {
                        self.utils.pushTask(data.tasks[i]);
                    }
                }
            }
        },
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
                    self.utils.pushTask(data.task);
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
                if (data.task) {
                    if (task.date.getTime() !== new Date(data.task.date).getTime()) {
                        self.utils.removeTask(data.task);
                        if (self.startDate <= new Date(data.task.date) <= self.endDate) {
                            self.utils.pushTask(data.task);
                        }
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
                }

                if (data.error) {
                    alert(data.errorMessage);
                }

                if (params.$dialog) {
                    params.$dialog.dialog("close");
                }
            }
        },
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
                return {
                    id: task.id(),
                    timeStamp: task.timeStamp,
                    userId: loggedUser.id
                };
            },
            callback: function(data, $dialog) {
                if (!data.error) {
                    self.utils.removeTask(data.task);
                    if ($dialog) {
                        $dialog.dialog("close");
                    }
                } else {
                    alert(data.errorMessage);
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
            if (self.utils.validate(self.NewTask)) {
                self.services.add.request(null, self.NewTask);
            }
        };

        actions.addDialog = function($dialog, task) {
            if (self.utils.validate(task)) {
                self.services.add.request($dialog, task);
            }

        };

        actions.save = function(task, boundTask, $dialog) {
            if (self.utils.validate(boundTask)) {
                self.services.edit.request(task, boundTask, $dialog);
            }
        };

        actions.search = function() {
            if (self.utils.checkDates()) {
                self.services.search.request();
            }
            else {
                 alert("La data di fine ricerca deve essere maggiore della data di inizio ricerca!");
            }
        };

        actions.markTask = function(taskToMark) {
            self.services.edit.request(taskToMark, taskToMark);
            return true;
        };

        actions.delete = function(taskToDelete, $dialog) {
            self.services.delete.request(taskToDelete, $dialog);
        };
    };
    self.domUtils = new function() {
        var domUtils = this;
        domUtils.openDialog = function(task) {
            var $dialog = $("#edit-task-popup");
            var boundTask;
            $dialog.dialog({
                autoOpen: true,
                height: 300,
                width: 350,
                modal: true,
                open: function(event, ui) {
                    if (task.id) {
                        //Hide add
                        $dialog.parent().find(".ui-dialog-buttonpane .addButton").hide();
                    } else {
                        //Hide edit e delete button
                        $dialog.parent().find(".ui-dialog-buttonpane .saveButton").hide();
                        $dialog.parent().find(".ui-dialog-buttonpane .deleteButton").hide();
                    }

                    boundTask = new self.Task({
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
                    $.extend(boundTask, {
                        Categories: self.Categories(),
                        Users: self.Users()
                    });

                    $("#taskDate").datepicker({
                        showOn: "button",
                        buttonImage: "/style/images/calendar.png",
                        buttonImageOnly: true,
                        onSelect: function() {
                            boundTask.date = $("#taskDate").datepicker("getDate");
                        }
                    });
                    $("#taskDate").datepicker("setDate", boundTask.date);
                    ko.applyBindings(boundTask, $dialog[0]);
                },
                buttons: [
                    {
                        text: "Aggiungi",
                        class: "addButton",
                        click: function() {
                            self.actions.addDialog($dialog, boundTask);
                        }
                    },
                    {
                        text: "Salva",
                        class: "saveButton",
                        click: function() {
                            self.actions.save(task, boundTask, $dialog);
                        }
                    },
                    {
                        text: "Elimina",
                        class: "deleteButton",
                        click: function() {
                            self.actions.delete(boundTask, $dialog);
                        }
                    }
                ],
                close: function(event, ui) {
                    $dialog.dialog("destroy");
                    $("#taskDate").datepicker("destroy");
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
        
        utils.checkDates = function(){
            if (self.startDate > self.endDate) {
                return false;             
            }
            else {
                return true;
            }
        };
        
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
        utils.validate = function(task) {
            if (task.title() !== "") {
                return true;
            } else {
                alert("Il nuovo task deve avere un titolo!");
            }
        };
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
        utils.removeTask = function(task) {
            var removed = false;
            for (var i = 0; i < self.Days().length && !removed; i++) {
                for (var j = 0; j < self.Days()[i].Tasks().length && !removed; j++) {
                    //var task = self.Days()[i].tasks()[j];
                    if (task.id === self.Days()[i].Tasks()[j].id()) {
                        self.Days()[i].Tasks.splice(j, 1);
                        removed = true;
                    }
                }
            }
        };
        utils.pushTask = function(task) {
            var pushed = false;
            for (var i = 0; i < self.Days().length && !pushed; i++) {
                if (compareDate(new Date(task.date), self.Days()[i].day)) {
                    self.Days()[i].Tasks.push(
                            new self.Task({
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
                            }));
                    pushed = true;
                }
            }
        };
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



