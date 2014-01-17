package asw1009.model;

import asw1009.model.entities.CategoryList;
import asw1009.responses.CategoriesListResponse;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Classe singleton rappresentante il gestore delle categorie. Contiene la lista
 * delle entità Category ed espone ai controllers i metodi per la loro gestione
 *
 * @author ASW1009
 */
public class CategoriesManager {

    private CategoryList items;
    private static CategoriesManager instance;
    private JAXBContext jc;
    private Unmarshaller um;
    protected File xml;
    protected String directoryPath;
    private String fileName;

    /**
     * Costruttore di classe.
     */
    public CategoriesManager() {

    }

    /**
     * Metodo che fa l'override del metodo init() della classe FileManager: se
     * il file.xml esiste istanzia la lista leggendo dal file, altrimenti crea
     * una lista di categorie nuova.
     *
     * @param directoryPath stringa rappresentante il percorso del file.
     */
    public void init(String directoryPath) {
        try {
            this.jc = JAXBContext.newInstance(CategoryList.class);
            this.um = jc.createUnmarshaller();

            this.directoryPath = directoryPath;
            this.fileName = "Categories";
            this.xml = new File(this.directoryPath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "xml" + System.getProperty("file.separator") + this.fileName + ".xml");

            if (xml.exists()) {
                items = (CategoryList) um.unmarshal(xml);
            } else {
                items = new CategoryList();
            }
        } catch (JAXBException ex) {
            Logger.getLogger(CategoriesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Restituisce l'istanza della classe.
     *
     * @return istanza statica del gestore delle categorie
     */
    public static synchronized CategoriesManager getInstance() {
        if (instance == null) {
            instance = new CategoriesManager();
        }

        return instance;
    }

    /**
     * Metodo invocato per recuperare la lista delle categorie
     *
     * @return viewModel che contiene la lista delle categorie
     */
    public CategoriesListResponse categoriesList() {
        CategoriesListResponse response = new CategoriesListResponse();
        response.setError(false);
        response.setCategories(items.getList());
        return response;
    }
}
