
package asw1009.model;

import asw1009.model.entities.Category;
import asw1009.viewmodel.response.CategoriesListResponseViewModel;

public class CategoriesManager extends FileManager {
    
    //private List<User> users;
    private EntityList<Category> _categories;
    private static CategoriesManager instance;

    public CategoriesManager() {
        //users = new ArrayList<>();
        _categories = new EntityList<>();
    }

    @Override
    public void init(String directoryPath, String fileName) {
        super.init(directoryPath, fileName); //To change body of generated methods, choose Tools | Templates.
        
        _xstream.alias("category", Category.class);
        _xstream.alias("categories", EntityList.class);
        _xstream.addImplicitCollection(EntityList.class, "list");
        _readXML();
        //Eventualmente, leggere il contenuto del file Users.xml e impostare gli oggetti in memoria.
    }
    
    public static synchronized CategoriesManager getInstance() {
        if (instance == null) {
            instance = new CategoriesManager();
        }

        return instance;
    }

    public void addCategory(Category category) {
        category.setId(_categories.getNextId());
        _categories.getItems().add(category);
    }

    private void _readXML(){
        if(xml.exists()){
            _categories = (EntityList<Category>)readXML();
        }else {
            _categories = new EntityList<>();
        }
    }
    
    private void _updateXML(){        
        writeXML(_xstream.toXML(_categories));
    }
    
    public CategoriesListResponseViewModel categoriesList() {
        CategoriesListResponseViewModel viewModel = new CategoriesListResponseViewModel();
        viewModel.setError(false);
        viewModel.setCategories(_categories.getItems());
        return viewModel;
    }
}
