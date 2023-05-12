package com.mycompany.app;

import java.util.ArrayList;

/**
 * Ticket per un determinato Bug
 */
public class Tickets {
    private String name;
    private String fixedRelease;
    private String iv;
    private String fv;
    private String commitId;
    private ArrayList<String> affectedVersions; /* nome di tutte le versioni affette dal bug */

    /* in caso non fosse presente IV allora bisogna applicare proportion per calcolarselo */

    public Tickets(String name, String fixedRelease, String iv, String fv, ArrayList<String> releases) {
        this.name = name;
        this.fixedRelease = fixedRelease;
        this.iv = iv;
        this.fv = fv;
        this.affectedVersions = new ArrayList<>();

        /* inizializzo l'array delle affected version
        * ad iv e fv bisogna anteporgli 'refs/tags/syncope' oppure refs/tags/release' a seconda del progetto
        * */
        int i = releases.indexOf(iv);
        int size = releases.indexOf(fv) - i;
        for(; i < size; i++){
            this.affectedVersions.add(releases.get(i));
        }
    }

    public String getName() {
        return name;
    }

    public String getFixedRelease() {
        return fixedRelease;
    }

    public String getIv() {
        return iv;
    }

    public String getFv() {
        return fv;
    }

    public String getCommitId() {
        return commitId;
    }

    public ArrayList<String> getAffectedVersions() {
        return affectedVersions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFixedRelease(String fixedRelease) {
        this.fixedRelease = fixedRelease;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public void setFv(String fv) {
        this.fv = fv;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public void setAffectedVersions(ArrayList<String> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }
}
