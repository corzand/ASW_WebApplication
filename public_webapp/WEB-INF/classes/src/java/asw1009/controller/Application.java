package asw1009.controller;

import asw1009.model.CategoriesManager;
import asw1009.model.TasksManager;
import asw1009.model.UsersManager;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Controller per tutte le chiamate con URL /application/*. 
 * Intercetta la richieste GET del client, seleziona la View ed effettua il forwarding.
 *
 * @author ASW1009
 */
@WebServlet(urlPatterns = {"/application/*"})
public class Application extends HttpServlet {

    /**
     * Inizializzazione dei vari Manager singleton  adibiti alla gestione del model
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        super.init();
        UsersManager.getInstance().init(getServletContext().getRealPath("/"), "Users");
        CategoriesManager.getInstance().init(getServletContext().getRealPath("/"), "Categories");
        TasksManager.getInstance().init(getServletContext().getRealPath("/"), "Tasks");
    }

    //Define servlet actions
    private final String ACTION_LOGIN = "login";
    private final String ACTION_SIGNUP = "signup";
    private final String ACTION_TASKS = "tasks";
    private final String ACTION_USER = "user";

    /**
     * Metodo invocato per inoltrare la richiesta http del client alla view 
     * opportunamente selzionata dal controller
     *
     * @param request Oggetto HttpServletRequest che contine i dati della
     * richiesta http
     * @param response Oggetto HttpServletResponse che contine i dati della
     * risposta http
     * @param page Stringa che contiene il titolo della view selezionata
     * @throws ServletException
     * @throws IOException
     */
    private void forward(HttpServletRequest request, HttpServletResponse response, String page)
            throws ServletException, IOException {
        ServletContext sc = getServletContext();
        RequestDispatcher rd = sc.getRequestDispatcher(page);
        rd.forward(request, response);
    }

    /**
     * Metodo richiamato su ogni richiesta GET. 
     * Il controller effettua view selection e forwarding alla relativa jsp
     *
     * @param request Oggetto HttpServletRequest che contine i dati della
     * richiesta http
     * @param response Oggetto HttpServletResponse che contine i dati della
     * risposta http
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String action = request.getPathInfo().replace("/", "");
        switch (action) {
            case ACTION_LOGIN:
                forward(request, response, "/index.jsp");
                break;
            case ACTION_SIGNUP:
                forward(request, response, "/jsp/signup.jsp");
                break;
            case ACTION_TASKS:
                forward(request, response, "/jsp/tasks.jsp");
                break;
            case ACTION_USER:
                forward(request, response, "/jsp/user.jsp");
                break;
        }
    }
}
