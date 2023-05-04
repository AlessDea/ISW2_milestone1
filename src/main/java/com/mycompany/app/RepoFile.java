package com.mycompany.app;

import java.util.ArrayList;

public class RepoFile
{
    private String name;
    private int revisions;
    private ArrayList<String> releases;
    private ArrayList<String> paths;
    private ArrayList<Integer> LOCs;
    private ArrayList<Integer> touchedLOCs; /* between two release : added + deleted */
    private ArrayList<Integer> churn; /* between two release : |added - deleted| -> questo può essere fatto facendo la differenza tra i LOC delle release da verificare*/
    private int revisionFirstAppearance;
    private ArrayList<Integer> nAuth;


    public RepoFile(String name) {
        this.name = name;
        this.revisions = 1;
        this.releases = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.LOCs = new ArrayList<>();
        this.touchedLOCs = new ArrayList<>();
        this.churn = new ArrayList<>();
        this.nAuth = new ArrayList<>();

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /* increment */
    public void incRevisions() {
        this.revisions++;
    }

    public void decRevisions() {
        this.revisions--;
    }

    public int getRevisions() {
        return revisions;
    }

    public Boolean equals(String name){
        if(this.getName().equals(name))
            return true;
        return false;
    }

    public void insertRelease(String release) {
        this.releases.add(release);
    }

    public ArrayList<String> getReleases() {
        return releases;
    }

    public void insertPath(String path) {
        this.paths.add(path);
    }

    public ArrayList<String> getPaths() {
        return paths;
    }

    public void insertLOCs(int l) {
        this.LOCs.add(l);
    }

    public ArrayList<Integer> getLOCs() {
        return LOCs;
    }

    public void insertTouchedLOCs(Integer t) {
        this.touchedLOCs.add(t);
    }

    public ArrayList<Integer> getTouchedLOCs() {
        return touchedLOCs;
    }



    /**
     * Insert the churn for a release
    * @param c index of the current release
    * */
    public void insertChurn(int c) {
        int churn;
        if(c == 0){ //first release -> added - deleted = LOCs
            churn = this.LOCs.get(c);
        }else{
            churn = Math.abs(this.LOCs.get(c) - this.LOCs.get(c-1));
        }


        this.churn.add(churn);
    }

    public ArrayList<Integer> getChurn() {
        return churn;
    }

    public void setRevisionFirstAppearance(int revisionFirstAppearance) {
        this.revisionFirstAppearance = revisionFirstAppearance;
    }

    public int getRevisionFirstAppearance() {
        return revisionFirstAppearance;
    }

    public void insertAuth(int nauth) {
        this.nAuth.add(nauth);
    }

    public ArrayList<Integer> getnAuth() {
        return nAuth;
    }
}