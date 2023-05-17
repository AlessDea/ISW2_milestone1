package com.mycompany.app;

import java.util.ArrayList;

public class RepoFile
{
    private String name;
    private int appearances;
    private ArrayList<Version> releases;
    private ArrayList<String> paths;
    private ArrayList<Integer> LOCs;
    private ArrayList<Integer> touchedLOCs; /* between two release : added + deleted */
    private ArrayList<Integer> churn; /* between two release : |added - deleted| -> questo può essere fatto facendo la differenza tra i LOC delle release da verificare*/
    private int revisionFirstAppearance;
    private ArrayList<Integer> nAuth;
    private ArrayList<Integer> revisions; //number of revisions
    private ArrayList<Integer> LOCAdded;
    private ArrayList<Boolean> buggy;
    private ArrayList<Integer> nFix; // number of fix in each release
    private ArrayList<Integer> weightedAge; //Age of release wighted by LoC touched
    private ArrayList<Integer> avgSetSize;


    public RepoFile(String name) {
        this.name = name;
        this.appearances = 1;
        this.releases = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.LOCs = new ArrayList<>();
        this.touchedLOCs = new ArrayList<>();
        this.churn = new ArrayList<>();
        this.nAuth = new ArrayList<>();
        this.revisions = new ArrayList<>();
        this.LOCAdded = new ArrayList<>();
        this.buggy = new ArrayList<>();
        this.weightedAge = new ArrayList<>();
        this.nFix = new ArrayList<>();
        this.avgSetSize = new ArrayList<>();

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public Boolean equals(String name){
        if(this.getName().equals(name))
            return true;
        return false;
    }

    public void insertRelease(Version release) {
        this.releases.add(release);
        this.buggy.add(false); // all'inizio le considero tutte non buggy
    }

    public ArrayList<Version> getReleases() {
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

    public void insertRevisions(Integer revision) {
        this.revisions.add(revision);
    }

    public ArrayList<Integer> getRevisions() {
        return this.revisions;
    }

    public int getAppearances() {
        return appearances;
    }

    public void incAppearances() {
        this.appearances += 1;
    }

    public void decAppearances() {
        this.appearances -= 1;
    }

    public void insertLOCAdded(int LOCAdded) {
        this.LOCAdded.add(LOCAdded);
    }

    public ArrayList<Integer> getLOCAdded() {
        return LOCAdded;
    }

    public ArrayList<Boolean> getBuggy() {
        return buggy;
    }

    public void insertBuggy(Boolean buggy) {
        this.buggy.add(buggy);
    }

    public ArrayList<Integer> getnFix() {
        return nFix;
    }

    public void insertnFix(int nFix) {
        this.nFix.add(nFix);
    }

    public ArrayList<Integer> getWeightedAge() {
        return weightedAge;
    }

    public void insertWeightedAge(int curRel) {
        //System.out.println("Loc touched array size: " + this.getTouchedLOCs().size());
        this.weightedAge.add((curRel - this.getRevisionFirstAppearance())*this.getTouchedLOCs().get(this.getTouchedLOCs().size()-1));
    }

    public ArrayList<Integer> getAvgSetSize() {
        return avgSetSize;
    }

    public void insertAvgSetSize(int avgSetSize) {
        this.avgSetSize.add(avgSetSize);
    }
}
