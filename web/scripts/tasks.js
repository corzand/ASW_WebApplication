/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var tasksInitialData;

function tasksViewModelDefinition() {
	var self = this;
	
	self.Personal = ko.observable(true);
	self.Categories = ko.observableArray([]);
	self.Users = ko.observableArray([]);
	self.CurrentTask = new function (){
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
	self.StartDate = new Date();//settare a ieri
	self.EndDate = new Date();

	self.getActiveCategories = function(){
		var activeCategories = [];
		
		for(var i=0; i<Categories.length;i++){
			if(Categories()[i].state() === true){
				activeCategories.push(Categories()[i]);//bisogna passare un oggetto che non abbia observable?
			}
		}
		return activeCategories;
	};

	self.searchData=function(){
		return {
			startDate: self.StartDate,
			endDate : self.EndDate,
			userId :loggedUser.id,
			categories : self.getActiveCategories(),
			personal : self.personal()
		};
	};
	
	self.searchCallback = function(data){
 		
		self.Days().removeAll();
                
		var currentDay = self.StartDate;
		var range = dateDiff(self.StartDate,self.EndDate);
		
		for(var i=0;i<range;i++){
			self.Days().push({
				day : currentDay,
				tasks : ko.observableArray([])
			});
			currentDay.setDate(currentDay.getDate()+1);
		}
		
		for(i=0;i<data.tasks.length;i++){
			for(var j=0; j<self.Days().length;j++){
				if(compareDate(data.tasks[i].date, self.Days()[j].day)){
					self.Days()[j].push({
						id : ko.observable(data.tasks[i].id),
						title : ko.observable(data.tasks[i].title),
						description : ko.observable(data.tasks[i].description),
						date : ko.observable(data.tasks[i].date),
						done : ko.observable(data.tasks[i].done),
						personal : ko.observable(data.tasks[i].personal),
						userId : ko.observable(data.tasks[i].userId),
						assignedUserId : ko.observable(data.tasks[i].assignedUserId),
						latitude : ko.observable(data.tasks[i].latitude),
						longitude : ko.observable(data.tasks[i].longitude),
						categoryId : ko.observable(data.tasks[i].categoryId),
						attachment : ko.observable(data.tasks[i].attachment)						
					});
				}
			}
		}
		
	};
	

	self.search=function(){
		var rSettings = new requestSettings();
		rSettings.url = '/tasks/search/';
		rSettings.requestData = JSON.stringify(self.searchData());
		rSettings.successCallback = self.searchCallback;
		sendRequest(rSettings);
	};

	self.init = function(){
		for(var i=0; i<tasksInitialData.categories.length;i++){
			self.Categories.push({
				id:ko.observable(tasksInitialData.categories[i].id),
				title:ko.observable(tasksInitialData.categories[i].title),
				state:ko.observable(tasksInitialData.categories[i].state),
				color:ko.observable(tasksInitialData.categories[i].color)
			});
		}
	
		for(i=0; i<tasksInitialData.users.length;i++){
			self.Users.push({
				id:ko.observable(tasksInitialData.users[i].id),
				username:ko.observable(tasksInitialData.users[i].username),
				picture:ko.observable(tasksInitialData.users[i].picture)
			});
		}
	
		self.StartDate.setDate(self.StartDate.getDate()-1);
		self.EndDate.setDate(self.EndDate.getDate()+10);
	};
}

$(document).ready(function() {
	//init view model and stuff
	var taskViewModel = new taskViewModelDefinition();
	taskViewModel.init();
	ko.applyBindings(taskViewModel,$(".container")[0]);
        
        taskViewModel.search();
});



