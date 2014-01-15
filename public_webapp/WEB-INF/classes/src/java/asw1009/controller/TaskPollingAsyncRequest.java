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
    private PollingRequest request;
    private String sessionId;

    /**
     * Oggetto utilizzato nella lista di richieste di polling, che memorizza,
     * oltre all'asyncContext relativo alla richiesta, anche i dati della richiesta
     * e l'id di sessione.
     *
     *
     * @param context Oggetto AsyncContext contiene il context della richiesta
     * @param request Oggetto request che contiene i dati della richiesta che il client ha inviato
     * @param sessionId Stringa contenente l'id della sessione
     */
    public TaskPollingAsyncRequest(AsyncContext context, PollingRequest request, String sessionId) {
        this.context = context;
        this.sessionId = sessionId;
        this.request = request;
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
     * Metodo che restituisce il request
     *
     * @return Oggetto request che contiene i dati della richiesta http
     */
    public PollingRequest getRequest() {
        return this.request;
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
