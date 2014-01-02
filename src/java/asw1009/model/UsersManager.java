/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import org.apache.commons.codec.binary.Base64;
import asw1009.model.entities.User;
import asw1009.viewmodel.request.EditUserRequestViewModel;
import asw1009.viewmodel.request.LoginRequestViewModel;
import asw1009.viewmodel.request.SignUpRequestViewModel;
import asw1009.viewmodel.response.BaseResponseViewModel;
import asw1009.viewmodel.response.EditUserResponseViewModel;
import asw1009.viewmodel.response.LoginResponseViewModel;
import asw1009.viewmodel.response.UsersListResponseViewModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UsersManager extends FileManager {

    //private List<User> users;
    private EntityList<User> _users;
    private static UsersManager instance;

    public UsersManager() {
        //users = new ArrayList<>();
        _users = new EntityList<>();
    }

    @Override
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName); //To change body of generated methods, choose Tools | Templates.

        _xstream.alias("user", User.class);
        _xstream.alias("users", EntityList.class);
        _xstream.addImplicitCollection(EntityList.class, "list");
        _readXML();
        //Eventualmente, leggere il contenuto del file Users.xml e impostare gli oggetti in memoria.
    }

    public static synchronized UsersManager getInstance() {
        if (instance == null) {
            instance = new UsersManager();
        }

        return instance;
    }

    public void addUser(User user) {
        //crea l'XML e scrivilo
//        users.add(user);
        user.setId(_users.getNextId());
        _users.getItems().add(user);

        _updateXML();
    }

    private User _getUserByUsername(String username) {

//        for (int i = 0; i < users.size(); i++) {
//            User user = users.get(i);
//            if (user.getUsername().equals(username)) {
//                return user;
//            }
//        }
        for (int i = 0; i < _users.getItems().size(); i++) {
            User user = _users.getItems().get(i);
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }

    private void _readXML() {
        if (xml.exists()) {
            _users = (EntityList<User>) readXML();
        } else {
            _users = new EntityList<>();
        }
    }

    private void _updateXML() {
        writeXML(_xstream.toXML(_users));
    }

    public BaseResponseViewModel _signUp(SignUpRequestViewModel request) {
        BaseResponseViewModel viewModel = new BaseResponseViewModel();

        if (_getUserByUsername(request.getUsername()) == null) {
            User userToAdd = new User();
            userToAdd.setEmail(request.getEmail());
            userToAdd.setFirstName(request.getFirstName());
            userToAdd.setLastName(request.getLastName());
            userToAdd.setUsername(request.getUsername());
            userToAdd.setPassword(request.getPassword());
            addUser(userToAdd);

            viewModel.setError(false);

        } else {
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
            userToAdd.setPassword(request.getPassword());

            addItem(userToAdd, userToAdd.getClass());
            viewModel.setError(false);
        }

        _updateXML();
        return viewModel;
    }

    public LoginResponseViewModel _login(LoginRequestViewModel request) {
        LoginResponseViewModel viewModel = new LoginResponseViewModel();
        User user = _getUserByUsername(request.getUsername());

        if (user != null && user.getPassword().equals(request.getPassword())) {
            viewModel.setError(false);
            viewModel.setErrorMessage("");
            viewModel.setLoggedUser(user);
        } else {
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

        if (user != null) {
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());

            //creare file immagine da stringa base64
            //settare nell'oggetto user.setPicture() il percorso al file
            
            if(!request.getPicture().equals("")) {
                String filePath = createImage(request.getPicture(), user.getId());
                user.setPicture(filePath);
            }

            viewModel.setLoggedUser(user);
            viewModel.setError(false);
            viewModel.setErrorMessage("");
        } else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Login failed");
        }

        _updateXML();
        return viewModel;
    }

    public UsersListResponseViewModel usersList() {
        UsersListResponseViewModel viewModel = new UsersListResponseViewModel();
        viewModel.setError(false);
        viewModel.setUsers(_users.getItems());
        return viewModel;
    }

    private String createImage(String base64, int id) {

        try {
            String[] splitted = base64.split(",");

            String extension = ".jpg";
            if (splitted[0].contains("gif")) {
                extension = ".gif";
            } else if (splitted[0].contains("png")) {
                extension = ".png";
            }

            // Converting a Base64 String into Image byte array
            byte[] imageByteArray = Base64.decodeBase64(splitted[1]);
            String filePath = "/files/users/" + id + extension;
            File file = new File(servletPath + filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (FileOutputStream imageOutFile = new FileOutputStream(
                    file)) {
                imageOutFile.write(imageByteArray);
            }
            return filePath;
        } catch (IOException ioe) {
            System.out.println("Exception while reading the Image " + ioe);
            return null;
        }

    }

}
