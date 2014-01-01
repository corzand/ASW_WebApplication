/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import asw1009.model.entities.Category;
import asw1009.model.entities.Task;
import asw1009.viewmodel.request.AddTaskRequestViewModel;
import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import asw1009.viewmodel.response.AddTaskResponseViewModel;
import asw1009.viewmodel.request.EditTaskRequestViewModel;
import asw1009.viewmodel.response.EditTaskResponseViewModel;
import asw1009.viewmodel.response.SearchTasksResponseViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrea
 */
public class TasksManager extends FileManager {

    //private List<User> users;
    private EntityList<Task> _tasks;
    private static TasksManager instance;

    public TasksManager() {
        //users = new ArrayList<>();
        _tasks = new EntityList<>();
    }

    @Override
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName); //To change body of generated methods, choose Tools | Templates.

        _xstream.alias("task", Task.class);
        _xstream.alias("tasks", EntityList.class);
        _xstream.addImplicitCollection(EntityList.class, "list");
        _readXML();
        //Eventualmente, leggere il contenuto del file Users.xml e impostare gli oggetti in memoria.
    }

    public static synchronized TasksManager getInstance() {
        if (instance == null) {
            instance = new TasksManager();
        }

        return instance;
    }

    private Task getTaskById(int id) {

        for (int i = 0; i < _tasks.getItems().size(); i++) {
            Task task = _tasks.getItems().get(i);
            if (task.getId() == id) {
                return task;
            }
        }

        return null;
    }

    private void _readXML() {
        if (xml.exists()) {
            _tasks = (EntityList<Task>) readXML();
        } else {
            _tasks = new EntityList<>();
        }
    }

    private void _updateXML() {
        writeXML(_xstream.toXML(_tasks));
    }

    public boolean isTaskMatchingRequest(Task task, SearchTasksRequestViewModel request) {
        boolean toAdd = true;

        if (request.getStartDate().before(task.getDate())
                && request.getEndDate().after(task.getDate())) {
            toAdd = toAdd && true;
        } else {
            toAdd = toAdd && false;
        }

        if(task.getUserId() == request.getUserId()){
            toAdd = toAdd && true;
        }else if(task.getPersonal() == false){
            toAdd = toAdd && true;
        }else if(task.getAssignedUserId() == request.getUserId()) {
            toAdd = toAdd && true;
        } else {
            toAdd = toAdd && false;
        }
//        if (request.getPersonal()) {
//            if (request.getUserId() == task.getUserId()
//                    || request.getUserId() == task.getAssignedUserId()) {
//                toAdd = toAdd && true;
//            } else {
//                toAdd = toAdd && false;
//            }
//        } else {
//            if (request.getUserId() == task.getUserId()
//                    || request.getUserId() == task.getAssignedUserId()
//                    || !request.getPersonal()) {
//                toAdd = toAdd && true;
//            } else {
//                toAdd = toAdd && false;
//            }
//        }

//        boolean categoryMatch = false;
//        for (Category category : request.getCategories()) {
//            if (task.getCategoryId() == category.getId()) {
//                categoryMatch = true;
//                break;
//            }
//        }
//
//        if (categoryMatch) {
//            toAdd = toAdd && true;
//        } else {
//            toAdd = toAdd && false;
//        }

        return toAdd;
    }

    public SearchTasksResponseViewModel searchTasks(SearchTasksRequestViewModel request) {
        SearchTasksResponseViewModel response = new SearchTasksResponseViewModel();
        List<Task> tasksToAdd = new ArrayList<>();
        for (int i = 0; i < _tasks.getItems().size(); i++) {
            
            Task currentTask = _tasks.getItems().get(i);
            if (isTaskMatchingRequest(currentTask, request)) {
                tasksToAdd.add(currentTask);
            }
        }

        response.setTasks(tasksToAdd);

        return response;
    }

    public AddTaskResponseViewModel addTask(AddTaskRequestViewModel request) {
        AddTaskResponseViewModel response = new AddTaskResponseViewModel();
        Task task = new Task();
        task.setId(_tasks.getNextId());
        task.setAssignedUserId(request.getAssignedUserId());
        task.setAttachment(request.getAttachment());
        task.setCategoryId(request.getCategoryId());
        task.setDate(request.getDate());
        task.setDescription(request.getDescription());
        task.setDone(request.getDone());
        task.setLatitude(request.getLatitude());
        task.setLongitude(request.getLongitude());
        task.setPersonal(request.getPersonal());
        task.setTitle(request.getTitle());
        task.setUserId(request.getUserId());

        _tasks.getItems().add(task);

        response.setTask(task);
        response.setError(false);

        _updateXML();

        return response;
    }

    public EditTaskResponseViewModel editTask(EditTaskRequestViewModel request) {
        EditTaskResponseViewModel viewModel = new EditTaskResponseViewModel();
        Task task = getTaskById(request.getId());

        if (task != null) {
            task.setTitle(request.getTitle());
            task.setDescription(request.getDescription());
            task.setDate(request.getDate());
            task.setDone(request.getDone());
            task.setCategoryId(request.getCategoryId());
            task.setUserId(request.getUserId());
            task.setLatitude(request.getLatitude());
            task.setLongitude(request.getLongitude());
            task.setPersonal(request.getPersonal());
            task.setAssignedUserId(request.getAssignedUserId());
            task.setAttachment(request.getAttachment());
            
            viewModel.setTask(task);
            viewModel.setError(false);
            viewModel.setErrorMessage("");
        } else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Aggiornamento fallito");
        }

        _updateXML();
        return viewModel;
    }
}
