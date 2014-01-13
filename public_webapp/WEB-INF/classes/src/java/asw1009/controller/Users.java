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
import asw1009.viewmodel.response.UsersListResponseViewModel;
import com.google.gson.Gson;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

@WebServlet(urlPatterns = {"/users/*"})
public class Users extends HttpServlet {

    private final String ACTION_LOGOUT = "logout";
    private final String ACTION_LOGIN = "login";
    private final String ACTION_SIGNUP = "signup";
    private final String ACTION_EDITUSER = "edituser";
    private final String ACTION_USERS = "users";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (request.getContentType().contains("application/json")) {
            //JSON over HTTP

            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String json = br.readLine();
            Gson gson = new Gson();
            response.setContentType("application/json;");

            String action = request.getPathInfo().replace("/", "");
            String jsonResponse = "";
            switch (action) {
                case ACTION_SIGNUP: {
                    SignUpRequestViewModel requestData = gson.fromJson(json, SignUpRequestViewModel.class);
                    jsonResponse = gson.toJson(signUp(requestData), BaseResponseViewModel.class);
                    break;
                }
                case ACTION_LOGOUT: {
                    BaseResponseViewModel response_logout = new BaseResponseViewModel();
                    response_logout.setError(false);
                    session.setAttribute("user", null);
                    jsonResponse = gson.toJson(response_logout, BaseResponseViewModel.class);
                    break;
                }
                case ACTION_EDITUSER: {
                    EditUserRequestViewModel requestData = gson.fromJson(json, EditUserRequestViewModel.class);
                    EditUserResponseViewModel response_edit = editUser(requestData);
                    response_edit.getLoggedUser().setPassword("");
                    session.setAttribute("user", gson.toJson(response_edit.getLoggedUser(), User.class));
                    jsonResponse = gson.toJson(response_edit, EditUserResponseViewModel.class);
                    break;
                }
                case ACTION_USERS: {
                    jsonResponse = gson.toJson(usersList(), UsersListResponseViewModel.class);
                    break;
                }
            }
            response.getOutputStream().print(jsonResponse);
            response.getOutputStream().flush();

        } else {
            //XML Over HTTP
            InputStream is = request.getInputStream();
            response.setContentType("text/xml;charset=UTF-8");
            OutputStream os = response.getOutputStream();
            try {
                ManageXML mngXML = new ManageXML();
                Document data = mngXML.parse(is);
                is.close();

                Element root = data.getDocumentElement();
                String action = root.getTagName();
                Document answer = null;

                if (action.equals(ACTION_LOGIN)) {
                    LoginRequestViewModel loginRequestViewModel = new LoginRequestViewModel();
                    loginRequestViewModel.setUsername(root.getElementsByTagName("username").item(0).getTextContent());
                    loginRequestViewModel.setPassword(root.getElementsByTagName("password").item(0).getTextContent());

                    LoginResponseViewModel loginResponseViewModel = login(loginRequestViewModel);

                    if (root.getElementsByTagName("remember").item(0).getTextContent().equals("true")) {
                        Cookie c_username = new Cookie("username", loginRequestViewModel.getUsername());
                        Cookie c_password = new Cookie("password", loginRequestViewModel.getPassword());
                        c_password.setMaxAge(-1);
                        c_username.setMaxAge(-1);
                        response.addCookie(c_username);
                        response.addCookie(c_password);
                    }

                    answer = mngXML.newDocument();

                    root = answer.createElement("login");

                    Element hasError = answer.createElement("hasError");
                    hasError.setTextContent("" + loginResponseViewModel.hasError());

                    Element errorMessage = answer.createElement("errorMessage");
                    errorMessage.setTextContent(loginResponseViewModel.getErrorMessage());

                    if (!loginResponseViewModel.hasError()) {
                        Element loggedUser = answer.createElement("loggedUser");
                        Element id = answer.createElement("id");
                        id.setTextContent(loginResponseViewModel.getLoggedUser().getId() + "");
                        Element firstName = answer.createElement("firstName");
                        firstName.setTextContent(loginResponseViewModel.getLoggedUser().getFirstName());
                        Element lastName = answer.createElement("lastName");
                        lastName.setTextContent(loginResponseViewModel.getLoggedUser().getLastName());
                        Element username = answer.createElement("username");
                        username.setTextContent(loginResponseViewModel.getLoggedUser().getUsername());
                        Element email = answer.createElement("email");
                        email.setTextContent(loginResponseViewModel.getLoggedUser().getEmail());
                        Element pictureUrl = answer.createElement("pictureUrl");
                        pictureUrl.setTextContent(loginResponseViewModel.getLoggedUser().getPicture());

                        loggedUser.appendChild(id);
                        loggedUser.appendChild(firstName);
                        loggedUser.appendChild(lastName);
                        loggedUser.appendChild(username);
                        loggedUser.appendChild(email);
                        loggedUser.appendChild(pictureUrl);
                        root.appendChild(loggedUser);
                    }
                    root.appendChild(hasError);
                    root.appendChild(errorMessage);

                    answer.appendChild(root);

                    loginRequestViewModel.setPassword("");

                    if (!loginResponseViewModel.hasError()) {
                        Gson gson = new Gson();
                        session.setAttribute("user", gson.toJson(loginResponseViewModel.getLoggedUser(), User.class));
                    }

                }

                mngXML.transform(os, answer);
                os.close();
            } catch (ParserConfigurationException | IOException | SAXException | DOMException | TransformerException e) {
                System.out.println(e);
            }
        }

    }

    private BaseResponseViewModel signUp(SignUpRequestViewModel data) {
        //fa cose --> chiama il model, registra etc
        BaseResponseViewModel response = new BaseResponseViewModel();
        if (data != null) {
            response = UsersManager.getInstance()._signUp(data);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }
        return response;
    }

    private LoginResponseViewModel login(LoginRequestViewModel data) {
        LoginResponseViewModel response = new LoginResponseViewModel();
        if (data != null) {

            response = UsersManager.getInstance()._login(data);

        } else {
            response.setError(true);
            response.setLoggedUser(null);//TODO
            response.setErrorMessage("Invalid data");
        }

        //return JSON string
        return response;
    }

    private EditUserResponseViewModel editUser(EditUserRequestViewModel request) {
        EditUserResponseViewModel response = new EditUserResponseViewModel();
        if (request != null) {
            response = UsersManager.getInstance()._editUser(request);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }
        return response;
    }

    private UsersListResponseViewModel usersList() {
        UsersListResponseViewModel response = UsersManager.getInstance().usersList();
        response.setError(false);
        return response;
    }

}
