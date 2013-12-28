package asw1009.controller;

import asw1009.ManageXML;
import asw1009.model.UsersManager;
import asw1009.model.entities.User;
import asw1009.viewmodel.request.EditUserRequestViewModel;
import asw1009.viewmodel.request.LoginRequestViewModel;
import asw1009.viewmodel.request.SignUpRequestViewModel;
import asw1009.viewmodel.response.BaseResponseViewModel;
import asw1009.viewmodel.response.EditUserResponseViewModel;
import asw1009.viewmodel.response.LoginResponseViewModel;
import com.google.gson.Gson;
import java.io.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

@WebServlet(urlPatterns = {"/application/*"})
public class Application extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        UsersManager.getInstance().init(getServletContext().getRealPath("/data/"), "Users");
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
                forward(request, response, "/View/login.jsp");
                break;
            case ACTION_SIGNUP:
                forward(request, response, "/View/signup.jsp");
                break;
            case ACTION_TASKS:
                forward(request, response, "/View/tasks.jsp");
                break;
            case ACTION_USER:
                forward(request, response, "/View/user.jsp");
                break;
                
        }
    }
}
