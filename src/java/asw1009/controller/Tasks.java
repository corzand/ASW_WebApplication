package asw1009.controller;

import asw1009.model.CategoriesManager;
import asw1009.model.TasksManager;
import asw1009.viewmodel.request.AddTaskRequestViewModel;
import asw1009.viewmodel.request.EditTaskRequestViewModel;
import asw1009.viewmodel.response.EditTaskResponseViewModel;
import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import asw1009.viewmodel.response.AddTaskResponseViewModel;
import asw1009.viewmodel.response.CategoriesListResponseViewModel;
import asw1009.viewmodel.response.SearchTasksResponseViewModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = {"/tasks/*"})
public class Tasks extends HttpServlet {
    
    private final String ACTION_SEARCH = "search";
    private final String ACTION_CATEGORIES = "categories";
    private final String ACTION_ADD = "add";
    private final String ACTION_EDIT = "edit";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        if (request.getContentType().contains("application/json")) {
            //JSON over HTTP

            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String json = br.readLine();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();;
            response.setContentType("application/json;");

            String action = request.getPathInfo().replace("/", "");
            String jsonResponse = "";
            switch(action){
                case ACTION_SEARCH :{
                    SearchTasksRequestViewModel requestData = gson.fromJson(json, SearchTasksRequestViewModel.class);
                    jsonResponse = gson.toJson(searchTasks(requestData), SearchTasksResponseViewModel.class);
                    break;
                }
                case ACTION_CATEGORIES:{
                    jsonResponse = gson.toJson(categoriesList(), CategoriesListResponseViewModel.class);
                    break;
                }
                case ACTION_ADD: {
                    AddTaskRequestViewModel requestData = gson.fromJson(json, AddTaskRequestViewModel.class);
                    jsonResponse = gson.toJson(addTask(requestData), AddTaskResponseViewModel.class);
                    break;
                }
				case ACTION_EDIT: {
                    EditTaskRequestViewModel requestData = gson.fromJson(json, EditTaskRequestViewModel.class);
                    jsonResponse = gson.toJson(editTask(requestData), EditTaskResponseViewModel.class);
                    break;
                }
            }

            response.getOutputStream().print(jsonResponse);
            response.getOutputStream().flush();
        }

    }
    
    public SearchTasksResponseViewModel searchTasks(SearchTasksRequestViewModel request){
        SearchTasksResponseViewModel response = new SearchTasksResponseViewModel(); 
        if (request != null) {
            response = TasksManager.getInstance().searchTasks(request);
            response.setError(false);
        }else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }
        
        return response;
    }
    
    private CategoriesListResponseViewModel categoriesList(){
        CategoriesListResponseViewModel response = CategoriesManager.getInstance().categoriesList();        
        response.setError(false);
        return response;
    }

    private AddTaskResponseViewModel addTask(AddTaskRequestViewModel request){
        
        AddTaskResponseViewModel response = new AddTaskResponseViewModel();
        if(request != null){
            response = TasksManager.getInstance().addTask(request);            
            response.setError(false);
        }else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }
        
        return response;
    }
	
	private EditTaskResponseViewModel editTask(EditTaskRequestViewModel request){
        
        EditTaskResponseViewModel response = new EditTaskResponseViewModel();
        if(request != null){
            response = TasksManager.getInstance().editTask(request);            
            response.setError(false);
        }else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }
        
        return response;
    }
}
