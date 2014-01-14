package asw1009.controller;

import asw1009.ManageXML;
import asw1009.model.UsersManager;
import asw1009.model.entities.User;
import asw1009.requests.EditUserRequest;
import asw1009.requests.LoginRequest;
import asw1009.requests.SignUpRequest;
import asw1009.responses.BaseResponse;
import asw1009.responses.EditUserResponse;
import asw1009.responses.LoginResponse;
import asw1009.responses.UsersListResponse;
import com.google.gson.Gson;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * Classe che definisce tutti i servizi legati alla gestione degli utenti
 *
 * @author ASW1009
 */
@WebServlet(urlPatterns = {"/users/*"})
public class Users extends HttpServlet {

    private final String ACTION_LOGOUT = "logout";
    private final String ACTION_LOGIN = "login";
    private final String ACTION_SIGNUP = "signup";
    private final String ACTION_EDITUSER = "edituser";
    private final String ACTION_USERS = "users";

    /**
     * Metodo che definisce il comportamento dell'applicazione a seguito di
     * chiamate di tipo POST al server. Il metodo decodifica, per ogni serivzio
     * chiamato, i dati inviati tramite una variabile di tipo gson e richiama i
     * singoli metodi che si occupano di eseguire il servizio in questione
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
            Gson gson = new Gson();
            response.setContentType("application/json;");

            String action = request.getPathInfo().replace("/", "");
            String jsonResponse = "";
            switch (action) {
                case ACTION_SIGNUP: {
                    SignUpRequest requestData = gson.fromJson(json, SignUpRequest.class);
                    jsonResponse = gson.toJson(signUp(requestData), BaseResponse.class);
                    break;
                }
                case ACTION_LOGOUT: {
                    BaseResponse response_logout = new BaseResponse();
                    response_logout.setError(false);
                    session.setAttribute("user", null);
                    jsonResponse = gson.toJson(response_logout, BaseResponse.class);
                    break;
                }
                case ACTION_EDITUSER: {
                    EditUserRequest requestData = gson.fromJson(json, EditUserRequest.class);
                    EditUserResponse response_edit = editUser(requestData);
                    
                    if(!response_edit.hasError()){
                        response_edit.getLoggedUser().setPassword("");
                    }
                    session.setAttribute("user", gson.toJson(response_edit.getLoggedUser(), User.class));
                    jsonResponse = gson.toJson(response_edit, EditUserResponse.class);
                    break;
                }
                case ACTION_USERS: {
                    jsonResponse = gson.toJson(usersList(), UsersListResponse.class);
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
                    LoginRequest loginRequestViewModel = new LoginRequest();
                    loginRequestViewModel.setUsername(root.getElementsByTagName("username").item(0).getTextContent());
                    loginRequestViewModel.setPassword(root.getElementsByTagName("password").item(0).getTextContent());
                    loginRequestViewModel.setRemember(Boolean.parseBoolean(root.getElementsByTagName("remember").item(0).getTextContent()));
                    LoginResponse loginResponseViewModel = login(loginRequestViewModel);

                    if (loginRequestViewModel.getRemember()) {
                        Cookie c_username = new Cookie("username", loginRequestViewModel.getUsername());
                        Cookie c_password = new Cookie("password", loginRequestViewModel.getPassword());
                        c_password.setMaxAge(60*60*24);
                        c_username.setMaxAge(60*60*24);
                        c_username.setPath("/");
                        c_password.setPath("/");
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

    /**
     * Metodo che invoca il servizio di registrazione all'applicazione chiamando
     * UserManager, situato all'interno del model, il quale si occupa di
     * effettuare il controllo dei dati
     *
     * @param data Oggetto SignUpRequest che contine i dati inseriti
 dall'utente
     * @return Oggetto BaseResponse che contiene tutti i dati del nuovo
 utente creato
     */
    private BaseResponse signUp(SignUpRequest data) {
        BaseResponse response = new BaseResponse();
        if (data != null) {
            response = UsersManager.getInstance().signUp(data);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }
        return response;
    }

    /**
     * Metodo che invoca il servizio di login chiamando UserManager, situato
     * all'inerno del model, il quale si occupa di effettuare il controllo dei
     * dati
     *
     * @param data Oggetto LoginRequest che contine i dati inseriti
 dall'utente
     * @return Oggetto LoginResponse che contiene i dati dell'utente
 appena loggato
     */
    private LoginResponse login(LoginRequest data) {
        LoginResponse response = new LoginResponse();
        if (data != null) {

            response = UsersManager.getInstance().login(data);

        } else {
            response.setError(true);
            response.setLoggedUser(null);//TODO
            response.setErrorMessage("Invalid data");
        }

        //return JSON string
        return response;
    }

    /**
     * Metodo che invoca il servizio di modifica dei dati dell'utente chiamando
     * UserManager, situato all'interno del model, il quale si occupa di
     * effettuare la modifica dei dati
     *
     * @param request Oggetto EditUserRequest che contine i dati
 inseriti dall'utente
     * @return Oggetto EditUserResponse che contiene i dati dell'utente
 appena modificato
     */
    private EditUserResponse editUser(EditUserRequest request) {
        EditUserResponse response = new EditUserResponse();
        if (request != null) {
            response = UsersManager.getInstance().editUser(request);
        } else {
            response.setError(true);
            response.setErrorMessage("Invalid data");
        }
        return response;
    }

    private UsersListResponse usersList() {
        UsersListResponse response = UsersManager.getInstance().usersList();
        response.setError(false);
        return response;
    }

}
