package com.mycompany.app;

import java.util.ArrayList;

public class TicketList extends ArrayList<Tickets> {

    public boolean containsCommit(String id){
        for(Tickets t : this){
            if(t.getCommitId().equals(id))
                return true;
        }
        return false;
    }

    public Tickets getFromCommitId(String id){
        for(Tickets t : this){
            if(t.getCommitId().equals(id))
                return t;
        }
        return null;
    }
}
