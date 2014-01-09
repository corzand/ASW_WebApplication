
package asw1009.model;

import asw1009.model.entities.Task;
import asw1009.viewmodel.request.AddTaskRequestViewModel;
import asw1009.viewmodel.request.DeleteTaskRequestViewModel;
import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import asw1009.viewmodel.response.AddTaskResponseViewModel;
import asw1009.viewmodel.request.EditTaskRequestViewModel;
import asw1009.viewmodel.response.DeleteTaskResponseViewModel;
import asw1009.viewmodel.response.EditTaskResponseViewModel;
import asw1009.viewmodel.response.SearchTasksResponseViewModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TasksManager extends FileManager {

    private List<Task> _tasks;
    private static TasksManager instance;

    public TasksManager() {
        _tasks = new ArrayList<>();
    }

    @Override
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName); //To change body of generated methods, choose Tools | Templates.
        if (xml.exists()) {
            _tasks = (List<Task>) readXML(Task.class);
        } else {
            _tasks = new ArrayList<>();
        }
        //Eventualmente, leggere il contenuto del file Users.xml e impostare gli oggetti in memoria.
    }

    public static synchronized TasksManager getInstance() {
        if (instance == null) {
            instance = new TasksManager();
        }

        return instance;
    }

    private Task getTaskById(int id) {

        for (int i = 0; i < _tasks.size(); i++) {
            Task task = _tasks.get(i);
            if (task.getId() == id) {
                return task;
            }
        }

        return null;
    }


    private void _updateXML() {
        writeXML(_tasks, Task.class);
    }

    public boolean isTaskMatchingRequest(Task task, SearchTasksRequestViewModel request) {
        boolean toAdd = true;

        if (task.getDate().equals(request.getStartDate()) || task.getDate().equals(request.getEndDate()) ||
                (request.getStartDate().before(task.getDate())
                && request.getEndDate().after(task.getDate()))) {
            toAdd = toAdd && true;
        } else {
            toAdd = toAdd && false;
        }

        if (task.getUserId() == request.getUserId()) {
            toAdd = toAdd && true;
        } else if (task.getPersonal() == false) {
            toAdd = toAdd && true;
        } else if (task.getAssignedUserId() == request.getUserId()) {
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
        for (int i = 0; i < _tasks.size(); i++) {

            Task currentTask = _tasks.get(i);
            if (isTaskMatchingRequest(currentTask, request)) {
                tasksToAdd.add(currentTask);
            }
        }

        response.setTasks(tasksToAdd);

        return response;
    }

    public AddTaskResponseViewModel addTask(AddTaskRequestViewModel request) {
        AddTaskResponseViewModel response = new AddTaskResponseViewModel();
        Date date = new Date();
        Task task = new Task();
        task.setId(getNextId());
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
        task.setTimeStamp(date.getTime());

        _tasks.add(task);

        response.setTask(task);
        response.setError(false);

        _updateXML();

        return response;
    }

    public EditTaskResponseViewModel editTask(EditTaskRequestViewModel request) {
        EditTaskResponseViewModel viewModel = new EditTaskResponseViewModel();
        Task task = getTaskById(request.getId());

        if (task != null) {
            if (task.getTimeStamp() == request.getTimeStamp()) {
                Date date = new Date();
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
                task.setTimeStamp(date.getTime());

                viewModel.setTask(task);
                viewModel.setError(false);
                viewModel.setErrorMessage("");
            } else {
                viewModel.setTask(task);
                viewModel.setError(true);
                viewModel.setErrorMessage("Aggiornamento fallito");
                return viewModel;
            }
        } else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Aggiornamento fallito");
        }

        _updateXML();
        return viewModel;
    }

    public DeleteTaskResponseViewModel deleteTask(DeleteTaskRequestViewModel request) {
        DeleteTaskResponseViewModel viewModel = new DeleteTaskResponseViewModel();

        Task task = getTaskById(request.getId());

        if (task != null) {
            if (task.getTimeStamp() == request.getTimeStamp()) {

                _tasks.remove(task);

                viewModel.setTask(task);
                viewModel.setError(false);
                viewModel.setErrorMessage("");
            } else {
                viewModel.setTask(task);
                viewModel.setError(true);
                viewModel.setErrorMessage("Eliminazione fallita");
                return viewModel;
            }
        } else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Il task non esiste");
        }

        _updateXML();
        return viewModel;
    }

}
