package it.polito.mad14.myDataStructures;

/**
 * Created by Utente on 01/05/2017.
 */

public class Summary {

    String name;
    String value;
    String currency;
    String email;
    boolean credit;// true è un credito (-> verde); false è un debito (-> rosso)
    int paymentSituation;

    public Summary(String name, String value, String email, String currency, boolean credit) {

        this.name = name;
        this.value = value;
        this.currency = currency;
        this.credit = credit;
        this.email=email;
        this.paymentSituation = 0;
      
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public int getPaymentSituation() {
        return paymentSituation;
    }

    public void setPaymentSituation(int paymentSituation) {
        this.paymentSituation = paymentSituation;
    }
}
