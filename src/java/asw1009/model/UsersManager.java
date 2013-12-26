/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import asw1009.model.entities.User;
import asw1009.viewmodel.request.EditUserRequestViewModel;
import asw1009.viewmodel.request.LoginRequestViewModel;
import asw1009.viewmodel.request.SignUpRequestViewModel;
import asw1009.viewmodel.response.BaseResponseViewModel;
import asw1009.viewmodel.response.EditUserResponseViewModel;
import asw1009.viewmodel.response.LoginResponseViewModel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrea
 */
public class UsersManager extends FileManager {

    private List<User> users;
    private int progId;
    private static UsersManager instance;

    public UsersManager() {
        users = new ArrayList<>();
        progId = 1;
    }

    @Override
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName); //To change body of generated methods, choose Tools | Templates.
        
        //Eventualmente, leggere il contenuto del file Users.xml e impostare gli oggetti in memoria.
    }
    
    

    public static synchronized UsersManager getInstance() {
        if (instance == null) {
            instance = new UsersManager();
        }

        return instance;
    }

    private int newProgId() {
        return ++progId;
    }

    public User getUser(String username, String password) {
        //Search a user and..
        return new User();
    }

    public void addUser(User user) {
        //crea l'XML e scrivilo
        users.add(user);
    }

    private User _getUserByUsername(String username){
        
        for (int i=0; i<users.size(); i++) {
            User user = users.get(i);
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        
        return null;
    }
    
    public BaseResponseViewModel _signUp(SignUpRequestViewModel request) {
        BaseResponseViewModel viewModel = new BaseResponseViewModel();
        
        if(_getUserByUsername(request.getUsername()) == null){
            User userToAdd = new User();
            userToAdd.setId(this.newProgId());
            userToAdd.setEmail(request.getEmail());
            userToAdd.setFirstName(request.getFirstName());
            userToAdd.setLastName(request.getLastName());
            userToAdd.setUsername(request.getUsername());
            userToAdd.setPassword(request.getPassword());
            addUser(userToAdd);
            
            viewModel.setError(false);
            
        }else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Questo username è già in uso");
        }
        
        return viewModel;
    }
    public BaseResponseViewModel signUp(SignUpRequestViewModel request) {
        BaseResponseViewModel viewModel = new BaseResponseViewModel();

        if (xml.exists()) {
            try {
                InputStream in = new FileInputStream(xml);
                Document document = xmlManager.parse(in);
                Element root = document.getDocumentElement();
                NodeList userNodes = root.getElementsByTagName("User");
                boolean found = false;
                for (int i = 0; i < userNodes.getLength() && !found; i++) {
                    Element user = (Element) userNodes.item(i);
                    if (user.getElementsByTagName("username").item(0).getTextContent().equals(request.getUsername())) {
                        found = true;
                    }
                }
                if (found) {
                    viewModel.setError(true);
                    viewModel.setErrorMessage("L'utente esiste già");
                } else {

                    User userToAdd = new User();
                    userToAdd.setEmail(request.getEmail());
                    userToAdd.setFirstName(request.getFirstName());
                    userToAdd.setLastName(request.getLastName());
                    userToAdd.setUsername(request.getUsername());
                    userToAdd.setId(this.newProgId());
                    userToAdd.setPicture("");
                    userToAdd.setPassword(request.getPassword());

                    addItem(userToAdd, userToAdd.getClass());
                    addUser(userToAdd);

                    viewModel.setError(false);
                }
            } catch (IOException | SAXException ex) {

            }
        } else {
            User userToAdd = new User();
            userToAdd.setEmail(request.getEmail());
            userToAdd.setFirstName(request.getFirstName());
            userToAdd.setLastName(request.getLastName());
            userToAdd.setUsername(request.getUsername());
            userToAdd.setId(this.newProgId());
            userToAdd.setPicture("");
            userToAdd.setPassword(request.getPassword());

            addItem(userToAdd, userToAdd.getClass());
            viewModel.setError(false);
        }

        return viewModel;
    }

    public LoginResponseViewModel _login(LoginRequestViewModel request) {
        LoginResponseViewModel viewModel = new LoginResponseViewModel();
        User user = _getUserByUsername(request.getUsername());
        
        if(user != null && user.getPassword().equals(request.getPassword())){
            viewModel.setError(false);  
            viewModel.setErrorMessage("");
            viewModel.setLoggedUser(user);
        }else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Login failed");
        }
        
        return viewModel;
    }
    
    public LoginResponseViewModel login(LoginRequestViewModel request) {
        LoginResponseViewModel viewModel = new LoginResponseViewModel();

        if (xml.exists()) {
            try {
                InputStream in = new FileInputStream(xml);
                Document document = xmlManager.parse(in);
                Element root = document.getDocumentElement();
                NodeList users = root.getElementsByTagName("User");
                boolean found = false;
                for (int i = 0; i < users.getLength() && !found; i++) {
                    Element user = (Element) users.item(i);
                    if (user.getElementsByTagName("username").item(0).getTextContent().equals(request.getUsername())) {
                        found = true;
                        if (user.getElementsByTagName("password").item(0).getTextContent().equals(request.getPassword())) {
                            viewModel.setError(false);
                            viewModel.setErrorMessage("");

                            User loggedUser = new User();
                            loggedUser.setFirstName(user.getElementsByTagName("firstName").item(0).getTextContent());
                            loggedUser.setLastName(user.getElementsByTagName("lastName").item(0).getTextContent());
                            loggedUser.setEmail(user.getElementsByTagName("email").item(0).getTextContent());
                            loggedUser.setId(Integer.parseInt(user.getElementsByTagName("id").item(0).getTextContent()));
                            loggedUser.setPicture(user.getElementsByTagName("picture").item(0).getTextContent());
                            loggedUser.setUsername(user.getElementsByTagName("username").item(0).getTextContent());

                            viewModel.setLoggedUser(loggedUser);
                        } else {
                            viewModel.setError(true);
                            viewModel.setErrorMessage("Password errata.");
                        }
                    }
                }
                if (!found) {
                    viewModel.setError(true);
                    viewModel.setErrorMessage("Utente inesistente");
                }
            } catch (IOException | SAXException ex) {

            }
        } else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Utente inesistente");
        }

        return viewModel;
    }
    
    public EditUserResponseViewModel _editUser(EditUserRequestViewModel request) {
        EditUserResponseViewModel viewModel = new EditUserResponseViewModel();
        User user = _getUserByUsername(request.getUsername());
        
        if(user != null){
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setPicture(request.getPicture());
            
            viewModel.setLoggedUser(user);
            viewModel.setError(false);  
            viewModel.setErrorMessage("");
        }else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Login failed");
        }
        
        return viewModel;
    }
}
