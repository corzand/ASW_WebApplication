package asw1009;

import asw1009.models.User;
import asw1009.models.request.LoginRequestViewModel;
import asw1009.models.request.SignUpRequestViewModel;
import asw1009.models.response.BaseResponseViewModel;
import asw1009.models.response.LoginResponseViewModel;
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

    //Define servlet actions
    private final String ACTION_LOGIN = "login";
    private final String ACTION_SIGNUP = "signup";

    private void forward(HttpServletRequest request, HttpServletResponse response, String page)
            throws ServletException, IOException {
        ServletContext sc = getServletContext();
        RequestDispatcher rd = sc.getRequestDispatcher(page);
        rd.forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String action = request.getPathInfo().replace("/", "");
        if(action.equals(ACTION_LOGIN)){
            System.out.println("forwarding...");
            forward(request, response, "/login.jsp");
        }else if (action.equals(ACTION_SIGNUP)){
            System.out.println("forwarding...");
            forward(request, response, "/signup.jsp");
        }        
    }

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

            if (action.equals(ACTION_LOGIN)) {
                LoginRequestViewModel requestData = gson.fromJson(json, LoginRequestViewModel.class);
                jsonResponse = gson.toJson(login(requestData), LoginResponseViewModel.class);
            }else if (action.equals(ACTION_SIGNUP)){
                SignUpRequestViewModel requestData = gson.fromJson(json, SignUpRequestViewModel.class);
                jsonResponse = gson.toJson(signUp(requestData), BaseResponseViewModel.class);
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

                    answer = mngXML.newDocument();

                    root = answer.createElement("login");

                    Element hasError = answer.createElement("hasError");
                    hasError.setTextContent("" + loginResponseViewModel.isError());

                    Element errorMessage = answer.createElement("errorMessage");
                    errorMessage.setTextContent(loginResponseViewModel.getErrorMessage());

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

                    root.appendChild(hasError);
                    root.appendChild(errorMessage);
                    root.appendChild(loggedUser);

                    answer.appendChild(root);
                }

                mngXML.transform(os, answer);
                os.close();
            } catch (ParserConfigurationException e) {
                System.out.println(e);
            } catch (IOException e) {
                System.out.println(e);
            } catch (SAXException e) {
                System.out.println(e);
            } catch (DOMException e) {
                System.out.println(e);
            } catch (TransformerException e) {
                System.out.println(e);
            }
        }

    }
    
    private BaseResponseViewModel signUp(SignUpRequestViewModel data){
        //fa cose --> chiama il model, registra etc
        BaseResponseViewModel responseViewModel = new BaseResponseViewModel();
        responseViewModel.setError(false);
        return responseViewModel;
    }

    private LoginResponseViewModel login(LoginRequestViewModel data) {
        LoginResponseViewModel response = new LoginResponseViewModel();
        if (data != null) {
            System.out.println(data.getUsername());
            System.out.println(data.getPassword());

            //Validate login
            //...
            response.setError(false);
            response.setLoggedUser(new User());//TODO
            response.setErrorMessage("");
        } else {
            response.setError(true);
            response.setLoggedUser(null);//TODO
            response.setErrorMessage("Invalid data");
        }

        //return JSON string
        return response;
    }

}
