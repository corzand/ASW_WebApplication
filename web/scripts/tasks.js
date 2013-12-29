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

    self.CurrentTask = new function() {
        var currentTask = this;
        currentTask.id = ko.observable();
        currentTask.title = ko.observable();
        currentTask.description = ko.observable();
        currentTask.date = new Date();
        currentTask.done = ko.observable();
        currentTask.personal = ko.observable();
        currentTask.userId = ko.observable();
        currentTask.assignedUserId = ko.observable();
        currentTask.latitude = ko.observable();
        currentTask.longitude = ko.observable();
        currentTask.categoryId = ko.observable();
        currentTask.attachment = ko.observable();
    };
    
    self.Days = ko.observableArray([]);
    
    self.startDate = new Date();//settare a ieri
    self.endDate = new Date();

    self.getActiveCategories = function() {
        var activeCategories = [];

        for (var i = 0; i < Categories.length; i++) {
            if (Categories()[i].state() === true) {
                activeCategories.push(Categories()[i]);//bisogna passare un oggetto che non abbia observables?
            }
        }
        return activeCategories;
    };

    self.searchData = function() {
        return {
            startDate: self.startDate,
            endDate: self.endDate,
            userId: loggedUser.id,
            categories: self.getActiveCategories(),
            personal: self.personal()
        };
    };

    self.searchCallback = function(data) {

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

    };

    self.search = function() {
        var rSettings = new requestSettings();
        rSettings.url = '/tasks/search/';
        rSettings.requestData = JSON.stringify(self.searchData());
        rSettings.successCallback = self.searchCallback;
        sendRequest(rSettings);
    };

    self.getCategoriesCallback = function(data) {
        for (var i = 0; i < data.categories.length; i++) {
            self.Categories.push({
                id: ko.observable(data.categories[i].id),
                title: ko.observable(data.categories[i].title),
                state: ko.observable(data.categories[i].state),
                color: ko.observable(data.categories[i].color)
            });
        }
    };

    self.getCategories = function() {
        var rSettings = new requestSettings();
        rSettings.url = '/tasks/categories/';
        rSettings.successCallback = self.getCategoriesCallback;
        return sendRequest(rSettings);
    };

    self.getUsersCallback = function(data) {
        for (i = 0; i < data.users.length; i++) {
            self.Users.push({
                id: ko.observable(data.users[i].id),
                username: ko.observable(data.users[i].username),
                picture: ko.observable(data.users[i].picture)
            });
        }
    };

    self.getUsers = function() {
        var rSettings = new requestSettings();
        rSettings.url = '/users/users/';
        rSettings.successCallback = self.getUsersCallback;
        return sendRequest(rSettings);
    };

    self.startDate.setDate(self.startDate.getDate() - 1);
    self.endDate.setDate(self.endDate.getDate() + 10);
}


$(document).ready(function() {
    //init view model and stuff
    var taskViewModel = new TaskViewModelDefinition();

    $.when(taskViewModel.getCategories(), taskViewModel.getUsers()).done(function() {
        ko.applyBindings(taskViewModel, $(".container")[0]);
        taskViewModel.search();
    });

});



