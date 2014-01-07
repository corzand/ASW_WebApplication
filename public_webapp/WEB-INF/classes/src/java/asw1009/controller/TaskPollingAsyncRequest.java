
package asw1009.controller;

import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import javax.servlet.AsyncContext;

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
