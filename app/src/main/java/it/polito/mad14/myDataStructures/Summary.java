package it.polito.mad14.myDataStructures;

/**
 * Created by Utente on 01/05/2017.
 */

public class Summary {

    String name;
    String value;
    boolean credit;// true è un credito (-> verde); false è un debito (-> rosso)
//    String image;

    public Summary(String name, String value, boolean credit) {
        this.name = name;
        this.value = value;
        this.credit = credit;
//        this.image = image;
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

    public boolean getCredit() {
        return credit;
    }

    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    public boolean equals(Summary s){
        return this.name.equals(s.getName());
    }

//    public boolean hasImage(){
//        if (image != null)
//            return true;
//        return false;
//    }
//
//    public String getImage() {
//        return image;
//    }
//
//    public void setImage(String image) {
//        this.image = image;
//    }
}
