package asw1009.controller;

import asw1009.model.CategoriesManager;
import asw1009.model.TasksManager;
import asw1009.model.entities.Task;
import asw1009.requests.AddTaskRequest;
import asw1009.requests.DeleteTaskRequest;
import asw1009.requests.EditTaskRequest;
import asw1009.requests.PollingRequest;
import asw1009.responses.EditTaskResponse;
import asw1009.requests.SearchTasksRequest;
import asw1009.responses.AddTaskResponse;
import asw1009.responses.CategoriesListResponse;
import asw1009.responses.DeleteTaskResponse;
import asw1009.responses.SearchTasksResponse;
import asw1009.responses.LongPollingResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Classe che definisce tutti i servizi legati alla gestione dei task
 *
 * @author ASW1009
 */
@WebServlet(urlPatterns = {"/tasks/*"}, asyncSupported = true)
public class Tasks extends HttpServlet {

    private final String ACTION_SEARCH = "search";
    private final String ACTION_CATEGORIES = "categories";
    private final String ACTION_ADD = "add";
    private final String ACTION_EDIT = "edit";
    private final String ACTION_POLLING = "polling";
    private final String ACTION_DELETE = "delete";

    private HashMap<String, TaskPollingAsyncRequest> contexts;
    private Semaphore semaphore;
    private Gson gson;

    /**
     * Metodo che inizializza tutti gli attributi della classe
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        contexts = new HashMap<>();
        semaphore = new Semaphore(1);
    }

    /**
     * Metodo che definisce il comportamento dell'applicazione a seguito di
     * chiamate di tipo POST al server. Il metodo decodifica, per ogni serivzio
     * chiamato, i dati inviati tramite una variabile di tipo gson e richiama i
     * singoli metodi che si occupano di eseguire il servizio in questione
     *
     * @param request Oggetto HttpServletRequest che contine i dati della
     * richiesta http
     * @param response Oggetto HttpServletResponse che contine i dati della
     * risposta http
     * @throws ServletException
     * @throws IOException
     */
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
                    SearchTasksRequest requestData = gson.fromJson(json, SearchTasksRequest.class);
                    jsonResponse = gson.toJson(searchTasks(requestData), SearchTasksResponse.class);
                    break;
                }
                case ACTION_CATEGORIES: {
                    jsonResponse = gson.toJson(categoriesList(), CategoriesListResponse.class);
                    break;
                }
                case ACTION_ADD: {
                    AddTaskRequest requestData = gson.fromJson(json, AddTaskRequest.class);
                    AddTaskResponse responseData = addTask(requestData);
                    jsonResponse = gson.toJson(responseData, AddTaskResponse.class);
                    if (!responseData.hasError()) {
                        new pushTaskChangedNotificationThread(session.getId(), responseData.getTask(), 0).start();
                    }
                    break;
                }
                case ACTION_EDIT: {
                    EditTaskRequest requestData = gson.fromJson(json, EditTaskRequest.class);
                    EditTaskResponse responseData = editTask(requestData);
                    jsonResponse = gson.toJson(responseData, EditTaskResponse.class);
                    if (!responseData.hasError()) {

                        new pushTaskChangedNotificationThread(session.getId(), responseData.getTask(), 1).start();
                        //ora che ho pushato, non rischio di perdermi eventuali modifiche "interessanti"
                        //da adesso alla prossima polling request per questo client?
                    }
                    break;
                }
                case ACTION_POLLING: {
                    PollingRequest requestData = gson.fromJson(json, PollingRequest.class);
                    AsyncContext asyncContext = request.startAsync(request, response);
                    try {
                        asyncContext.setTimeout(0);
                        //il timeout a noi non serve... semplicemente se non ho nulla da
                        //mandare al client, il client resta lì in attesa "infinita"
                        semaphore.acquire();

                        contexts.put(session.getId(), new TaskPollingAsyncRequest(asyncContext, requestData, session.getId()));

                        semaphore.release();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Tasks.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
                case ACTION_DELETE: {
                    DeleteTaskRequest requestData = gson.fromJson(json, DeleteTaskRequest.class);
                    DeleteTaskResponse responseData = deleteTask(requestData);
                    jsonResponse = gson.toJson(responseData, DeleteTaskResponse.class);
                    if (!responseData.hasError()) {

                        new pushTaskChangedNotificationThread(session.getId(), responseData.getTask(), 2).start();
                        //ora che ho pushato, non rischio di perdermi eventuali modifiche "interessanti"
                        //da adesso alla prossima polling request per questo client?
                    }
                }
            }

            response.getOutputStream().print(jsonResponse);
            response.getOutputStream().flush();
        }

    }

    /**
     * Metodo che implementa il servizio di ricerca di più tasks
     *
     * @param request Oggetto SearchTasksRequest che contine i dati
 della richiesta
     *
     * @return Oggetto SearchTasksResponse che contiene l'elenco dei
 tasks richiesti
     */
    public SearchTasksResponse searchTasks(SearchTasksRequest request) {
        SearchTasksResponse response = new SearchTasksResponse();
        if (request != null) {
            response = TasksManager.getInstance().searchTasks(request);
            response.setError(false);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }

        return response;
    }

    /**
     *
     * Metodo che restituisce la lista delle categorie previste dal sistema
     * chiamando CategoriesManager, situato all'interno del model, il quale si
     * occupa di effettuare la ricerca
     *
     * @return Oggetto CategoriesListResponse contenente l'elenco delle
 categorie presenti
     */
    private CategoriesListResponse categoriesList() {
        CategoriesListResponse response = CategoriesManager.getInstance().categoriesList();
        response.setError(false);
        return response;
    }

    /**
     * Metodo che permette di creare un task chiamando TaskManager, situato
     * all'interno del model, il quale si occupa di effettuare la scrittura dei
     * dati sul server
     *
     * @param request Oggetto AddTaskRequest che contiene i dati della
 richiesta
     * @return VOggetto AddTaskResponse che contiene i valori del task
 appena inserito
     */
    private AddTaskResponse addTask(AddTaskRequest request) {

        AddTaskResponse response = new AddTaskResponse();
        if (request != null) {
            response = TasksManager.getInstance().addTask(request);
            response.setError(false);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }

        return response;
    }

    /**
     * Metodo che permette di eliminare un task chiamando TaskManager, situato
     * all'interno del model, il quale si occupa di effettuare la modifica dei
     * dati
     *
     * @param request Oggetto DeleteTaskRequest che contiene i dati
 della richiesta
     * @return Oggetto DeleteTaskResponse che contiene i valori del
 task appena eliminato
     */
    private DeleteTaskResponse deleteTask(DeleteTaskRequest request) {
        DeleteTaskResponse response = new DeleteTaskResponse();
        if (request != null) {
            response = TasksManager.getInstance().deleteTask(request);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }

        return response;
    }

    /**
     * Metodo che permette di modificare un task chiamando TaskManager, situato
     * all'interno del model, il quale si occupa di effettuare la modifica dei
     * dati
     *
     * @param request Oggetto DeleteTaskRequest che contiene i dati
 della richiesta
     * @return Oggetto DeleteTaskResponse che contiene i valori del
 task appena modificato
     */
    private EditTaskResponse editTask(EditTaskRequest request) {

        EditTaskResponse response = new EditTaskResponse();
        if (request != null) {
            response = TasksManager.getInstance().editTask(request);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }

        return response;
    }

    /**
     * Classe che permette di notificare al client un'azione su un task
     */
    private class pushTaskChangedNotificationThread extends Thread {

        //Source of notification
        String sessionId;

        //Edited task
        Task task;

        //Operation
        int operation;

        /**
         * Costruttore della classe
         *
         * @param sessionId Stringa che contiene l'id della sessione
         * @param task Oggetto Task che contiene i dati del task
         * @param operation Intero rappresentante il tipo di operazione.
         */
        public pushTaskChangedNotificationThread(String sessionId, Task task, int operation) {
            this.sessionId = sessionId;
            this.task = task;
            this.operation = operation;
        }

        /**
         * Metodo che verifica se un task visaulizzato nella timeline ha subito
         * modifche da altri utenti
         *
         * @param requestViewModel Oggetto PollingRequest che contiene
 i dati della richiesta
         * @param task Oggetto Task che contiene i dati del task in questione
         * @return Valore booleano che vale 1 se il task è presente nella
         * timeline e 0 se non è presente
         */
        private boolean isSubscribedToTask(PollingRequest requestViewModel, Task task) {
            for (int id : requestViewModel.getTaskIds()) {
                if (id == task.getId()) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Metodo che implementa l'attesa delle richieste tramite polling
         */
        @Override
        public void run() {
            try {
                semaphore.acquire();
                Iterator<Entry<String, TaskPollingAsyncRequest>> iter = contexts.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, TaskPollingAsyncRequest> entry = iter.next();
                    TaskPollingAsyncRequest asyncRequest = entry.getValue();
                    if (!sessionId.equals(asyncRequest.getSessionId())
                            && (TasksManager.getInstance().isTaskMatchingRequest(task, asyncRequest.getRequestViewModel().getSearchRequestViewModel())
                            || isSubscribedToTask(asyncRequest.getRequestViewModel(), task))) {
                        //Notify!
                        iter.remove();
                        AsyncContext context = asyncRequest.getContext();
                        HttpServletResponse clientToPush = (HttpServletResponse) context.getResponse();
                        LongPollingResponse responseData = new LongPollingResponse(operation, task);
                        String jsonResponse = gson.toJson(responseData, LongPollingResponse.class);

                        try {
                            clientToPush.getOutputStream().print(jsonResponse);
                            clientToPush.getOutputStream().flush();
                        } catch (IOException ex) {
                            System.out.println("Client disconnected");
                            //Client disconnected... nothing do do, just bye bye :)
                        }
                        context.complete();
                    }
                }
                semaphore.release();

            } catch (InterruptedException ex) {
                Logger.getLogger(Tasks.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
