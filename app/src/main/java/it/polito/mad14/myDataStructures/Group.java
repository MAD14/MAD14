package it.polito.mad14.myDataStructures;

/**
 * Created by Utente on 30/04/2017.
 */

public class Group {
    String name;
    String author;
    String date;

    public Group(String name, String author, String date) {
        this.name = name;
        this.author = author;
        this.date = date;
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
}
