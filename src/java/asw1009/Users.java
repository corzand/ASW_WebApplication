package asw1009;

import asw1009.models.User;
import asw1009.models.request.LoginRequestViewModel;
import asw1009.models.response.LoginResponseViewModel;
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

    //Define servlet actions
    private final String ACTION_LOGIN = "login";

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
                    loginRequestViewModel.username = root.getElementsByTagName("username").item(0).getTextContent();
                    loginRequestViewModel.password = root.getElementsByTagName("password").item(0).getTextContent();

                    LoginResponseViewModel loginResponseViewModel = login(loginRequestViewModel);

                    answer = mngXML.newDocument();

                    root = answer.createElement("login");
                    
                    Element hasError = answer.createElement("hasError");
                    hasError.setTextContent("" + loginResponseViewModel.hasError);

                    Element errorMessage = answer.createElement("errorMessage");
                    errorMessage.setTextContent(loginResponseViewModel.errorMessage);

                    Element loggedUser = answer.createElement("loggedUser");

                    Element id = answer.createElement("id");
                    id.setTextContent(loginResponseViewModel.loggedUser.id + "");
                    Element firstName = answer.createElement("firstName");
                    firstName.setTextContent(loginResponseViewModel.loggedUser.firstName);
                    Element lastName = answer.createElement("lastName");
                    lastName.setTextContent(loginResponseViewModel.loggedUser.lastName);
                    Element username = answer.createElement("username");
                    username.setTextContent(loginResponseViewModel.loggedUser.username);
                    Element email = answer.createElement("email");
                    email.setTextContent(loginResponseViewModel.loggedUser.email);
                    Element pictureUrl = answer.createElement("pictureUrl");
                    pictureUrl.setTextContent(loginResponseViewModel.loggedUser.pictureUrl);

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

    private LoginResponseViewModel login(LoginRequestViewModel data) {
        LoginResponseViewModel response = new LoginResponseViewModel();
        if (data != null) {
            System.out.println(data.username);
            System.out.println(data.password);

            //Validate login
            //...
            response.hasError = false;
            response.loggedUser = new User();
            response.errorMessage = "";
        } else {
            response.hasError = true;
            response.loggedUser = null;
            response.errorMessage = "Invalid data.";
        }

        //return JSON string
        return response;
    }

}
