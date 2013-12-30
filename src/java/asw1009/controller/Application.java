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
public class Application extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        UsersManager.getInstance().init(getServletContext().getRealPath("/data/"), "Users");
        CategoriesManager.getInstance().init(getServletContext().getRealPath("/data/"), "Categories");
        TasksManager.getInstance().init(getServletContext().getRealPath("/data/"), "Tasks");
    }

    //Define servlet actions
    private final String ACTION_LOGIN = "login";
    private final String ACTION_SIGNUP = "signup";
    private final String ACTION_TASKS = "tasks";
    private final String ACTION_USER = "user";

    private void forward(HttpServletRequest request, HttpServletResponse response, String page)
            throws ServletException, IOException {
        ServletContext sc = getServletContext();
        RequestDispatcher rd = sc.getRequestDispatcher(page);
        rd.forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //HttpSession session = request.getSession();
        String action = request.getPathInfo().replace("/", "");
        switch (action) {
            case ACTION_LOGIN:
                forward(request, response, "/view/login.jsp");
                break;
            case ACTION_SIGNUP:
                forward(request, response, "/view/signup.jsp");
                break;
            case ACTION_TASKS:
                forward(request, response, "/view/tasks.jsp");
                break;
            case ACTION_USER:
                forward(request, response, "/view/user.jsp");
                break;
                
        }
    }
}
