package com.mycompany.app;

import java.util.ArrayList;
import java.util.List;

import static com.mycompany.app.Proportion.calcInjectedVersion;

/**
 * Ticket per un determinato Bug
 */
public class Tickets {
    private String name;
    private Version iv;
    private Version fv;
    private Version ov;
    private String commitId;
    private List<Version> affectedVersions; /* nome di tutte le versioni affette dal bug */

    /* in caso non fosse presente IV allora bisogna applicare proportion per calcolarselo */


    //releases should be relNames but for make the class independent of the entire project, I prefer to pass it as argument
    public Tickets(String name, Version iv, Version fv, Version ov, List<Version> releases) {
        this.name = name;
        this.iv = iv;
        this.fv = fv;
        this.ov = ov;
        this.affectedVersions = new ArrayList<>(releases.subList(releases.indexOf(iv), releases.indexOf(fv)));

    }

    public Tickets(String name, Version fv, Version ov) {
        this.name = name;
        this.fv = fv;
        this.ov = ov;
        this.iv = null;
        this.affectedVersions = new ArrayList<>();

    }

    public void calIvAndSetAv(List<Version> releases){
        /* ... The Injected Version is not available: use Proportion ... */
        int ivn = calcInjectedVersion(this);
        this.iv = releases.get(ivn - 1);
        this.affectedVersions = new ArrayList<>(releases.subList(releases.indexOf(this.iv), releases.indexOf(this.fv)));
    }


    public String getName() {
        return this.name;
    }

    public Version getIv() {
        return this.iv;
    }

    public Version getFv() {
        return this.fv;
    }

    public Version getOv() {
        return this.ov;
    }

    public String getCommitId() {
        return this.commitId;
    }

    public List<Version> getAffectedVersions() {
        return this.affectedVersions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

}
