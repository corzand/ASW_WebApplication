package asw1009.model;

import asw1009.model.entities.Task;
import asw1009.model.entities.TaskList;
import asw1009.requests.AddTaskRequest;
import asw1009.requests.DeleteTaskRequest;
import asw1009.requests.SearchTasksRequest;
import asw1009.responses.AddTaskResponse;
import asw1009.requests.EditTaskRequest;
import asw1009.responses.DeleteTaskResponse;
import asw1009.responses.EditTaskResponse;
import asw1009.responses.SearchTasksResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Classe singleton rappresentante il gestore dei task. Contiene la lista delle
 * entità Task.
 *
 * @author ASW1009
 */
public class TasksManager {

    private TaskList items;
    private static TasksManager instance;
    private JAXBContext jc;
    private Unmarshaller um;
    private Marshaller m;
    protected File xml;
    protected String directoryPath;
    private String fileName;

    /**
     * Costruttore di classe.
     */
    public TasksManager() {
    }

    /**
     * Override del metodo init() della classe FileManager: se
     * il file.xml esiste istanzia la lista di task leggendo dal file,
     * altrimenti crea una lista di task nuova.
     *
     * @param directoryPath stringa rappresentante il percorso del file.
     */
    public void init(String directoryPath) {
       try {
            this.jc = JAXBContext.newInstance(TaskList.class);
            this.um = jc.createUnmarshaller();
            this.m = jc.createMarshaller();
            this.directoryPath = directoryPath;
            this.fileName = "Tasks";
            this.xml = new File(this.directoryPath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "xml" + System.getProperty("file.separator") + this.fileName + ".xml");

            if (xml.exists()) {
                items = (TaskList) um.unmarshal(xml);
            } else {
                items = new TaskList();
            }
        } catch (JAXBException ex) {
            Logger.getLogger(CategoriesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Restituisce l'istanza della classe.
     *
     * @return istanza statica del gestore dei task 
     */
    public static synchronized TasksManager getInstance() {
        if (instance == null) {
            instance = new TasksManager();
        }
        return instance;
    }

    /**
     * Restituisce il task corrispondente all'id passato come parametro, 
     * prendendolo dalla lista in memoria
     *
     * @param id intero rappresentante l'identificativo del task
     * @return oggetto task, altrimenti null
     */
    private Task getTaskById(int id) {

        for (int i = 0; i < items.getList().size(); i++) {
            Task task = items.getList().get(i);
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    /**
     * Aggiorna il fileXML scrivendolo.
     */
    private void updateXML() {
        try {
            m.marshal(items, xml);
        } catch (JAXBException ex) {
            Logger.getLogger(UsersManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metodo che verifica se il task passato come parametro "matcha" la richiesta 
     * di ricerca task effettuata dall'utente
     *
     * @param task rappresentante un singolo task
     * @param request rappresenta la richiesta ricerca task
     * @return toAdd Booleano rappresentante l'esito del matching
     */
    public boolean isTaskMatchingRequest(Task task, SearchTasksRequest request) {
        boolean toAdd = true;

        if (task.getDate().equals(request.getStartDate()) || task.getDate().equals(request.getEndDate())
                || (request.getStartDate().before(task.getDate())
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
     *
     * @param request richiesta di ricerca dei tasks.
     * @return response che contiene la lista dei task.
     */
    public SearchTasksResponse searchTasks(SearchTasksRequest request) {
        SearchTasksResponse response = new SearchTasksResponse();
        List<Task> tasksToAdd = new ArrayList<>();
        for (int i = 0; i < items.getList().size(); i++) {

            Task currentTask = items.getList().get(i);
            if (isTaskMatchingRequest(currentTask, request)) {
                tasksToAdd.add(currentTask);
            }
        }

        response.setTasks(tasksToAdd);

        return response;
    }

    /**
     * Metodo richiamato per aggiungere un task.
     *
     * @param request rappresenta la richiesta di aggiunta del task da parte dell'utente.
     * @return response che contiene il task aggiornato.
     */
    public AddTaskResponse addTask(AddTaskRequest request) {
        AddTaskResponse response = new AddTaskResponse();
        Date date = new Date();
        Task task = new Task();
        task.setId(items.getNextId());
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

        items.getList().add(task);

        response.setTask(task);
        response.setError(false);

        updateXML();

        return response;
    }

    /**
     * Metodo invocato per effettuare la modifica di un task. Verifica che il
     * timeStamp sia uguale a quello dell'ultima versione, in caso contrario
     * invia un messaggio di errato aggiornamento corredato dal task aggiornato.
     *
     * @param request oggetto contenente la richiesta di modifica del task.
     * @return response che contiene il task aggiornato.
     */
    public EditTaskResponse editTask(EditTaskRequest request) {
        EditTaskResponse response = new EditTaskResponse();
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

                response.setTask(task);
                response.setError(false);
                response.setErrorMessage("");
            } else {
                response.setTask(task);
                response.setError(true);
                response.setErrorMessage("Aggiornamento fallito");
                return response;
            }
        } else {
            response.setError(true);
            response.setErrorMessage("Aggiornamento fallito");
        }

        updateXML();
        return response;
    }

    /**
     * Metodo invocato per effettuare la cancellazione del task. Come per
     * l'editTask si verifica che il il timeStamp sia uguale a quello
     * dell'ultima versione, in caso contrario invia un messaggio di errato
     * aggiornamento.
     *
     * @param request rappresenta la richiesta di cancellazione del task da parte del client.
     * @return response che contiene il task eliminato.
     */
    public DeleteTaskResponse deleteTask(DeleteTaskRequest request) {
        DeleteTaskResponse viewModel = new DeleteTaskResponse();

        Task task = getTaskById(request.getId());

        if (task != null) {
            if (task.getTimeStamp() == request.getTimeStamp()) {

                items.getList().remove(task);

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

        updateXML();
        return viewModel;
    }
}
