
package asw1009.model;

import java.util.ArrayList;
import java.util.List;

    
public class EntityList<T> {
        private List<T> list;
        private int id;

        public EntityList(){
            list = new ArrayList<T>();
            id = 0;
        }
        
        public List<T> getItems(){
            return list;
        }
        
        public int getNextId(){
            return ++id;
        }
		
    }