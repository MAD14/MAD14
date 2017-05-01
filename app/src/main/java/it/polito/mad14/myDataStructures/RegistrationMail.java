package it.polito.mad14.myDataStructures;

/**
 * Created by Elena on 27/04/2017.
 */

public class RegistrationMail extends Mail {

    private String message = "Hello! \n" +
            "You completed the registration form of TeddyApplication.\n" +
            "Please, click this link to confirm your email address:\n" +
            "https://mad14-4e361.firebaseapp.com/__/auth/action?mode=<action>&oobCode=<code>\n" +
            "Your MAD14 team";
    private String _body;

    public RegistrationMail(){
        super();
        this._body=message;
    }

    public String get_body() {
        return _body;
    }
}
