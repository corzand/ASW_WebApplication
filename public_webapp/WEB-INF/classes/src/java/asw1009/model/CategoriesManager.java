
package asw1009.model;

import asw1009.model.entities.Category;
import asw1009.viewmodel.response.CategoriesListResponseViewModel;
import java.util.ArrayList;
import java.util.List;

public class CategoriesManager extends FileManager {
    
    private List<Category> _categories;
    private static CategoriesManager instance;

    public CategoriesManager() {
        //users = new ArrayList<>();
        _categories = new ArrayList<>();
    }

    @Override
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName); //To change body of generated methods, choose Tools | Templates.
        if(xml.exists()){
            _categories = (List<Category>)readXML(Category.class);
        }else {
            _categories = new ArrayList<>();
        }
        //Eventualmente, leggere il contenuto del file Users.xml e impostare gli oggetti in memoria.
    }
    
    public static synchronized CategoriesManager getInstance() {
        if (instance == null) {
            instance = new CategoriesManager();
        }

        return instance;
    }


    private void _readXML(){

    }
    
    public CategoriesListResponseViewModel categoriesList() {
        CategoriesListResponseViewModel viewModel = new CategoriesListResponseViewModel();
        viewModel.setError(false);
        viewModel.setCategories(_categories);
        return viewModel;
    }
}
