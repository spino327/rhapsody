/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agenda;

/**
 *
 * @author Swoko
 */
public class Contact {

    private String name, phoneNumber, mail;
    private int phoneType, contactType;

    public Contact(String name, String phoneNumber, int phoneType, String mail,
            int contactType){
        this.name = name;   this.phoneNumber = phoneNumber;   this.mail = mail;
        this.phoneType = phoneType; this.contactType = contactType;
    }

    public String getName(){
        return name;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public String getMail(){
        return mail;
    }

    public int getPhoneType(){
        return phoneType;
    }

    public int getContactType(){
        return contactType;
    }

}
