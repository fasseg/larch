/* 
* Copyright 2014 Frank Asseg
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/
package net.objecthunter.larch.model.security;

import java.util.List;

/**
 * A DTO for a larch User
 */
public class User {
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String pwhash;
    private List<Group> groups;

    /**
     * Get the user's name
     * @return th user's name
     */
    public String getName() {
        return name;
    }

    /**
     * set the user's name
     * @param name the user's name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get the user's first name
     * @return the user's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the user's first name
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the user's last name
     * @return the user's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the user's last name
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get the user's email address
     * @return the email dress
     */
    public String getEmail() {
        return email;
    }

    /**
     * set the user's email address
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the user's password hash
     * @return the user's password hash
     */
    public String getPwhash() {
        return pwhash;
    }

    /**
     * set the user's password hash
     * @param pwhash the hash to set
     */
    public void setPwhash(String pwhash) {
        this.pwhash = pwhash;
    }

    /**
     * get the groups of the user
     * @return the groups
     */
    public List<Group> getGroups() {
        return groups;
    }

    /**
     * Set the groups of the user
     * @param groups the groups to set
     */
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
