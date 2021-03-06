package asw1009.controller;

import asw1009.ManageXML;
import asw1009.model.UsersManager;
import asw1009.requests.EditUserRequest;
import asw1009.requests.LoginRequest;
import asw1009.requests.SignUpRequest;
import asw1009.responses.BaseResponse;
import asw1009.responses.EditUserResponse;
import asw1009.responses.LoginResponse;
import asw1009.responses.UsersListResponse;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * Controller che espone i servizi relativi all'entità Utente. Intercetta tutte
 * le chiamate che hanno come url pattern /users/
 *
 * @author ASW1009
 */
@WebServlet(urlPatterns = {"/users/*"})
public class Users extends HttpServlet {

    //Operations che possono essere richiamate dal client
    private final String ACTION_LOGOUT = "logout";
    private final String ACTION_LOGIN = "login";
    private final String ACTION_SIGNUP = "signup";
    private final String ACTION_EDITUSER = "edituser";
    private final String ACTION_USERS = "users";

    /**
     * Metodo privato per poter fare l'unmarshalling di una Stringa JSON in un
     * oggetto di tipo classOfT
     * @param json stringa json da deserializzare
     * @param classOfT oggetto che vogliamo ottenere
     * @return oggetto derivato dall'unmarshalling
     */
    private Object unmarshall(String json, Class classOfT) {
        try {
            JAXBContext jc = JAXBContext.newInstance(classOfT);
            Unmarshaller um = jc.createUnmarshaller();
            JSONObject obj = new JSONObject(json);
            XMLStreamReader xmlStreamReader = new MappedXMLStreamReader(obj);
            return um.unmarshal(xmlStreamReader);
        } catch (JAXBException | JSONException | XMLStreamException ex) {
            Logger.getLogger(Tasks.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Metodo privato per fare marshalling di un oggetto in JSON
     * @param instance oggetto da serializzare
     * @return stringa serializzata
     */
    private String marshall(Object instance) {
        try {
            JAXBContext jc = JAXBContext.newInstance(instance.getClass());
            Marshaller m = jc.createMarshaller();
            MappedNamespaceConvention mnc = new MappedNamespaceConvention();
            StringWriter writer = new StringWriter();
            XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(mnc, writer);
            m.marshal(instance, xmlStreamWriter);
            return writer.toString();
        } catch (JAXBException ex) {
            Logger.getLogger(Tasks.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }

    /**
     * Metodo del controller che intercetta le chiamate di tipo POST. L'ultima
     * parte dell'URL di chiamata al servizio, specifica il nome dell'operation
     * che il client richiede. Alcune operations prevedono uno scambio di dati
     * con il client sotto forma di stringhe JSON, mentre l'operation di login
     * richiede XML over HTTP. Il controller, in questo caso, seleziona le
     * operations da eseguire e risponde al client in modo sincrono.
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

            response.setContentType("application/json;");

            //Action che il client vuole eseguire! Corrisponde all'operation selezionata
            String action = request.getPathInfo().replace("/", "");

            String jsonResponse = "";

            //Operation selection
            switch (action) {
                case ACTION_SIGNUP: {
                    SignUpRequest requestData = (SignUpRequest) unmarshall(json, SignUpRequest.class);
                    jsonResponse = marshall(signUp(requestData));
                    break;
                }
                case ACTION_LOGOUT: {
                    BaseResponse response_logout = new BaseResponse();
                    response_logout.setError(false);
                    session.setAttribute("user", null);
                    jsonResponse = marshall(response_logout);
                    break;
                }
                case ACTION_EDITUSER: {
                    EditUserRequest requestData = (EditUserRequest) unmarshall(json, EditUserRequest.class);
                    EditUserResponse response_edit = editUser(requestData);

                    //Per ragioni di sicurezza, togliamo la password dalle informazioni legate all'utente
                    //che andiamo a salvare in sessione
                    if (!response_edit.hasError()) {
                        response_edit.getLoggedUser().setPassword("");
                    }
                    session.setAttribute("user", marshall(response_edit.getLoggedUser()));
                    jsonResponse = marshall(response_edit);
                    break;
                }
                case ACTION_USERS: {
                    jsonResponse = marshall(usersList());
                    break;
                }
            }

            //Si invia al client la risposta
            response.getOutputStream().print(jsonResponse);
            response.getOutputStream().flush();

        } else {
            //XML Over HTTP

            //Serializzazione/deserializzazione manuale
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

                //L'unica action che il client può invocare via XML/POST è quella di Login
                if (action.equals(ACTION_LOGIN)) {

                    //Creazione dell'oggetto di richiesta
                    LoginRequest loginRequest = new LoginRequest();
                    loginRequest.setUsername(root.getElementsByTagName("username").item(0).getTextContent());
                    loginRequest.setPassword(root.getElementsByTagName("password").item(0).getTextContent());
                    loginRequest.setRemember(Boolean.parseBoolean(root.getElementsByTagName("remember").item(0).getTextContent()));

                    //do operation
                    LoginResponse loginResponse = login(loginRequest);

                    //Setto i cookie con le credenziali, se l'utente richiede il flag "ricordami"
                    if (loginRequest.getRemember()) {
                        Cookie c_username = new Cookie("username", loginRequest.getUsername());
                        Cookie c_password = new Cookie("password", loginRequest.getPassword());
                        c_password.setMaxAge(60 * 60 * 24);
                        c_username.setMaxAge(60 * 60 * 24);
                        c_username.setPath("/");
                        c_password.setPath("/");
                        response.addCookie(c_username);
                        response.addCookie(c_password);
                    }

                    //Prepara l'xml da inviare al client
                    answer = mngXML.newDocument();

                    root = answer.createElement("login");

                    Element hasError = answer.createElement("hasError");
                    hasError.setTextContent("" + loginResponse.hasError());

                    Element errorMessage = answer.createElement("errorMessage");
                    errorMessage.setTextContent(loginResponse.getErrorMessage());

                    if (!loginResponse.hasError()) {
                        Element loggedUser = answer.createElement("loggedUser");
                        Element id = answer.createElement("id");
                        id.setTextContent(loginResponse.getLoggedUser().getId() + "");
                        Element firstName = answer.createElement("firstName");
                        firstName.setTextContent(loginResponse.getLoggedUser().getFirstName());
                        Element lastName = answer.createElement("lastName");
                        lastName.setTextContent(loginResponse.getLoggedUser().getLastName());
                        Element username = answer.createElement("username");
                        username.setTextContent(loginResponse.getLoggedUser().getUsername());
                        Element email = answer.createElement("email");
                        email.setTextContent(loginResponse.getLoggedUser().getEmail());
                        Element pictureUrl = answer.createElement("pictureUrl");
                        pictureUrl.setTextContent(loginResponse.getLoggedUser().getPicture());

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

                    loginRequest.setPassword("");

                    //user serializzato in JSON viene inserito in sessione.
                    //Verrà poi deserializzato dal fragment top.jspf e inserito in un campo javascript
                    if (!loginResponse.hasError()) {
                        session.setAttribute("user", marshall(loginResponse.getLoggedUser()));
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
     * Operation che offre il servizio di registrazione utente. L'istanza di
     * model utilizzata è UsersManager, che si occupa di effettuare il controllo
     * dei dati e restituire una risposta all'utente che ha richiesto la
     * registrazione
     *
     * @param data Oggetto SignUpRequest che contine i dati inseriti dall'utente
     * @return Oggetto BaseResponse che contiene tutti i dati del nuovo utente
     * creato
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
     * Metodo che invoca il servizio di login chiamando UsersManager, situato
     * all'inerno del model, il quale si occupa di effettuare il controllo dei
     * dati
     *
     * @param data Oggetto LoginRequest che contine i dati inseriti dall'utente
     * @return Oggetto LoginResponse che contiene i dati dell'utente appena
     * loggato
     */
    private LoginResponse login(LoginRequest data) {
        LoginResponse response = new LoginResponse();
        if (data != null) {

            response = UsersManager.getInstance().login(data);

        } else {
            response.setError(true);
            response.setLoggedUser(null);
            response.setErrorMessage("Invalid data");
        }

        return response;
    }

    /**
     * Operation che invoca la modifica dei dati dell'utente chiamando il model
     * attraverso UsersManager, il quale si occupa di effettuare la modifica dei
     * dati
     *
     * @param request Oggetto EditUserRequest che contine i dati inseriti
     * dall'utente
     * @return Oggetto EditUserResponse che contiene i dati dell'utente
     * modificato
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

    /**
     * Operation invocata per ottenere la lista degli utenti che fanno parte
     * dell'applicazione, senza le informazioni personali
     *
     * @return la lista degli utenti.
     */
    private UsersListResponse usersList() {
        UsersListResponse response = UsersManager.getInstance().usersList();
        response.setError(false);
        return response;
    }

}
