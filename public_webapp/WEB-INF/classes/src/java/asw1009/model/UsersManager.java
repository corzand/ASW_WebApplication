package asw1009.model;

import org.apache.commons.codec.binary.Base64;
import asw1009.model.entities.User;
import asw1009.model.entities.UserList;
import asw1009.requests.EditUserRequest;
import asw1009.requests.LoginRequest;
import asw1009.requests.SignUpRequest;
import asw1009.responses.BaseResponse;
import asw1009.responses.EditUserResponse;
import asw1009.responses.LoginResponse;
import asw1009.responses.UsersListResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Classe singleton rappresentante il gestore degli utenti. Contiene la lista
 * delle entità User.
 *
 * @author ASW1009
 */
public class UsersManager {

    private UserList items;
    private static UsersManager instance;
    private JAXBContext jc;
    private Unmarshaller um;
    private Marshaller m;
    protected File xml;
    protected String directoryPath;
    private String fileName;

    /**
     * Costruttore di classe.
     */
    public UsersManager() {
    }

    /**
     * Metodo che fa l'override del metodo init() della classe FileManager: se
     * il file.xml esiste istanzia la lista leggendo dal file, altrimenti crea
     * una lista di utenti nuova.
     *
     * @param directoryPath stringa rappresentante il percorso del file.
     */
    public void init(String directoryPath) {
        try {
            this.jc = JAXBContext.newInstance(UserList.class);
            this.um = jc.createUnmarshaller();
            this.m = jc.createMarshaller();
            this.directoryPath = directoryPath;
            this.fileName = "Users";
            this.xml = new File(this.directoryPath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "xml" + System.getProperty("file.separator") + this.fileName + ".xml");

            if (xml.exists()) {
                items = (UserList) um.unmarshal(xml);
            } else {
                items = new UserList();
            }
        } catch (JAXBException ex) {
            Logger.getLogger(CategoriesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Restituisce l'istanza della classe.
     *
     * @return istanza statica del gestore degli utenti
     */
    public static synchronized UsersManager getInstance() {
        if (instance == null) {
            instance = new UsersManager();
        }

        return instance;
    }

    /**
     * Aggiunge un nuovo utente e aggiorna il fileXML.
     *
     * @param user rappresentante l'utente.
     */
    public void addUser(User user) {
        user.setId(items.getNextId());
        items.getList().add(user);

        updateXML();
    }

    /**
     * Restituisce l'utente corrispondente all'username inserito, prendendolo
     * dalla lista di utenti.
     *
     * @param username rappresentante il nome dell'utente.
     * @return user relativo alla lista di utenti.
     */
    private User getUserByUsername(String username) {

        for (int i = 0; i < items.getList().size(); i++) {
            User user = items.getList().get(i);
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        return null;
    }

    /**
     * Aggiorna il fileXML scrivendolo.
     */
    private void updateXML() {
        try {
            m.marshal(items, xml);
        } catch (JAXBException ex) {
            Logger.getLogger(UsersManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metodo invocato per effettuare l'iscrizione dell'utente. Se la
     * registrazione viene effettuata per la prima volta si impostano i campi
     * utili, altrimenti viene inviato un messaggio di errore.
     *
     * @param request oggetto rappresentante la richiesta di
     * registrazione dell'utente.
     * @return response rappresentante la risposta del client.
     */
    public BaseResponse signUp(SignUpRequest request) {
        BaseResponse response = new BaseResponse();

        if (getUserByUsername(request.getUsername()) == null) {
            User userToAdd = new User();
            userToAdd.setEmail(request.getEmail());
            userToAdd.setFirstName(request.getFirstName());
            userToAdd.setLastName(request.getLastName());
            userToAdd.setUsername(request.getUsername());
            userToAdd.setPassword(request.getPassword());
            addUser(userToAdd);

            response.setError(false);

        } else {
            response.setError(true);
            response.setErrorMessage("Questo username è già in uso");
        }

        return response;
    }

    /**
     * Metodo invocato per effettuare l'accesso dell'utente.
     *
     * @param request contiene la richiesta di login dell'utente.
     * @return response rappresentante la risposta da inviare al client.
     */
    public LoginResponse login(LoginRequest request) {
        LoginResponse viewModel = new LoginResponse();
        User user = getUserByUsername(request.getUsername());

        if (user != null) {
            if (user.getPassword().equals(request.getPassword())) {
                viewModel.setError(false);
                viewModel.setErrorMessage("");
                viewModel.setLoggedUser(user);
            } else {
                viewModel.setError(true);
                viewModel.setErrorMessage("Password errata.");
            }
        } else {
            viewModel.setError(true);
            viewModel.setErrorMessage("Username non trovato.");
        }

        return viewModel;
    }

    /**
     * Metodo invocato per effettuare la modifica di un user. 
     * Aggiorna l'utente in memoria e infine aggiorna il fileXML.
     *
     * @param request oggetto che rappresenta la richiesta di modifica dell'utente.
     * @return risposta da inviare al client, contenente le nuove informazioni relative all'utente loggato.
     */
    public EditUserResponse editUser(EditUserRequest request) {
        EditUserResponse response = new EditUserResponse();
        User user = getUserByUsername(request.getUsername());
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
                sessionUser.setPicture(user.getPicture());
                sessionUser.setUsername(user.getUsername());

                if (!request.getPicture().equals("")) {
                    //Se viene inviata l'immagine sotto forma di stringa di bytes, il server crea il file nella directory
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

                response.setLoggedUser(sessionUser);
                response.setError(false);
                response.setErrorMessage("");
            } else {
                response.setError(true);
                response.setErrorMessage("Password errata");
            }

        } else {
            response.setError(true);
            response.setErrorMessage("Login failed");
        }

        updateXML();
        return response;
    }

    /**
     * Metodo invocato per recuperare la lista degli utenti
     *
     * @return oggetto che che contiene la lista degli utenti.
     */
    public UsersListResponse usersList() {
        UsersListResponse response = new UsersListResponse();
        response.setError(false);
        response.setUsers(items.getList());
        return response;
    }

    /**
     * Metodo privato invocato per creare l'immagine del profilo utente,
     * attraverso la conversione di una stringa in base64 in un file immagine da salvare a FileSystem.
     *
     * @param base64 Immagine codificata in base64
     * @param id Intero rappresentante l'identificativo univoco dell'utente a cui è associata l'immagine.
     * @return filepath rappresentante il percorso del file.
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
            String filePath = "multimedia/users/" + id + extension;
            File file = new File(directoryPath + filePath);
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
