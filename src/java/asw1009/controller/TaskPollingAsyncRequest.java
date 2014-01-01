/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1009.controller;

import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import javax.servlet.AsyncContext;

/**
 *
 * @author Andrea
 */
public class TaskPollingAsyncRequest {
    private AsyncContext context;
    private SearchTasksRequestViewModel requestViewModel;
    
    public TaskPollingAsyncRequest(AsyncContext context, SearchTasksRequestViewModel requestViewModel){
        this.context = context;
        this.requestViewModel = requestViewModel;
    }
    
    public AsyncContext getContext(){
        return this.context;
    }
    
    public SearchTasksRequestViewModel getRequestViewModel(){
        return this.requestViewModel;
    }
}
