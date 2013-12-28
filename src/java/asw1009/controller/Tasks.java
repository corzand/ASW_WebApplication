package asw1009.controller;

import asw1009.model.TasksManager;
import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import asw1009.viewmodel.response.SearchTasksResponseViewModel;
import com.google.gson.Gson;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = {"/tasks/*"})
public class Tasks extends HttpServlet {
    
    private final String ACTION_SEARCH = "search";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        if (request.getContentType().contains("application/json")) {
            //JSON over HTTP

            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String json = br.readLine();
            Gson gson = new Gson();
            response.setContentType("application/json;");

            String action = request.getPathInfo().replace("/", "");
            String jsonResponse = "";
            switch(action){
                case ACTION_SEARCH :
                    SearchTasksRequestViewModel requestData = gson.fromJson(json, SearchTasksRequestViewModel.class);
                    jsonResponse = gson.toJson(searchTasks(requestData), SearchTasksResponseViewModel.class);
                    break;
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

}
