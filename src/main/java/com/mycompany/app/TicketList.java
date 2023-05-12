package com.mycompany.app;

import java.util.ArrayList;

public class TicketList<T> extends ArrayList<Tickets> {

    ArrayList<Tickets> tickets;
    public boolean containsCommit(String id){
        for(Tickets t : tickets){
            if(t.getCommitId().equals(id))
                return true;
        }
        return false;
    }

    public Tickets getFromCommitId(String id){
        for(Tickets t : tickets){
            if(t.getCommitId().equals(id))
                return t;
        }
        return null;
    }
}
