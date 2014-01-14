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

@WebServlet(urlPatterns = {"/application/*"})
/**
 * Classe che provvede definisce le chiamate ai servizi attraverso metodi di
 * tipo HttpServlet
 *
 * @author ASW1009
 */
public class Application extends HttpServlet {

    /**
     * Metodo che inizializza la classi che gestiscono i sevizi principali
     * dell'applicazione
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
     * Metodo che inoltra la richiesta http con i dati del servizio richiesto
     *
     * @param request Oggetto HttpServletRequest che contine i dati della
     * richiesta http
     * @param response Oggetto HttpServletResponse che contine i dati della
     * risposta http
     * @param page Stringa che contiene il titolo della pagina da invocare per
     * chiamare un servizio
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
     * Metodo che prepara la richiesta http da inoltrare impostando la pagina
     * jsp da chiamare
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
        //HttpSession session = request.getSession();
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
