/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function TasksViewModelDefinition() {
    var self = this;

    self.personal = ko.observable(true);
    self.Categories = ko.observableArray([]);
    self.Users = ko.observableArray([]);
    self.Days = ko.observableArray([]);
    self.startDate = new Date();//settare a ieri
    self.endDate = new Date();

    self.NewTask = new function() {
        var newTask = this;
        newTask.id = ko.observable(-1);
        newTask.title = ko.observable("");
        newTask.description = ko.observable("");
        newTask.date = new Date();
        newTask.done = ko.observable(false);
        newTask.personal = ko.observable(true);
        newTask.userId = ko.observable(loggedUser.id);
        newTask.assignedUserId = ko.observable(-1);
        newTask.latitude = ko.observable("");
        newTask.longitude = ko.observable("");
        newTask.categoryId = ko.observable(-1);
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
        "search": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/search/';
                rSettings.requestData = JSON.stringify(self.services.search.requestData());
                rSettings.successCallback = self.services.search.callback;
                return sendRequest(rSettings);
            },
            "requestData": function() {
                return {
                    startDate: self.startDate,
                    endDate: self.endDate,
                    userId: loggedUser.id,
                    categories: self.utils.getActiveCategories(),
                    personal: self.personal()
                };
            },
            "callback": function(data) {

                self.Days.removeAll();

                var currentDay = self.startDate;
                var range = dateDiff(self.startDate, self.endDate);

                for (var i = 0; i < range; i++) {
                    self.Days().push({
                        day: currentDay,
                        tasks: ko.observableArray([])
                    });
                    currentDay.setDate(currentDay.getDate() + 1);
                }

                for (i = 0; i < data.tasks.length; i++) {
                    var pushed = false;
                    for (var j = 0; j < self.Days().length && !pushed; j++) {
                        if (compareDate(data.tasks[i].date, self.Days()[j].day)) {
                            self.Days()[j].tasks.push({
                                id: ko.observable(data.tasks[i].id),
                                title: ko.observable(data.tasks[i].title),
                                description: ko.observable(data.tasks[i].description),
                                date: ko.observable(data.tasks[i].date),
                                done: ko.observable(data.tasks[i].done),
                                personal: ko.observable(data.tasks[i].personal),
                                userId: ko.observable(data.tasks[i].userId),
                                assignedUserId: ko.observable(data.tasks[i].assignedUserId),
                                latitude: ko.observable(data.tasks[i].latitude),
                                longitude: ko.observable(data.tasks[i].longitude),
                                categoryId: ko.observable(data.tasks[i].categoryId),
                                attachment: ko.observable(data.tasks[i].attachment)
                            });
                            pushed = true;
                        }
                    }
                }
            }
        },
        "add": {
            "request": function() {
                var rSettings = new requestSettings();
                rSettings.url = '/tasks/add/';
                rSettings.requestData = JSON.stringify(self.services.add.requestData());
                rSettings.successCallback = self.services.add.callback;
                return sendRequest(rSettings);
            },
            "requestData": function() {
                return {
                    title: self.NewTask.title(),
                    description:  self.NewTask.description(),
                    date:  self.NewTask.date,
                    done:  self.NewTask.done(),
                    personal:  self.NewTask.personal(),
                    userId: loggedUser.id,
                    assignedUserId:  self.NewTask.assignedUserId(),
                    latitude: self.NewTask.latitude(),
                    longitude: self.NewTask.longitude(),
                    categoryId: self.NewTask.categoryId(),
                    attachment: self.NewTask.attachment()
                };
            },
            "callback": function(data) {

            }
        }
    };

    self.actions = new function() {
        var actions = this;
        actions.edit = function(taskToEdit) {
            self.domUtils.openDialog(taskToEdit);
        };
        actions.add = function(taskToAdd) {
            self.services.add.request();
        };
        actions.search = function() {
            self.services.search.request();
        };
    };

    self.domUtils = new function() {
        var domUtils = this;

        domUtils.openDialog = function(task) {
            $("#edit-task-popup").dialog({
                autoOpen: true,
                height: 300,
                width: 350,
                modal: true,
                open: function(event, ui) {
                    ko.applyBindings(task, $("#edit-task-popup")[0]);
                },
                buttons: {
                    "Add": function() {
                        self.services.add.request();
                    }
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
                    self.NewTask.date = $("#startDate").datepicker("getDate");
                }
            });

            $("#endDate").datepicker({
                showOn: "button",
                buttonImage: "/style/images/calendar.png",
                buttonImageOnly: true,
                onSelect: function() {
                    self.NewTask.date = $("#endDate").datepicker("getDate");
                }
            });
        };
    };

    self.utils = new function() {
        var utils = this;
        utils.initDates = function() {
            self.NewTask.date = getMidnightDate(new Date(self.NewTask.date));
            self.startDate = getMidnightDate(new Date(self.startDate.setDate(self.startDate.getDate() - 1)));
            self.endDate = getMidnightDate(new Date(self.endDate.setDate(self.endDate.getDate() + 10)));
        };
        utils.getActiveCategories = function() {
            var activeCategories = [];

            for (var i = 0; i < self.Categories.length; i++) {
                if (self.Categories()[i].state() === true) {
                    activeCategories.push(self.Categories()[i]);//bisogna passare un oggetto che non abbia observables?
                }
            }
            return activeCategories;
        };
    };
}


$(document).ready(function() {
    //init view model and stuff
    var tasksViewModel = new TasksViewModelDefinition();

    $.when(tasksViewModel.services.getCategories.request(), tasksViewModel.services.getUsers.request()).done(function() {
        ko.applyBindings(tasksViewModel, $(".container")[0]);

        tasksViewModel.domUtils.initDatePickers();

        tasksViewModel.services.search.request();
    });

});



