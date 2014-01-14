
package asw1009.controller;

import asw1009.viewmodel.request.PollingRequestViewModel;
import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import javax.servlet.AsyncContext;

public class TaskPollingAsyncRequest {
    private AsyncContext context;
    private PollingRequestViewModel requestViewModel;
    private String sessionId;
    
    public TaskPollingAsyncRequest(AsyncContext context, PollingRequestViewModel requestViewModel, String sessionId){
        this.context = context;
        this.sessionId = sessionId;
        this.requestViewModel = requestViewModel;
    }
    
    public AsyncContext getContext(){
        return this.context;
    }
    
    public PollingRequestViewModel getRequestViewModel(){
        return this.requestViewModel;
    }
    
    public String getSessionId(){
        return this.sessionId;
    }
}
