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
 * Controller che espone i servizi relativi ai Tasks.
 * Intercetta tutte le chiamate che hanno come url pattern /tasks/
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
     * Metodo che inizializza i campi del controller, serializzatore json
     * semaforo per gestire la concorrenza e HashMap contenente le richieste asincrone
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
     * Intercetta tutte le chiamate POST al server. 
     * L'ultima parte dell'URL di chiamata al servizio, specifica il nome dell'operation
     * che il client richiede. Tutte le operations prevedono uno scambio di dati con il client
     * sotto forma di stringhe JSON (over http).
     * Il controller, sulla base della stringa richiesta dal client, seleziona le operations 
     * da eseguire e risponde al client. La risposta avviene sempre in modo sincrono, eccetto per la 
     * richiesta di polling in cui la risposta avviene in modo asincrono, solo quando c'è una notifica
     * da inviare.
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
                        //Start notification thread
                        new pushTaskChangedNotificationThread(session.getId(), responseData.getTask(), 0).start();
                    }
                    break;
                }
                case ACTION_EDIT: {
                    EditTaskRequest requestData = gson.fromJson(json, EditTaskRequest.class);
                    EditTaskResponse responseData = editTask(requestData);
                    jsonResponse = gson.toJson(responseData, EditTaskResponse.class);
                    if (!responseData.hasError()) {
                        //Start notification thread
                        new pushTaskChangedNotificationThread(session.getId(), responseData.getTask(), 1).start();
                    }
                    break;
                }
                case ACTION_POLLING: {
                    PollingRequest requestData = gson.fromJson(json, PollingRequest.class);
                    //Risposta asincrona;
                    AsyncContext asyncContext = request.startAsync(request, response);
                    try {
                        asyncContext.setTimeout(0);
                        //il timeout a noi non serve, il client resta in attesa
                        //acquire semaphore
                        semaphore.acquire();

                        //Metto la risposta nella lista di oggetti, la chiave della hashMap è sessionId
                        contexts.put(session.getId(), new TaskPollingAsyncRequest(asyncContext, requestData, session.getId()));

                        //release semaphore
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
                        //Start notification thread
                        new pushTaskChangedNotificationThread(session.getId(), responseData.getTask(), 2).start();
                    }
                }
            }

            response.getOutputStream().print(jsonResponse);
            response.getOutputStream().flush();
        }

    }

    /**
     * Operation che offre il servizio di ricerca tasks
     *
     * @param request Oggetto SearchTasksRequest che contine i dati della richiesta
     *
     * @return Oggetto SearchTasksResponse che contiene l'elenco dei tasks richiesti
     */
    private SearchTasksResponse searchTasks(SearchTasksRequest request) {
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
     * @return Oggetto CategoriesListResponse contenente l'elenco delle categorie presenti
     */
    private CategoriesListResponse categoriesList() {
        CategoriesListResponse response = CategoriesManager.getInstance().categoriesList();
        response.setError(false);
        return response;
    }

    /**
     * Metodo che permette di creare un task chiamando la singleton TaskManager, situata
     * all'interno del model, la quale si occupa della creazione dei dati
     *
     * @param request Oggetto AddTaskRequest che contiene i dati della richiesta
     * @return VOggetto AddTaskResponse che contiene i valori del task appena inserito
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
     * Metodo che permette di eliminare un task chiamando la singleton TaskManager, situata
     * all'interno del model, la quale si occupa di effettuare l'eliminazione dati
     *
     * @param request Oggetto DeleteTaskRequest che contiene i dati della richiesta
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
     * Metodo che permette di modificare un task chiamando la singleton TaskManager, situata
     * all'interno del model, la quale si occupa di effettuare la modifica dei dati
     *
     * @param request Oggetto DeleteTaskRequest che contiene i dati della richiesta
     * @return Oggetto DeleteTaskResponse che contiene i valori del task appena modificato
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
     * Thread utilizzato per notificare al client un'azione effettuata da 
     * un'altro client su un task
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
         * Metodo utilizzato per capire se l'utente è sottoscritto al task che è stato modificato, 
         * in modo da inviargli eventualmente la notifica
         *
         * @param requestViewModel Oggetto PollingRequest che contiene i dati della richiesta di polling dell'utente
         * @param task Oggetto Task che contiene i dati del task modificato
         * @return Valore booleano che vale 1 se il task è presente nella timeline dell'utente e 0 se non è presente
         */
        private boolean isSubscribedToTask(PollingRequest requestViewModel, Task task) {
            for (int id : requestViewModel.getTaskIds()) {
                if (id == task.getId()) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void run() {
            try {
                //Preso il lock sulla lista delle richieste asincrone
                semaphore.acquire();
                Iterator<Entry<String, TaskPollingAsyncRequest>> iter = contexts.entrySet().iterator();
                while (iter.hasNext()) {
                    //per ogni richiesta, si valuta se mandare la notifica del task modificato.
                    Entry<String, TaskPollingAsyncRequest> entry = iter.next();
                    TaskPollingAsyncRequest asyncRequest = entry.getValue();
                    
                    if (!sessionId.equals(asyncRequest.getSessionId())
                            && (TasksManager.getInstance().isTaskMatchingRequest(task, asyncRequest.getRequest().getSearchRequestViewModel())
                            || isSubscribedToTask(asyncRequest.getRequest(), task))) {
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
