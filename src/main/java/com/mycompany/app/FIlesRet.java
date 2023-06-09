package com.mycompany.app;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import utils.ProjectsUtils;


import java.io.*;
import java.util.*;

import static com.mycompany.app.GetReleaseInfo.*;
import static com.mycompany.app.RetrieveTicketsID.retrieveTickets;
import static com.mycompany.app.RetrieveTicketsID.tickets;

public class FIlesRet {

    static List<RepoFile> files = null;

    static String repoPath = null;
    static String projName = null;

    static List<Ref> tags = null;
    static Repository repository = null;

    static TicketList buggyRelCommits = null;

    static RevCommit theOldestCommit = null;

    static final String AUTHORSFIELD = "authors";
    static final String TOUCHEDFIELD = "touched";
    static final String ADDEDFIELD = "added";
    static final String REVISIONSFIELD = "revisions";

    public static void findAndDeleteMantReleaseMetrics(Version v){
        for(RepoFile rf : files){
            if(rf.getReleases().contains(v)) {
                removeMantClasses(rf, rf.getReleases().indexOf(v));
            }
        }
    }

    public static void removeMantClasses(RepoFile file, int index){
        file.getPaths().remove(index);
        file.getLocs().remove(index);
        file.getChurn().remove(index);
        file.getWeightedAge().remove(index);
        file.getnAuth().remove(index);
        file.getRevisions().remove(index);
        file.getTouchedLOCs().remove(index);
        file.getLocAdded().remove(index);
        file.getAvgSetSize().remove(index);
        file.getnFix().remove(index);
        file.getBuggy().remove(index);
    }


    public static void writeOnFile() throws IOException {
        FileWriter fileWriter = null;
        int numVersions;
        String outname = projName + "FilesInfo.csv";

        try {
            fileWriter = new FileWriter(outname);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            fileWriter.close();
        }


        try {

            fileWriter.append("Version, Version Name, Name, LOCs, Churn, Age, Weighted Age, Number of Authors, Revisions, LOC Touched, LOC Added, Avg Set Size, Number of Fix, Buggy");
            fileWriter.append("\n");
            numVersions = relNames.size();
            if(projName.equals("SYNCOPE")) {
                numVersions -= 2; //così escludo le ultime 2
            }

            for (int i = 0; i < numVersions ; i++) {

                for (RepoFile file : files) {

                    /* viene fatta l'assunzione che un file compaia solo in modo sequenziale e non ci sia una sua assenza (un buco) tra release */
                    if ((i >= file.getRevisionFirstAppearance() - 1) && (file.getAppearances() > 0)) {
                        //versione number
                        fileWriter.append(Integer.toString(i + 1));
                        fileWriter.append(",");

                        //version name
                        fileWriter.append(relNames.get(i).getExtendedName());
                        fileWriter.append(",");

                        //class path
                        fileWriter.append(file.getPaths().get(0));
                        fileWriter.append(",");

                        //LOCs
                        fileWriter.append(file.getLocs().get(0).toString());
                        fileWriter.append(",");

                        //churn
                        fileWriter.append(file.getChurn().get(0).toString());
                        fileWriter.append(",");

                        //age
                        fileWriter.append((Integer.toString(i - file.getRevisionFirstAppearance() + 1)));
                        fileWriter.append(",");

                        //weighted age
                        fileWriter.append(file.getWeightedAge().get(0).toString());
                        fileWriter.append(",");

                        //authors number
                        fileWriter.append((file.getnAuth().get(0).toString()));
                        fileWriter.append(",");

                        //number of revisions
                        fileWriter.append(file.getRevisions().get(0).toString());
                        fileWriter.append(",");

                        //touched LOCs
                        fileWriter.append(file.getTouchedLOCs().get(0).toString());
                        fileWriter.append(",");

                        //Added LOCs
                        fileWriter.append(file.getLocAdded().get(0).toString());
                        fileWriter.append(",");

                        //Avg Set size
                        fileWriter.append(file.getAvgSetSize().get(0).toString());
                        fileWriter.append(",");

                        //number of fixes
                        fileWriter.append(file.getnFix().get(0).toString());
                        fileWriter.append(",");

                        //buggyness
                        fileWriter.append(file.getBuggy().get(0).toString());

                        fileWriter.append("\n");

                        file.getPaths().remove(0);
                        file.getLocs().remove(0);
                        file.getChurn().remove(0);
                        file.getWeightedAge().remove(0);
                        file.getnAuth().remove(0);
                        file.getRevisions().remove(0);
                        file.getTouchedLOCs().remove(0);
                        file.getLocAdded().remove(0);
                        file.getAvgSetSize().remove(0);
                        file.getnFix().remove(0);
                        file.getBuggy().remove(0);

                        file.decAppearances();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int iterateAndCompareFiles(String name, String path) {
        if(name == null)
            return -1;
        for (RepoFile f : files) {
            boolean b1 = f.equals(name);
            boolean b2 = f.getPaths().isEmpty();
            if (b1 && !b2 && path.equals(f.getPaths().get(f.getPaths().size() - 1))) {
                return files.indexOf(f);
            }
        }
        return -1;
    }

    public static void listRepositoryContents(Version rel, int releaseNumber) throws IOException, GitAPIException {

        ObjectId head = repository.resolve(rel.getExtendedName());
        if (head == null)
            return;

        // a RevWalk allows to walk over commits based on some filtering that is defined
        RevWalk walk = new RevWalk(repository);

        RevCommit commit = walk.parseCommit(head.toObjectId());

        RevTree tree = commit.getTree();


        // now use a TreeWalk to iterate over all files in the Tree recursively
        // you can set Filters to narrow down the results if needed
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        int ret;
        String[] tkns;

        while (treeWalk.next()) {
            if (treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test")) {

                tkns = treeWalk.getPathString().split("/");

                ret = iterateAndCompareFiles(tkns[tkns.length - 1], treeWalk.getPathString()); //fondamentalmente va bene solo se ha lo stesso nome

                if (ret >= 0) {
                    listRepoContentNotFirstApp(ret, rel, releaseNumber, treeWalk);
                } else {
                    listRepoContentFirstApp(tkns, rel, releaseNumber, treeWalk);
                }
            }
        }
    }


    public static void listRepoContentNotFirstApp(int ret, Version rel, int releaseNumber, TreeWalk treeWalk) throws IOException, GitAPIException {
        files.get(ret).incAppearances();
        files.get(ret).insertRelease(rel);
        files.get(ret).insertPath(treeWalk.getPathString());
        files.get(ret).insertLOCs(countLOCs(treeWalk.getPathString(), rel.getExtendedName()));
        files.get(ret).insertChurn(files.get(ret).getReleases().size() - 1);
        files.get(ret).insertAvgSetSize(avgSetSize(repository, treeWalk.getPathString(), rel.getExtendedName(), relNames.get(relNames.indexOf(rel) - 1).getExtendedName()));

        if (releaseNumber > 1) {
            Map<String, Integer> ar = countAuthAndRevs(repository, treeWalk.getPathString(), rel.getExtendedName(), relNames.get(releaseNumber - 2).getExtendedName());
            files.get(ret).insertAuth(ar.get(AUTHORSFIELD));
            files.get(ret).insertRevisions(ar.get(REVISIONSFIELD));

            Map<String, Integer> r = locTouched(repository, relNames.get(releaseNumber - 2).getExtendedName(), rel.getExtendedName(), treeWalk.getPathString());
            files.get(ret).insertTouchedLOCs(r.get(TOUCHEDFIELD));
            files.get(ret).insertLOCAdded(r.get(ADDEDFIELD));
        } else {
            Map<String, Integer> ar = countAuthAndRevs(repository, treeWalk.getPathString(), rel.getExtendedName(), null);
            files.get(ret).insertAuth(ar.get(AUTHORSFIELD));
            files.get(ret).insertRevisions(ar.get(REVISIONSFIELD));

            //è la prima release, quindi loc touched e anche le added sono tutte le loc
            files.get(ret).insertTouchedLOCs(files.get(ret).getLocs().get(files.get(ret).getLocs().size() - 1));
            files.get(ret).insertLOCAdded(files.get(ret).getLocs().get(files.get(ret).getLocs().size() - 1));
        }
        files.get(ret).insertWeightedAge(releaseNumber);
    }



    public static void listRepoContentFirstApp(String[] tkns, Version rel, int releaseNumber, TreeWalk treeWalk) throws IOException, GitAPIException {
        RepoFile rf = new RepoFile(tkns[tkns.length - 1]);
        rf.insertRelease(rel);
        rf.insertPath(treeWalk.getPathString());
        rf.insertLOCs(countLOCs(treeWalk.getPathString(), rel.getExtendedName()));
        rf.insertChurn(0);
        rf.setRevisionFirstAppearance(releaseNumber);
        rf.insertAvgSetSize(avgSetSize(repository, treeWalk.getPathString(), rel.getExtendedName(), null));


        if (releaseNumber > 1) {
            Map<String, Integer> ar = countAuthAndRevs(repository, treeWalk.getPathString(), rel.getExtendedName(), relNames.get(releaseNumber - 2).getExtendedName());
            rf.insertAuth(ar.get(AUTHORSFIELD));
            rf.insertRevisions(ar.get(REVISIONSFIELD));

            Map<String, Integer> r = locTouched(repository, relNames.get(releaseNumber - 2).getExtendedName(), rel.getExtendedName(), treeWalk.getPathString());
            rf.insertTouchedLOCs(r.get(TOUCHEDFIELD));
            rf.insertLOCAdded(r.get(ADDEDFIELD));
        } else {
            Map<String, Integer> ar = countAuthAndRevs(repository, treeWalk.getPathString(), rel.getExtendedName(), null);
            rf.insertAuth(ar.get(AUTHORSFIELD));
            rf.insertRevisions(ar.get(REVISIONSFIELD));

            //è la prima release, quindi loc touched e anche le added sono tutte le loc
            rf.insertTouchedLOCs(rf.getLocs().get(0));
            rf.insertLOCAdded(rf.getLocs().get(0));
        }
        rf.insertWeightedAge(releaseNumber);
        files.add(rf);
    }

    /**
     * This method is used only for that releases that are marked with '-Mx' which are manteinance releases. Their aim is to fix bugs.
     * So it is useless to calculate metrics for this releases, we only want information about the fixes.
     */
    public static void listRepoContentMantReleases(Version rel, int releaseNumber) throws IOException {

        ObjectId head = repository.resolve(rel.getExtendedName());
        if (head == null)
            return;

        // a RevWalk allows to walk over commits based on some filtering that is defined
        RevWalk walk = new RevWalk(repository);

        RevCommit commit = walk.parseCommit(head.toObjectId());

        RevTree tree = commit.getTree();


        // now use a TreeWalk to iterate over all files in the Tree recursively
        // you can set Filters to narrow down the results if needed
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        int ret;
        String[] tkns;

        while (treeWalk.next()) {
            if (treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test")) {

                tkns = treeWalk.getPathString().split("/");

                ret = iterateAndCompareFiles(tkns[tkns.length - 1], treeWalk.getPathString()); //fondamentalmente va bene solo se ha lo stesso nome

                if (ret >= 0) {
                    listRepoContentMantNotFirstApp(ret, rel, treeWalk);
                } else {
                    listRepoContentMantFirstApp(tkns, rel, releaseNumber, treeWalk);
                }
            }
        }
    }


    public static void listRepoContentMantNotFirstApp(int ret, Version rel, TreeWalk treeWalk) {

        files.get(ret).insertRelease(rel);
        files.get(ret).insertPath(treeWalk.getPathString());
        files.get(ret).insertLOCs(-1);
        files.get(ret).insertChurn(-1);
        files.get(ret).insertAvgSetSize(-1);
        files.get(ret).insertAuth(-1);
        files.get(ret).insertRevisions(-1);
        files.get(ret).insertTouchedLOCs(-1);
        files.get(ret).insertLOCAdded(-1);
        files.get(ret).insertWeightedAge(-1);
    }



    public static void listRepoContentMantFirstApp(String[] tkns, Version rel, int releaseNumber, TreeWalk treeWalk){
        RepoFile rf = new RepoFile(tkns[tkns.length - 1]);
        rf.insertRelease(rel);
        rf.insertPath(treeWalk.getPathString());
        rf.insertLOCs(-1);
        rf.insertChurn(-1);
        rf.setRevisionFirstAppearance(releaseNumber);
        rf.insertAvgSetSize(-1);
        rf.insertAuth(-1);
        rf.insertRevisions(-1);
        rf.insertTouchedLOCs(-1);
        rf.insertLOCAdded(-1);
        rf.insertWeightedAge(-1);

        files.add(rf);
    }


    public static int countLOCs(String filePath, String release) throws IOException {
        RevWalk walk = new RevWalk(repository);
        ObjectId headId = repository.resolve(release);
        RevCommit commit = walk.parseCommit(headId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        int lines = 0;
        BufferedReader reader = null;

        try {
            while (treeWalk.next()) {
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                reader = new BufferedReader(new InputStreamReader(loader.openStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lines++;
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert reader != null;
            reader.close();
        }


        return lines;
    }


    public static Map<String, Integer> countAuthAndRevs(Repository repository, String file, String currentRelease, String prevRelease) throws IOException, GitAPIException {
        RevWalk walk = new RevWalk(repository);
        Git git = new Git(repository);
        int nAuth = 0;
        int nComm = 0;
        ArrayList<String> authors = new ArrayList<>();
        Map<String, Integer> ret = new HashMap<>();

        LogCommand log;

        ObjectId objcurId;
        ObjectId objprevId = null;
        RevCommit curRelCommit;
        RevCommit prevRelCommit = null;


        // prendi gli object id delle release
        objcurId = repository.resolve(currentRelease);
        if (prevRelease != null) {
            objprevId = repository.resolve(prevRelease);
        }

        curRelCommit = walk.parseCommit(objcurId);
        if (objprevId != null) {
            prevRelCommit = walk.parseCommit(objprevId);
        }

        if (prevRelCommit != null) {
            log = git.log().addRange(prevRelCommit, curRelCommit);
        } else {
            log = git.log().addRange(theOldestCommit, curRelCommit); //add prende lo start commit e quindi fa da la fino alla fine.
        }

        Iterable<RevCommit> commits = log.addPath(file).call();

        ArrayList<RevCommit> relCommits = new ArrayList<>();
        for (RevCommit commit : commits) {
            relCommits.add(commit);
        }

        for (RevCommit com : relCommits) {

            nComm++; //conto i commit che toccano il mio file

            if (!authors.contains(com.getAuthorIdent().getName())) {
                nAuth++; //conto gli autori che hanno toccato il file
                authors.add(com.getAuthorIdent().getName());
            }

        }
        ret.put(AUTHORSFIELD, nAuth);
        ret.put(REVISIONSFIELD, nComm);
        return ret;
    }


    /**
     * Returns:
     * - the number of touched LOCs (added + removed)
     * - the number of added LOCs
     * These values are put in a Map which is returned.
     */
    public static Map<String, Integer> locTouched(Repository repository, String startRel, String endRel, String fileName) throws IOException {

        RevWalk walk = new RevWalk(repository);
        int locTouched = 0;
        int locAdded = 0;

        Map<String, Integer> ret = new HashMap<>();

        // get the commit id
        ObjectId startCommit = repository.resolve(startRel);
        ObjectId endCommit = repository.resolve(endRel);

        RevCommit start = walk.parseCommit(startCommit);
        RevCommit end = walk.parseCommit(endCommit);


        // Obtain tree iterators to traverse the tree of the old/new commit
        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, start.getTree());
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, end.getTree());

        // Use a DiffFormatter to compare new and old tree and return a list of changes
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repository);
        diffFormatter.setContext(0);
        List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

        for (DiffEntry entry : entries) {
            if (entry.getNewPath().equals(fileName)) {
                FileHeader fileHeader = diffFormatter.toFileHeader(entry);
                ArrayList<Edit> edits = fileHeader.toEditList();


                int locsA = 0;
                int locsD = 0; // mi servono per mantenere le LOCs added e deleted
                for (Edit e : edits) {

                    if (e.toString().contains("INSERT")) {
                        locsA += e.getLengthB();

                    } else if (e.toString().contains("DELETE")) {
                        locsD += e.getLengthA();

                    }
                }
                locTouched = (locsA + locsD);
                locAdded = locsA;
                break; //ogni DiffEntry corrisponde alle modifiche di un singolo file, quindi se si arriva qui quelle del file d'interesse sono state controllate tutte
            }

        }

        ret.put(TOUCHEDFIELD, locTouched);
        ret.put(ADDEDFIELD, locAdded);
        return ret;
    }


    /**
     * Ritorna, per la release, quanti file mediamente vengono toccati nei commit insieme al file specificato
     */
    public static int avgSetSize(Repository repository, String file, String currentRelease, String prevRelease) throws IOException, GitAPIException {

        RevWalk walk = new RevWalk(repository);
        Git git = new Git(repository);

        ObjectReader reader = null;
        try {
            reader = repository.newObjectReader();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            assert reader != null;
            reader.close();
        }

        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();

        ObjectId objcurId;
        ObjectId objprevId = null;
        RevCommit curRelCommit;
        RevCommit prevRelCommit = null;

        LogCommand log;

        // prendi gli object id delle release
        objcurId = repository.resolve(currentRelease);
        if (prevRelease != null) {
            objprevId = repository.resolve(prevRelease);
        }

        curRelCommit = walk.parseCommit(objcurId);
        if (objprevId != null) {
            prevRelCommit = walk.parseCommit(objprevId);
        }

        if (prevRelCommit != null) {
            log = git.log().addRange(prevRelCommit, curRelCommit);
        } else {
            log = git.log().addRange(theOldestCommit, curRelCommit); //add prende lo start commit e quindi fa da la fino alla fine.
        }

        Iterable<RevCommit> commits = log.call();

        ArrayList<RevCommit> relCommits = new ArrayList<>();
        for (RevCommit com : commits) {
            relCommits.add(com);
        }

        ArrayList<String> filesSet = new ArrayList<>();

        for (RevCommit commit : relCommits) {
            if (relCommits.indexOf(commit) == relCommits.size() - 1)
                break;

            RevTree tree1 = commit.getTree();
            newTreeIter.reset(reader, tree1);

            RevTree tree2 = relCommits.get(relCommits.indexOf(commit) + 1).getTree();
            oldTreeIter.reset(reader, tree2);

            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);     // classi cambiate tra due commit

            for (DiffEntry entry : entries) {
                if (!filesSet.contains(entry.getNewPath()) && !entry.getNewPath().equals(file))
                    filesSet.add(entry.getNewPath());
            }
        }
        reader.close();
        return filesSet.size() / relCommits.size();
    }


    /**
     * Retrieve all the commits and save only the ones which have a ticket in their message
     *
     * @param repository
     * @throws IOException
     * @throws GitAPIException
     */

    public static void retrieveTicketsFromCommit(Repository repository) throws IOException, GitAPIException {
        Git git = new Git(repository);
        ArrayList<RevCommit> commitsMsgs = new ArrayList<>();
        for (RevCommit commit : git.log().all().call()) {
            commitsMsgs.add(commit);
        }

        //dai tag presi da jira filtra tutti i commit di git mantenendo solo quelli che contengono un ticket
        for (RevCommit c : commitsMsgs) {
            for (Tickets t : tickets) {
                if (c.getShortMessage().contains(t.getName())) {
                    t.setCommitId(c.getName()); //aggiungo il commitID con cui è stato fatto quel fix
                    buggyRelCommits.add(t);
                    break; //check the next commit
                }
            }
        }
    }


    /* questa cosa devo farla quando ho terminato di controllare tutte le metriche per ogni file di ogni release. Questo perchè quando trovo una classe che in una certa release
     * è stata fixata allora devo andare a settare a buggy quella classe in tutte quante le release precedenti (dall' IV). Lo faccio così:
     * - se sul ticket di jira c'è l'IV allora torno indietro tra release fino a quella settando a buggy la classe
     * - se non c'è l'IV invece faccio proportion, per il quale ho bisogno di OV e FV (che ho per tutte)
     *  */
    public static int checkIfBuggy(Repository repository, String currentRelease, String prevRelease, RepoFile file) throws IOException, GitAPIException {
        RevWalk walk = new RevWalk(repository);
        Git git = new Git(repository);
        int nFix = 0;
        TicketList bugs = new TicketList();

        LogCommand log;

        ObjectId objcurId;
        ObjectId objprevId = null;
        RevCommit curRelCommit;
        RevCommit prevRelCommit = null;


        // prendi gli object id delle release
        objcurId = repository.resolve(currentRelease);
        if (prevRelease != null) {
            objprevId = repository.resolve(prevRelease);
        }

        curRelCommit = walk.parseCommit(objcurId);
        if (objprevId != null) {
            prevRelCommit = walk.parseCommit(objprevId);
        }

        if (prevRelCommit != null) {
            log = git.log().addRange(prevRelCommit, curRelCommit);
        } else {
            log = git.log().addRange(theOldestCommit, curRelCommit); //add prende lo start commit e quindi fa da la fino alla fine, ecco perchè c'è add range.
        }

        Iterable<RevCommit> commits = log.addPath(file.getPaths().get(0)).call();

        ArrayList<RevCommit> relCommits = new ArrayList<>();
        for (RevCommit commit : commits) {
            relCommits.add(commit);
        }

        /* per il seguente file prendi i commit relativi ad un ticket: significa che con quel commit il file (la classe) è stata fixata da un bug */
        for (RevCommit com : relCommits) {

            if (buggyRelCommits.containsCommit(com.getName())) { //ho creato una classe che estende ArrayList in modo da poter avere un contains che controlla solo il nome del commit
                //significa che è buggy
                bugs.add(buggyRelCommits.getFromCommitId(com.getName())); //ho tutti i ticket dei bug che affettano questo file
                nFix++; //conto quanti commit (con ticket) hanno toccato quel file
            }

        }

        if (nFix > 0) {
            /* devo andare a settare a buggy il file in tutte le release precedenti fino alla INJECTED VERSION (che su jira corrisponde alla AFFECTED VERSION) */
            buggynessForFile(file, bugs);
        }
        //ritorno il numero di commit che hanno fixato il file, se è pari a 0 allora non è stato fixato
        return nFix;

    }


    public static void setAffectedVersions(TicketList bugs, List<Version> fileRels, RepoFile f){
        for (Tickets t : bugs) {
            if (t.getAffectedVersions().size() > 1) {
                int firstAffRel = fileRels.indexOf(t.getAffectedVersions().get(0));
                if (firstAffRel < 0) {
                    continue;
                }
                int lastAffRel = fileRels.indexOf(t.getAffectedVersions().get(t.getAffectedVersions().size() - 1)) + 1;
                for (int i = firstAffRel; i < lastAffRel; i++) {
                    f.getBuggy().set(i, true);

                }

            }
        }
    }


    public static void buggynessForFile(RepoFile file, TicketList bugs){
        for (RepoFile f : files) { //scorro tutti i file e arrivo fino a quello d'interesse

            if (f.getPaths().get(0).equals(file.getPaths().get(0))) { //controllo il primo, tanto in paths c'è lo stesso ripetuto per ogni release in cui esiste
                /* a questo punto prendo la lista delle release del file, per ogni bug (etichettato dal ticket t) presente in bugs setto a buggy le release del file corrispondenti
                 * all'affected versions presenti in t */
                List<Version> fileRels = f.getReleases();
                setAffectedVersions(bugs, fileRels, f);
                break; //il file una volta sola compare nella lista
            }
        }
    }


    /**
     * Get the oldest commit of the repository. The oldest commit is needed when there is the necessity do make a log beetween two commit in caso of the first release,
     * so that it is possible to callo log().addRange(theOldest, release).
     *
     * @param repository
     */
    public static void getTheOldestCommit(Repository repository) {
        RevWalk walk = new RevWalk(repository);
        RevCommit c = null;
        AnyObjectId headId;
        try {
            headId = repository.resolve(Constants.HEAD);
            RevCommit root = walk.parseCommit(headId);
            walk.sort(RevSort.REVERSE);
            walk.markStart(root);
            c = walk.next();
        } catch (IOException e) {
            e.printStackTrace();
        }

        theOldestCommit = c;
    }


    public static void retrieveMetrics() throws IOException, GitAPIException {

        int relNum = 0;
        int ret;
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder
                .setGitDir(new File(repoPath)).readEnvironment()
                .findGitDir().build();


        getTheOldestCommit(repository); // prendi l'id del primo commit di sempre

        retrieveReleasesFromJira(); // prendi la lista di tutte le release: vengono filtrate quelle di jira con quelle prese dai tag di git


        // per ogni release (tag) lista i file e calcola le metriche
        for (Version release : relNames) {
            //per ogni branch cerca tutti i file - excludi HEAD e master
            relNum++;
            if(release.getExtendedName().contains("2.0.0-M1")) {
                listRepoContentMantReleases(release, relNum); // don't calculate metrics for maintenance release, just take classes for buggyness
            }else {
                listRepositoryContents(release, relNum); // calcola le metriche
            }
        }

        // calcola e setta la buggyness
        retrieveTickets(projName);
        retrieveTicketsFromCommit(repository);

        for (Version rel : relNames) {
            for (RepoFile f : files) {
                if (relNames.indexOf(rel) == 0) {
                    ret = checkIfBuggy(repository, rel.getExtendedName(), null, f);
                }else {
                    ret = checkIfBuggy(repository, rel.getExtendedName(), relNames.get(relNames.indexOf(rel) - 1).getExtendedName(), f);
                }
                f.insertnFix(ret);
            }
        }


        // scarta l'ultimo 50% delle release
        if(projName.equals("BOOKKEEPER")) {
            halveReleases();
        }


        // Scrivi il file csv
        writeOnFile();

        repository.close();
    }

    public static void main(String[] args) throws IOException, GitAPIException {

        ProjectsUtils.getInstance();

        files = new ArrayList<>();
        tags = new ArrayList<>();
        buggyRelCommits = new TicketList();
        projName = ProjectsUtils.getProjectNames().get(0);
        repoPath = ProjectsUtils.getRepoPath().get(0);
        retrieveMetrics();

        // re-init the status of the program
        files = null;
        repoPath = null;
        projName = null;
        tags = null;
        repository = null;
        buggyRelCommits = null;
        theOldestCommit = null;


        files = new ArrayList<>();
        tags = new ArrayList<>();
        buggyRelCommits = new TicketList();
        projName = ProjectsUtils.getProjectNames().get(1);
        repoPath = ProjectsUtils.getRepoPath().get(1);
        retrieveMetrics();

    }


}
