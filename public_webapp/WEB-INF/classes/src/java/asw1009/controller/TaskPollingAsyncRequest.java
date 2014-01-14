package asw1009.controller;

import asw1009.requests.PollingRequest;
import javax.servlet.AsyncContext;

/**
 * Classe che definisce la struttura di una richiesta http asincrona
 *
 * @author ASW1009
 */
public class TaskPollingAsyncRequest {

    private AsyncContext context;
    private PollingRequest requestViewModel;
    private String sessionId;

    /**
     * Costruttore della classe
     *
     *
     * @param context Oggetto AsyncContext contiene il context della richiesta
     * @param requestViewModel Oggetto requestViewModel che contiene i dati
     * della richiesta http
     * @param sessionId Stringa contenente l'id della sessione
     */
    public TaskPollingAsyncRequest(AsyncContext context, PollingRequest requestViewModel, String sessionId) {
        this.context = context;
        this.sessionId = sessionId;
        this.requestViewModel = requestViewModel;
    }

    /**
     * Metodo che restituisce il context di una richiesta istanziata
     *
     * @return Oggetto AsyncContext che contiene il context della richiesta
     */
    public AsyncContext getContext() {
        return this.context;
    }

    /**
     * Metodo che restituisce il requestViewModel
     *
     * @return Oggetto requestViewModel che contiene i dati della richiesta http
     */
    public PollingRequest getRequestViewModel() {
        return this.requestViewModel;
    }

    /**
     * Metodo che restituisce l'id della sessione
     *
     * @return Stringa contenente l'id della sessione
     */
    public String getSessionId() {
        return this.sessionId;
    }
}
