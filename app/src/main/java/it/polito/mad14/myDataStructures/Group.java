package it.polito.mad14.myDataStructures;

public class Group {
    String ID;
    String name;
    String author;
    String date;
    String credit;
    String debit;
    String image;
    String currency;
    String lastChange;


    public Group(String ID,String name, String author, String date, String credit, String debit, String image, String currency, String lastChange) {
        this.ID=ID;
        this.name = name;
        this.author = author;
        this.date = date;
        this.credit = credit;
        this.debit = debit;
        this.image = image;
        this.currency = currency;
        this.lastChange = lastChange;
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

    public void setDate(String date) {
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

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public boolean hasImage(){
        if (image != null) {
            return true;
        } else {
            return false;
        }
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLastChange() {
        return lastChange;
    }

    public void setLastChange(String lastChange) {
        this.lastChange = lastChange;
    }
}
