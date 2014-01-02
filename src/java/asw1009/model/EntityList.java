/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package asw1009.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrea
 */
    
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