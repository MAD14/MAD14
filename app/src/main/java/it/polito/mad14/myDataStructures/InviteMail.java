package it.polito.mad14.myDataStructures;

/**
 * Created by Utente on 27/04/2017.
 */

public class InviteMail extends Mail {
    private String message = "Hello! \n" +
            "You received an invite to join a group in MAD14 from one of your friend.\n" +
            "Lets join our community downloading our app at this link:\n" +
            "https://teddyapplication.com/welcome\n" +
            "To join the group .... " +
            "Your MAD14 team";
    private String _body;
    private String _subject;
    private String _user;
    private String _pass;

    private String[] _to;

    public InviteMail(){
        super();
        this._body=message;
        this._subject="MAD14 Application - Invite";

    }

    public InviteMail(String user, String pass) {
        this();
        _user = user;
        _pass = pass;
    }

    public String get_body() {
        return _body;
    }

    public void set_body(String _body) {
        this._body = _body;
    }
}
