package asw1009.controller;

import asw1009.model.CategoriesManager;
import asw1009.model.TasksManager;
import asw1009.model.entities.Task;
import asw1009.viewmodel.request.AddTaskRequestViewModel;
import asw1009.viewmodel.request.DeleteTaskRequestViewModel;
import asw1009.viewmodel.request.EditTaskRequestViewModel;
import asw1009.viewmodel.response.EditTaskResponseViewModel;
import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import asw1009.viewmodel.response.AddTaskResponseViewModel;
import asw1009.viewmodel.response.CategoriesListResponseViewModel;
import asw1009.viewmodel.response.DeleteTaskResponseViewModel;
import asw1009.viewmodel.response.SearchTasksResponseViewModel;
import asw1009.viewmodel.response.TaskChangedPushNotificationViewModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = {"/tasks/*"}, asyncSupported = true)
public class Tasks extends HttpServlet {

    private final String ACTION_SEARCH = "search";
    private final String ACTION_CATEGORIES = "categories";
    private final String ACTION_ADD = "add";
    private final String ACTION_EDIT = "edit";
    private final String ACTION_POLLING = "polling";
    private final String ACTION_DELETE = "delete";

    private HashMap<Integer, TaskPollingAsyncRequest> contexts;
    private Semaphore semaphore;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        contexts = new HashMap<>();
        semaphore = new Semaphore(1);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        if (request.getContentType().contains("application/json")) {
            //JSON over HTTP

            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String json = br.readLine();
            response.setContentType("application/json;");

            String action = request.getPathInfo().replace("/", "");
            String jsonResponse = "";
            switch (action) {
                case ACTION_SEARCH: {
                    SearchTasksRequestViewModel requestData = gson.fromJson(json, SearchTasksRequestViewModel.class);
                    jsonResponse = gson.toJson(searchTasks(requestData), SearchTasksResponseViewModel.class);
                    break;
                }
                case ACTION_CATEGORIES: {
                    jsonResponse = gson.toJson(categoriesList(), CategoriesListResponseViewModel.class);
                    break;
                }
                case ACTION_ADD: {
                    AddTaskRequestViewModel requestData = gson.fromJson(json, AddTaskRequestViewModel.class);
                    AddTaskResponseViewModel responseData = addTask(requestData);
                    jsonResponse = gson.toJson(responseData, AddTaskResponseViewModel.class);
                    if (!responseData.hasError()) {
                        new pushTaskChangedNotificationThread(requestData.getUserId(), responseData.getTask(), 0).start();                        
                    }
                    break;
                }
                case ACTION_EDIT: {
                    EditTaskRequestViewModel requestData = gson.fromJson(json, EditTaskRequestViewModel.class);
                    EditTaskResponseViewModel responseData = editTask(requestData);
                    jsonResponse = gson.toJson(responseData, EditTaskResponseViewModel.class);
                    if (!responseData.hasError()) {

                        new pushTaskChangedNotificationThread(requestData.getUserId(), responseData.getTask(), 1).start();
                        //ora che ho pushato, non rischio di perdermi eventuali modifiche "interessanti"
                        //da adesso alla prossima polling request per questo client?
                    }
                    break;
                }
                case ACTION_POLLING: {
                    SearchTasksRequestViewModel requestData = gson.fromJson(json, SearchTasksRequestViewModel.class);
                    AsyncContext asyncContext = request.startAsync(request, response);
                    try {
                        asyncContext.setTimeout(0);
                        //il timeout a noi non serve... semplicemente se non ho nulla da
                        //mandare al client, il client resta lì in attesa "infinita"
                        semaphore.acquire();
                        
                        contexts.put(requestData.getUserId(), new TaskPollingAsyncRequest(asyncContext, requestData));
                        
                        semaphore.release();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Tasks.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
                case ACTION_DELETE: {
                    DeleteTaskRequestViewModel requestData = gson.fromJson(json, DeleteTaskRequestViewModel.class);
                    DeleteTaskResponseViewModel responseData = deleteTask(requestData);
                    jsonResponse = gson.toJson(responseData, DeleteTaskResponseViewModel.class);
                    if (!responseData.hasError()) {

                        new pushTaskChangedNotificationThread(requestData.getUserId(), responseData.getTask(), 2).start();
                        //ora che ho pushato, non rischio di perdermi eventuali modifiche "interessanti"
                        //da adesso alla prossima polling request per questo client?
                    }
                }
            }

            response.getOutputStream().print(jsonResponse);
            response.getOutputStream().flush();
        }

    }

    public SearchTasksResponseViewModel searchTasks(SearchTasksRequestViewModel request) {
        SearchTasksResponseViewModel response = new SearchTasksResponseViewModel();
        if (request != null) {
            response = TasksManager.getInstance().searchTasks(request);
            response.setError(false);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }

        return response;
    }

    private CategoriesListResponseViewModel categoriesList() {
        CategoriesListResponseViewModel response = CategoriesManager.getInstance().categoriesList();
        response.setError(false);
        return response;
    }

    private AddTaskResponseViewModel addTask(AddTaskRequestViewModel request) {

        AddTaskResponseViewModel response = new AddTaskResponseViewModel();
        if (request != null) {
            response = TasksManager.getInstance().addTask(request);
            response.setError(false);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }

        return response;
    }
    
    private DeleteTaskResponseViewModel deleteTask(DeleteTaskRequestViewModel request){
        DeleteTaskResponseViewModel response = new DeleteTaskResponseViewModel();
        if(request != null){
            response = TasksManager.getInstance().deleteTask(request);
        }else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }
        
        return response;
    }

    private EditTaskResponseViewModel editTask(EditTaskRequestViewModel request) {

        EditTaskResponseViewModel response = new EditTaskResponseViewModel();
        if (request != null) {
            response = TasksManager.getInstance().editTask(request);
            response.setError(false);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }

        return response;
    }

    private class pushTaskChangedNotificationThread extends Thread {

        int userId;
        Task task;
        int operation;

        public pushTaskChangedNotificationThread(int userId, Task task, int operation) {
            this.userId = userId;
            this.task = task;
            this.operation = operation;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                Iterator<Entry<Integer,TaskPollingAsyncRequest>> iter = contexts.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<Integer,TaskPollingAsyncRequest> entry = iter.next();
                    TaskPollingAsyncRequest asyncRequest = entry.getValue();
                    if (userId != asyncRequest.getRequestViewModel().getUserId()
                            && TasksManager.getInstance().isTaskMatchingRequest(task, asyncRequest.getRequestViewModel())) {
                        //Notify!
                        iter.remove();
                        AsyncContext context = asyncRequest.getContext();
                        HttpServletResponse clientToPush = (HttpServletResponse) context.getResponse();

                        TaskChangedPushNotificationViewModel responseData = new TaskChangedPushNotificationViewModel(operation, task);
                        String jsonResponse = gson.toJson(responseData, TaskChangedPushNotificationViewModel.class);
                        clientToPush.getOutputStream().print(jsonResponse);
                        clientToPush.getOutputStream().flush();

                        context.complete();
                    }
                }
                semaphore.release();

            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(Tasks.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
