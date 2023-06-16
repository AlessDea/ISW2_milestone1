package com.mycompany.app;

import java.util.ArrayList;
import java.util.List;

public class RepoFile
{
    private String name;
    private int appearances;
    private ArrayList<Version> releases;
    private ArrayList<String> paths;
    private ArrayList<Integer> locs;
    private ArrayList<Integer> touchedLOCs; /* between two release : added + deleted */
    private ArrayList<Integer> churn; /* between two release : |added - deleted| -> questo può essere fatto facendo la differenza tra i LOC delle release da verificare*/
    private int revisionFirstAppearance;
    private ArrayList<Integer> nAuth;
    private ArrayList<Integer> revisions; //number of revisions
    private ArrayList<Integer> locAdded;
    private ArrayList<Boolean> buggy;
    private ArrayList<Integer> nFix; // number of fix in each release
    private ArrayList<Integer> weightedAge; //Age of release wighted by LoC touched
    private ArrayList<Integer> avgSetSize;


    public RepoFile(String name) {
        this.name = name;
        this.appearances = 1;
        this.releases = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.locs = new ArrayList<>();
        this.touchedLOCs = new ArrayList<>();
        this.churn = new ArrayList<>();
        this.nAuth = new ArrayList<>();
        this.revisions = new ArrayList<>();
        this.locAdded = new ArrayList<>();
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
        return this.getName().equals(name);
    }

    public void insertRelease(Version release) {
        this.releases.add(release);
        this.buggy.add(false); // all'inizio le considero tutte non buggy
    }

    public List<Version> getReleases() {
        return releases;
    }

    public void insertPath(String path) {
        this.paths.add(path);
    }

    public List<String> getPaths() {
        return paths;
    }

    public void insertLOCs(int l) {
        this.locs.add(l);
    }

    public List<Integer> getLocs() {
        return locs;
    }

    public void insertTouchedLOCs(Integer t) {
        this.touchedLOCs.add(t);
    }

    public List<Integer> getTouchedLOCs() {
        return touchedLOCs;
    }



    /**
     * Insert the churn for a release
    * @param c index of the current release
    * */
    public void insertChurn(int c) {
        int chrn;
        if(c < 0){
            this.churn.add(c);
            return;
        }
        if(c == 0){ //first release -> added - deleted = LOCs
            chrn = this.locs.get(c);
        }else{
            chrn = Math.abs(this.locs.get(c) - this.locs.get(c-1));
        }

        this.churn.add(chrn);
    }

    public List<Integer> getChurn() {
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

    public List<Integer> getnAuth() {
        return nAuth;
    }

    public void insertRevisions(Integer revision) {
        this.revisions.add(revision);
    }

    public List<Integer> getRevisions() {
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

    public void insertLOCAdded(int lAdded) {
        this.locAdded.add(lAdded);
    }

    public List<Integer> getLocAdded() {
        return locAdded;
    }

    public List<Boolean> getBuggy() {
        return buggy;
    }

    public void insertBuggy(Boolean buggy) {
        this.buggy.add(buggy);
    }

    public List<Integer> getnFix() {
        return nFix;
    }

    public void insertnFix(int nFix) {
        this.nFix.add(nFix);
    }

    public List<Integer> getWeightedAge() {
        return weightedAge;
    }

    public void insertWeightedAge(int curRel) {
        if(curRel < 0){
            this.weightedAge.add(curRel);
            return;
        }
        this.weightedAge.add((curRel - this.getRevisionFirstAppearance())*this.getTouchedLOCs().get(this.getTouchedLOCs().size()-1));
    }

    public List<Integer> getAvgSetSize() {
        return avgSetSize;
    }

    public void insertAvgSetSize(int avgSetSize) {
        this.avgSetSize.add(avgSetSize);
    }
}
