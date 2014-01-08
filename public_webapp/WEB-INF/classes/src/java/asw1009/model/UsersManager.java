
package asw1009.model;

import asw1009.model.entities.Task;
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
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UsersManager extends FileManager {

    private List<User> users;
    private static UsersManager instance;

    public UsersManager() {
        users = new ArrayList<>();
    }

    @Override
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName); //To change body of generated methods, choose Tools | Templates.
        
        if (xml.exists()) {
            users = readXML(User.class);
        } else {
            users = new ArrayList<>();
        }
    }

    public static synchronized UsersManager getInstance() {
        if (instance == null) {
            instance = new UsersManager();
        }

        return instance;
    }

    public void addUser(User user) {
        user.setId(getNextId());
        users.add(user);

        _updateXML();
    }

    private User _getUserByUsername(String username) {

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }

    private void _updateXML() {
        writeXML(users, User.class);
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

public EditUserResponseViewModel _editUser(EditUserRequestViewModel request) {
        EditUserResponseViewModel viewModel = new EditUserResponseViewModel();
        User user = _getUserByUsername(request.getUsername());
        User sessionUser = new User();

        if (user != null) {

            if (request.getOldPassword().equals(user.getPassword())) {
                user.setFirstName(request.getFirstName());
                sessionUser.setFirstName(request.getFirstName());
                
                user.setLastName(request.getLastName());
                sessionUser.setLastName(request.getLastName());
                
                user.setEmail(request.getEmail());
                sessionUser.setEmail(request.getEmail());
                
                sessionUser.setId(user.getId());
                
                sessionUser.setUsername(user.getUsername());
                
                if (!request.getPicture().equals("")) {
                    String filePath = createImage(request.getPicture(), user.getId());
                    user.setPicture(filePath);
                    sessionUser.setPicture(filePath);
                }

                if (request.getNewPassword().equals("")) {
                    sessionUser.setPassword(user.getPassword());
                } else {
                    user.setPassword(request.getNewPassword());
                    sessionUser.setPassword(request.getNewPassword());
                }
                
                
                viewModel.setLoggedUser(sessionUser);
                viewModel.setError(false);
                viewModel.setErrorMessage("");
            } else {
                viewModel.setError(true);
                viewModel.setErrorMessage("Password errata");
            }

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
        viewModel.setUsers(users);
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
            String filePath = "/multimedia/users/" + id + extension;
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
