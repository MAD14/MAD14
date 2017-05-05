package it.polito.mad14.myDataStructures;

/**
 * Created by Utente on 30/04/2017.
 */

public class Group {
    String ID;
    String name;
    String author;
    String date;

    public Group(String ID,String name, String author, String date) {
        this.ID=ID;
        this.name = name;
        this.author = author;
        this.date = date;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID=ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setData(String date) {
        this.date = date;
    }

    public String toString(){
        return this.name;
    }

    public boolean compare(Group group){

        if(group.ID.equals(this.ID))
            return true;
        else
            return false;
    }
}
