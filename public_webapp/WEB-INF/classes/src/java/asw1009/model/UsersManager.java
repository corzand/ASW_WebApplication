
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

/**
 * Classe singleton rappresentante il gestore degli utenti.
 * Contiene la lista delle entità User.
 * 
 * @author ASW1009
 */

public class UsersManager extends FileManager {

    private List<User> users;
    private static UsersManager instance;
    
    /**
    * Costruttore di classe.
    */
    public UsersManager() {
        users = new ArrayList<>();
    }

    @Override
    /**
     * Metodo che fa l'override del metodo init() della classe FileManager: se il file.xml esiste
     * istanzia la lista leggendo dal file, altrimenti crea una lista di utenti nuova.
     * 
     * @param directoryPath stringa rappresentante il percorso del file.
     * @param fileName stringa rappresentante il nome del file.
    */
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName);
        
        if (xml.exists()) {
            users = readXML(User.class);
        } else {
            users = new ArrayList<>();
        }
    }
    
    /**
     * Restituisce l'istanza della classe.
     * @return instance statico del gestore degli utenti inizilizzato all'inizio a null.
    */
    public static synchronized UsersManager getInstance() {
        if (instance == null) {
            instance = new UsersManager();
        }

        return instance;
    }
    
    /**
     * Aggiunge un nuovo utente e aggiorna il fileXML.
     * @param user rappresentante l'utente.
     */
    public void addUser(User user) {
        user.setId(getNextId());
        users.add(user);

        _updateXML();
    }
    
    /**
     * Restituisce l'utente corrispondente all'username inserito, prendendolo dalla lista di utenti.
     * @param username rappresentante il nome dell'utente.
     * @return user relativo alla lista di utenti.
     */
    private User _getUserByUsername(String username) {

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }
    /**
     * Aggiorna il fileXML scrivendolo.
     */
    private void _updateXML() {
        writeXML(users, User.class);
    }
    
    /**
     * Metodo invocato per effettuare l'iscrizione dell'utente. Se la registrazione viene effettuata
     * per la prima volta si impostano i campi utili, altrimenti viene inviato un messaggio di errore. 
     * @param request indica il viewModel rappresentante la richiesta di registrazione dell'utente.
     * @return viewModel rappresentante la risposta del client.
     */
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
    /**
     * Metodo invocato per effettuare l'accesso dell'utente.
     * @param request indica il viewModel rappresentante la richiesta di login dell'utente.
     * @return viewModel rappresentante la risposta del client.
     */
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
    /**
     * Metodo invocato per effettuare la modifica di un user. Consente di effettuare modifiche se:
     * la vecchia password è uguale alla password attuale, se l'utente non ha cambiato la password o
     * non ha ancora inserito l'immagine profilo. Al contrario, invia un messaggio di registrazione 
     * errata. Infine aggiorna il fileXML.
     * @param request indica il viewModel rappresentante la richiesta di modifica dell'utente.
     * @return viewModel rappresentante la risposta del client.
     */
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
    /**
     * Metodo invocato per recuperare un view model contenente la lista degli utenti
     * @return viewModel che contiene la lista degli utenti.
     */
    public UsersListResponseViewModel usersList() {
        UsersListResponseViewModel viewModel = new UsersListResponseViewModel();
        viewModel.setError(false);
        viewModel.setUsers(users);
        return viewModel;
    }
    
    /**
     * Metodo privato invocato per creare l'immagine del profilo utente, attraverso la 
     * conversione di una stringa a base64 in un immagine di array di byte.
     * @param base64 Stringa codificata secondo la numerazione posizionale che usa 64 simboli.
     * @param id Intero rappresentante l'identificativo univoco dell'immagine.
     * @return filepath rappresentante il percorso del file dell'immagine.
     */
    private String createImage(String base64, int id) {

        try {
            String[] splitted = base64.split(",");

            String extension = ".jpg";
            if (splitted[0].contains("gif")) {
                extension = ".gif";
            } else if (splitted[0].contains("png")) {
                extension = ".png";
            }

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