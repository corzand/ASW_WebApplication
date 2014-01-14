
package asw1009.model;

import asw1009.model.entities.Task;
import asw1009.viewmodel.request.AddTaskRequestViewModel;
import asw1009.viewmodel.request.DeleteTaskRequestViewModel;
import asw1009.viewmodel.request.SearchTasksRequestViewModel;
import asw1009.viewmodel.response.AddTaskResponseViewModel;
import asw1009.viewmodel.request.EditTaskRequestViewModel;
import asw1009.viewmodel.response.DeleteTaskResponseViewModel;
import asw1009.viewmodel.response.EditTaskResponseViewModel;
import asw1009.viewmodel.response.SearchTasksResponseViewModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe singleton rappresentante il gestore dei task.
 * Contiene la lista delle entità Task.
 * 
 * @author ASW1009
*/


public class TasksManager extends FileManager {

    private List<Task> _tasks;
    private static TasksManager instance;
    
    /**
    * Costruttore di classe.
    */
    public TasksManager() {
        _tasks = new ArrayList<>();
    }

    
    @Override
    /**
    * Metodo che fa l'override del metodo init() della classe FileManager: se il file.xml esiste
    * istanzia la lista di task leggendo dal file, altrimenti crea una lista di task nuova. 
    * 
    * @param directoryPath stringa rappresentante il percorso del file.
    * @param fileName stringa rappresentante il nome del file.
    */
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName);
        if (xml.exists()) {
            _tasks = (List<Task>) readXML(Task.class);
        } else {
            _tasks = new ArrayList<>();
        }
    }
    
    /**
    * Restituisce l'istanza della classe.
    * @return instance statico del gestore del task inizilizzato all'inizio a null.
    */
    public static synchronized TasksManager getInstance() {
        if (instance == null) {
            instance = new TasksManager();
        }
        return instance;
    }
    
   /**
    * Restituisce il task corrispondente all'id inserito, prendendolo dalla lista dei task 
    * @param id intero rappresentante l'identificativo del task
    * @return task relativo alla lista dei task
    */
    private Task getTaskById(int id) {

        for (int i = 0; i < _tasks.size(); i++) {
            Task task = _tasks.get(i);
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }
    
    /**
     * Aggiorna il task scrivendolo 
     */
    private void _updateXML() {
        writeXML(_tasks, Task.class);
    }
    
    /**
     * Metodo che verifica se il task corrente corrisponde alla richiesta
     * @param task rappresentante un singolo task
     * @param request indica il viewModel rappresentante la richiesta di match del task
     * @return toAdd Booleano rappresentante il buon esito del maching
     */
    public boolean isTaskMatchingRequest(Task task, SearchTasksRequestViewModel request) {
        boolean toAdd = true;

        if (task.getDate().equals(request.getStartDate()) || task.getDate().equals(request.getEndDate()) ||
                (request.getStartDate().before(task.getDate())
                && request.getEndDate().after(task.getDate()))) {
            toAdd = toAdd && true;
        } else {
            toAdd = toAdd && false;
        }

        if (task.getUserId() == request.getUserId()) {
            toAdd = toAdd && true;
        } else if (task.getPersonal() == false) {
            toAdd = toAdd && true;
        } else if (task.getAssignedUserId() == request.getUserId()) {
            toAdd = toAdd && true;
        } else {
            toAdd = toAdd && false;
        }
        return toAdd;
    }
    
    /**
     * Effettua la ricerca dei tasks.
     * @param request indica il viewModel rappresentante la richiesta di ricerca del task.
     * @return response che contiene la lista dei task.
    */
    public SearchTasksResponseViewModel searchTasks(SearchTasksRequestViewModel request) {
        SearchTasksResponseViewModel response = new SearchTasksResponseViewModel();
        List<Task> tasksToAdd = new ArrayList<>();
        for (int i = 0; i < _tasks.size(); i++) {

            Task currentTask = _tasks.get(i);
            if (isTaskMatchingRequest(currentTask, request)) {
                tasksToAdd.add(currentTask);
            }
        }

        response.setTasks(tasksToAdd);

        return response;
    }
    
   /**
    * Metodo invocato per aggiungere un task.
    * @param request indica il viewModel rappresentate la richiesta di aggiunta del task
    * da parte del client.
    * @return response che contiene il task aggiornato.
    */
    public AddTaskResponseViewModel addTask(AddTaskRequestViewModel request) {
        AddTaskResponseViewModel response = new AddTaskResponseViewModel();
        Date date = new Date();
        Task task = new Task();
        task.setId(getNextId());
        task.setAssignedUserId(request.getAssignedUserId());
        task.setAttachment(request.getAttachment());
        task.setCategoryId(request.getCategoryId());
        task.setDate(request.getDate());
        task.setDescription(request.getDescription());
        task.setDone(request.getDone());
        task.setLatitude(request.getLatitude());
        task.setLongitude(request.getLongitude());
        task.setPersonal(request.getPersonal());
        task.setTitle(request.getTitle());
        task.setUserId(request.getUserId());
        task.setTimeStamp(date.getTime());

        _tasks.add(task);

        response.setTask(task);
        response.setError(false);

        _updateXML();

        return response;
    }

    /**
     * Metodo invocato per effettuare la modifica di un task. Verifica che il timeStamp sia uguale
     * a quello dell'ultima versione, in caso contrario invia un messaggio di errato aggiornamento.
     * @param request indica il viewModel che rappresenta la richiesta di modifica del task 
     * da parte del client.
     * @return viewModel che contiene il task aggiornato.
     */
    public EditTaskResponseViewModel editTask(EditTaskRequestViewModel request) {
        EditTaskResponseViewModel viewModel = new EditTaskResponseViewModel();
        Task task = getTaskById(request.getId());

        if (task != null) {
            if (task.getTimeStamp() == request.getTimeStamp()) {
                Date date = new Date();
                task.setTitle(request.getTitle());
                task.setDescription(request.getDescription());
                task.setDate(request.getDate());
                task.setDone(request.getDone());
                task.setCategoryId(request.getCategoryId());
                task.setLatitude(request.getLatitude());
                task.setLongitude(request.getLongitude());
                task.setPersonal(request.getPersonal());
                task.setAssignedUserId(request.getAssignedUserId());
                task.setAttachment(request.getAttachment());
                task.setTimeStamp(date.getTime());

                viewModel.setTask(task);
                viewModel.setError(false);
                viewModel.setErrorMessage("");
            } else {
                viewModel.setTask(task);
                viewModel.setError(true);
                viewModel.setErrorMessage("Aggiornamento fallito");
                return viewModel;
            }
        } else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Aggiornamento fallito");
        }

        _updateXML();
        return viewModel;
    }

    /**
     * Metodo invocato per effettuare la cancellazione del task. Come per l'editTask si verifica 
     * che il il timeStamp sia uguale a quello dell'ultima versione, in caso contrario invia 
     * un messaggio di errato aggiornamento.
     * @param request indica il viewModel che rappresenta la modifica di cancellazione del task 
     * da parte del client.
     * @return viewModel che contiene il task.
     */
    public DeleteTaskResponseViewModel deleteTask(DeleteTaskRequestViewModel request) {
        DeleteTaskResponseViewModel viewModel = new DeleteTaskResponseViewModel();

        Task task = getTaskById(request.getId());

        if (task != null) {
            if (task.getTimeStamp() == request.getTimeStamp()) {

                _tasks.remove(task);

                viewModel.setTask(task);
                viewModel.setError(false);
                viewModel.setErrorMessage("");
            } else {
                viewModel.setTask(task);
                viewModel.setError(true);
                viewModel.setErrorMessage("Eliminazione fallita");
                return viewModel;
            }
        } else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Il task non esiste");
        }

        _updateXML();
        return viewModel;
    }

}
