package it.polito.mad14.myDataStructures;

/**
 * Created by Utente on 01/05/2017.
 */

public class Expense {
    private String name;
    private String value; //double
    private String description;
    private String author;
    private String group;


    public Expense(String name, String value, String description, String author,String group) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.author = author;
        this.group= group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGroup(){ return group;}

    public void setGroup(String group){ this.group=group;}



}
