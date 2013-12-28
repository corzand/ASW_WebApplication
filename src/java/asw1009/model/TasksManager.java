/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import asw1009.model.entities.Category;
import asw1009.model.entities.Task;
import asw1009.viewmodel.request.SearchTasksRequestViewModel;
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

    public void addTask(Task task) {
        task.setId(_tasks.getNextId());
        _tasks.getItems().add(task);
        
        _updateXML();
    }

    private void _readXML(){
        if(xml.exists()){
            _tasks = (EntityList<Task>)readXML();
        }else {
            _tasks = new EntityList<>();
        }
    }
    
    private void _updateXML(){        
        writeXML(_xstream.toXML(_tasks));
    }
    
    public SearchTasksResponseViewModel searchTasks(SearchTasksRequestViewModel request) {
        SearchTasksResponseViewModel response = new SearchTasksResponseViewModel();
        List<Task> tasksToAdd = new ArrayList<>();
        for(int i=0; i<_tasks.getItems().size(); i++){
            Task currentTask = _tasks.getItems().get(i);            
            boolean toAdd = true;
            
            if(request.getStartDate().before(currentTask.getDate()) 
                    && request.getEndDate().after(currentTask.getDate())){
                toAdd = toAdd && true;
            }else {
                toAdd = toAdd && false;
            }
            
            if(request.getPersonal()){
                if(request.getUserId() == currentTask.getUserId() ||
                        request.getUserId() == currentTask.getAssignedUserId()){
                    toAdd = toAdd && true;
                }else {
                    toAdd = toAdd && false;
                }
            }else {
                if(request.getUserId() == currentTask.getUserId() ||
                        request.getUserId() == currentTask.getAssignedUserId() ||
                        !request.getPersonal()){
                    toAdd = toAdd && true;
                }else {
                    toAdd = toAdd && false;
                }
            }
            
            boolean categoryMatch = false;
            for(Category category : request.getCategories()){                
                if(currentTask.getCategoryId() == category.getId()){
                    categoryMatch = true;
                    break;
                }
            }
            
            if(categoryMatch){
                toAdd = toAdd && true;
            }else {
                toAdd = toAdd && false;
            }
            
            
            if(toAdd){
                tasksToAdd.add(currentTask);
            }
        }
        
        response.setTasks(tasksToAdd);
        
        return response;
    } 
}
