package it.polito.mad14.myDataStructures;

/**
 * Created by Utente on 02/05/2017.
 */

public class Contact {

    private String name;
    private String surname;
    private String username;
    private String email;
    private String image;

    public Contact(String name, String surname, String username, String email,String image) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.image = image;
    }

    public String toString(){ return name + " " + surname + " - " + username; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean hasImage(){
        if (image != null)
            return true;
        return false;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
