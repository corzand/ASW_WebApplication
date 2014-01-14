package asw1009.model;

import asw1009.model.entities.Category;
import asw1009.responses.CategoriesListResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe singleton rappresentante il gestore delle categorie. Contiene la lista
 * delle entità Category ed espone ai controllers i metodi per la loro gestione
 *
 * @author ASW1009
 */
public class CategoriesManager extends FileManager {

    private List<Category> categories;
    private static CategoriesManager instance;

    /**
     * Costruttore di classe.
     */
    public CategoriesManager() {
        categories = new ArrayList<>();
    }

    /**
     * Metodo che fa l'override del metodo init() della classe FileManager: se
     * il file.xml esiste istanzia la lista leggendo dal file, altrimenti crea
     * una lista di categorie nuova.
     *
     * @param directoryPath stringa rappresentante il percorso del file.
     * @param fileName stringa rappresentante il nome del file.
     */
    @Override
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName);
        if (xml.exists()) {
            categories = (List<Category>) readXML(Category.class);
        } else {
            categories = new ArrayList<>();
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
        response.setCategories(categories);
        return response;
    }
}
