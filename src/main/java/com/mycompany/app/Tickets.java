package com.mycompany.app;

import java.util.ArrayList;

/**
 * Ticket per un determinato Bug
 */
public class Tickets {
    private String name;
    private Version iv;
    private Version fv;
    private Version ov;
    private String commitId;
    private ArrayList<Version> affectedVersions; /* nome di tutte le versioni affette dal bug */

    /* in caso non fosse presente IV allora bisogna applicare proportion per calcolarselo */


    //releases should be relNames but for make the class independent of the entire project, I prefer to pass it as argument
    public Tickets(String name, Version iv, Version fv, Version ov, ArrayList<Version> releases) {
        this.name = name;
        this.iv = iv;
        this.fv = fv;
        this.ov = ov;
        this.affectedVersions = new ArrayList<>();
        this.affectedVersions = new ArrayList<>(releases.subList(releases.indexOf(iv), releases.indexOf(fv)));
    }

    //TODO: implements proportion
    public Tickets(String name, Version fv, Version ov) {
        this.name = name;
        this.fv = fv;
        this.ov = ov;
        this.affectedVersions = new ArrayList<>();

        /* inizializzo l'array delle affected version: non ho l'injected version quindi bisogna usare proportion*/

        /* ... Proportion ... */


    }

    public String getName() {
        return name;
    }

    public Version getIv() {
        return iv;
    }

    public Version getFv() {
        return fv;
    }

    public String getCommitId() {
        return commitId;
    }

    public ArrayList<Version> getAffectedVersions() {
        return affectedVersions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIv(Version iv) {
        this.iv = iv;
    }

    public void setFv(Version fv) {
        this.fv = fv;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public void setAffectedVersions(ArrayList<Version> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }
}
