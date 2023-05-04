package com.mycompany.app;

import org.eclipse.jgit.lib.Ref;

import java.util.Calendar;

public class Release {
    private Ref ref;
    private Calendar date;

    public Release(Ref ref, Calendar date) {
        this.ref = ref;
        this.date = date;
    }

    public Ref getRef() {
        return ref;
    }

    public Calendar getDate() {
        return date;
    }

    public void setRef(Ref ref) {
        this.ref = ref;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
}
