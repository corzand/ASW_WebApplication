
package asw1009.controller;

import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import javax.servlet.AsyncContext;

public class TaskPollingAsyncRequest {
    private AsyncContext context;
    private SearchTasksRequestViewModel requestViewModel;
    private String sessionId;
    
    public TaskPollingAsyncRequest(AsyncContext context, SearchTasksRequestViewModel requestViewModel, String sessionId){
        this.context = context;
        this.sessionId = sessionId;
        this.requestViewModel = requestViewModel;
    }
    
    public AsyncContext getContext(){
        return this.context;
    }
    
    public SearchTasksRequestViewModel getRequestViewModel(){
        return this.requestViewModel;
    }
    
    public String getSessionId(){
        return this.sessionId;
    }
}
