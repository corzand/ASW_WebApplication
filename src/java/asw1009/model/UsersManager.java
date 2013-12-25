/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import asw1009.model.entities.User;
import asw1009.viewmodel.request.LoginRequestViewModel;
import asw1009.viewmodel.request.SignUpRequestViewModel;
import asw1009.viewmodel.response.BaseResponseViewModel;
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

    public BaseResponseViewModel signUp(SignUpRequestViewModel request) {
        BaseResponseViewModel viewModel = new BaseResponseViewModel();

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
}
