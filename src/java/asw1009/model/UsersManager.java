/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asw1009.model;

import asw1009.model.entities.User;

/**
 *
 * @author Andrea
 */
public class UsersManager {

    private static UsersManager instance;

    private UsersManager() {

    }

    public static synchronized UsersManager getInstance() {
        if (instance == null) {
            instance = new UsersManager();
        }

        return instance;
    }
    
    public User getUser(String username, String password){
        //Search a user and..
        return new User();
    }
}
