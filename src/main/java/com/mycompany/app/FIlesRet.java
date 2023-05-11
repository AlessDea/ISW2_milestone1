package com.mycompany.app;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;


import java.io.*;
import java.util.*;

import static com.mycompany.app.getReleaseInfo.relNames;
import static com.mycompany.app.getReleaseInfo.retrieveReleases;

public class FIlesRet {

    public static ArrayList<RepoFile> files = new ArrayList<>();
    //public static String repo_path = "/home/alessandrodea/Scrivania/uni/Magistrale/isw2/isw 22-23/projects/syncope/.git";
    public static String repo_path = "/home/alessandrodea/Scrivania/uni/Magistrale/isw2/isw 22-23/projects/bookkeeper/.git";
    //public static String projName = "SYNCOPE";
    public static String projName = "BOOKKEEPER";
    public static List<Ref> branches = new ArrayList<>();
    public static List<Ref> tags = new ArrayList<>();
    public static Repository repository;

    public static ArrayList<Release> releases = new ArrayList<>();



    /*
    * Le release sono quelle i cui commit hanno un tag. Notare che quelli validi sono quelli dalla release 1.2.1, quelli
    * precedenti non fanno parte della repository principale ma di un fork probabilmente.
    *
    * */
    /*public static void retrieveTags() throws GitAPIException, IOException {
        Git jGit = new Git(repository);
        int len;
        List<Ref> call = jGit.tagList().call();

        RevWalk walk = new RevWalk(repository);

        for (Ref ref : call) {
            RevCommit commit = repository.parseCommit(ref.getObjectId());

            //System.out.println("Tag: " + ref.getName() + " Commit: " + ref.getObjectId().getName());// + " Msg: " + commit.getFullMessage());

            RevTag tag = walk.parseTag(ref.getObjectId());

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(tag.getTaggerIdent().getWhen().getTime());

            releases.add(new Release(ref, calendar));


        }

        releases.sort(new Comparator<Release>() {
            @Override
            public int compare(Release o1, Release o2) {
                return o1.getDate().getTime().compareTo(o2.getDate().getTime());
            }
        });

        for(Release r : releases){
            tags.add(r.getRef()); //li inserisco in tag in modo ordinato

            int year = r.getDate().get(Calendar.YEAR);
            int month = r.getDate().get(Calendar.MONTH) + 1;
            int day = r.getDate().get(Calendar.DAY_OF_MONTH);
            System.out.println("Date: " + day + "/" + month + "/"+ year);
        }


        // scarta l'ultimo 50% delle release
        len = tags.size();
        System.out.println(len);
        for(int i =  len - 1; i > len/2; i--){
            tags.remove(i);
            releases.remove(i);
        }
    }*/


    /*
    * Una volta prese tutte le release vado a vedere a quei commit tutti i cambiamenti fatti ad ogni file
    * */
    /*public static List<Ref> retrieveBranches() throws GitAPIException {
        Git jGit = new Git(repository);
        List<Ref> call = jGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        int n = 0;
        for (Ref ref : call) {
            if(!ref.getName().contains("HEAD") && !ref.getName().contains("master")){
                System.out.println("Branch: " + ref.getName() + " " + ref.getObjectId().getName());
                n++;
            }
        }
        System.out.println(n);
        return call;
    }*/


    public static void writeOnFile(){
        FileWriter fileWriter = null;
        int numVersions;
        int j;
        try {
            String outname = projName + "FilesInfo.csv";
            //Name of CSV for output
            fileWriter = new FileWriter(outname);
            fileWriter.append("Version, Version Name, Name, LOCs, Churn, Age, Number of Authors, Revisions, LOC Touched");
            fileWriter.append("\n");
            numVersions = relNames.size();
            for (int i = 0; i < numVersions; i++) {

                for (RepoFile file : files) {

                    if ((i >= file.getRevisionFirstAppearance() - 1) && (file.getAppearances() > 0)) {
                        fileWriter.append(Integer.toString(i+1));
                        fileWriter.append(",");

                        fileWriter.append(relNames.get(i));
                        fileWriter.append(",");

                        fileWriter.append(file.getPaths().get(0));
                        fileWriter.append(",");

                        fileWriter.append(file.getLOCs().get(0).toString());
                        fileWriter.append(",");

                        fileWriter.append(file.getChurn().get(0).toString());
                        fileWriter.append(",");

                        fileWriter.append((Integer.toString(i - file.getRevisionFirstAppearance() + 1)));
                        fileWriter.append(",");

                        fileWriter.append((file.getnAuth().get(0).toString()));
                        fileWriter.append(",");

                        fileWriter.append(file.getRevisions().get(0).toString());
                        fileWriter.append(",");

                        fileWriter.append(file.getTouchedLOCs().get(0).toString());

                        fileWriter.append("\n");

                        file.getPaths().remove(0);
                        file.getLOCs().remove(0);
                        file.getChurn().remove(0);
                        file.getnAuth().remove(0);
                        file.getRevisions().remove(0);
                        file.getTouchedLOCs().remove(0);

                        file.decAppearances();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error in csv writer");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
        System.out.println("File correctly written");
    }

    public static int iterateAndCompareFiles(String name, String path, int relNum){
        for(RepoFile f : files){
            if(f.equals(name)){
                if(f.getPaths().size() > 0) {
                    if (path.equals(f.getPaths().get(f.getPaths().size()-1))) {
                        return files.indexOf(f);
                    }
                }
            }
        }
        return -1;
    }

    public static void listRepositoryContents(String rel, int releaseNumber) throws IOException, GitAPIException {

        System.out.println(rel);

        ObjectId head = repository.resolve(rel);
        if(head == null) //TODO: controlla cosa fare qua, perchè la seconda release torna null
            return;

        //System.out.println(head.getName());
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
            if(treeWalk.getPathString().contains(".java")) {

                tkns = treeWalk.getPathString().split("/");
                if(!treeWalk.getPathString().contains("/test")){
                    //System.out.println("Release Number: " + releaseNumber + " name: " + treeWalk.getPathString());
                    ret = iterateAndCompareFiles(tkns[tkns.length - 1], treeWalk.getPathString(), releaseNumber-1);

                    if (ret >= 0) {

                        files.get(ret).incAppearances();
                        files.get(ret).insertRelease(rel);
                        files.get(ret).insertPath(treeWalk.getPathString());
                        files.get(ret).insertLOCs(countLOCs(treeWalk.getPathString(), rel));
                        files.get(ret).insertChurn(files.get(ret).getReleases().size() - 1);

                        if(releaseNumber > 1){
                            Map<String, Integer> ar = countAuthAndRevs(repository, treeWalk.getPathString(), rel, relNames.get(releaseNumber-2));
                            files.get(ret).insertAuth(ar.get("authors"));
                            files.get(ret).insertRevisions(ar.get("revisions"));

                            Map<String, Integer> r = locTouched(repository, relNames.get(releaseNumber-2), rel, treeWalk.getPathString());
                            files.get(ret).insertTouchedLOCs(r.get("touched"));
                            files.get(ret).insertLOCAdded(r.get("added"));
                        }else{
                            Map<String, Integer> ar = countAuthAndRevs(repository, treeWalk.getPathString(), rel, null);
                            files.get(ret).insertAuth(ar.get("authors"));
                            files.get(ret).insertRevisions(ar.get("revisions"));

                            //è la prima release, quindi loc touched e anche le added sono tutte le loc
                            files.get(ret).insertTouchedLOCs(files.get(ret).getLOCs().get(files.get(ret).getLOCs().size() - 1));
                            files.get(ret).insertLOCAdded(files.get(ret).getLOCs().get(files.get(ret).getLOCs().size() - 1));
                        }

                    } else {
                        //System.out.println("else: Release Number: " + releaseNumber + " name: " + treeWalk.getPathString());
                        RepoFile rf = new RepoFile(tkns[tkns.length - 1]);
                        rf.insertRelease(rel);
                        rf.insertPath(treeWalk.getPathString());
                        rf.insertLOCs(countLOCs(treeWalk.getPathString(), rel));
                        rf.insertChurn(0);
                        rf.setRevisionFirstAppearance(releaseNumber);

                        if(releaseNumber > 1) {
                            Map<String, Integer> ar = countAuthAndRevs(repository, treeWalk.getPathString(), rel, relNames.get(releaseNumber-2));
                            rf.insertAuth(ar.get("authors"));
                            rf.insertRevisions(ar.get("revisions"));

                            Map<String, Integer> r = locTouched(repository, relNames.get(releaseNumber - 2), rel, treeWalk.getPathString());
                            rf.insertTouchedLOCs(r.get("touched"));
                            rf.insertLOCAdded(r.get("added"));
                        }else{
                            Map<String, Integer> ar = countAuthAndRevs(repository, treeWalk.getPathString(), rel, null);
                            rf.insertAuth(ar.get("authors"));
                            rf.insertRevisions(ar.get("revisions"));

                            //è la prima release, quindi loc touched e anche le added sono tutte le loc
                            rf.insertTouchedLOCs(rf.getLOCs().get(0));
                            rf.insertLOCAdded(rf.getLOCs().get(0));
                        }
                        files.add(rf);
                    }
                }
            }
        }
    }

    /**
     *
     * */
    public static int countLOCs(String filePath, String release) throws IOException, GitAPIException {
        RevWalk walk = new RevWalk(repository);
        ObjectId headId = repository.resolve(release);
        RevCommit commit = walk.parseCommit(headId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        int lines = 0;
        while (treeWalk.next()) {
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines++;
                }
            }
        }
        return lines;
    }

    /**
     * TODO: devi farlo fra due release poichè se glie ne passi una sola prende tutti i commit fino a quel punto (partendo dal primo di sempre)
     * */
    public static int countAuthorsInFile(String filePath, String currentRel) throws IOException, GitAPIException {
        int authorsCount = 0;


        ObjectId to = repository.resolve(currentRel); //resolve the commit id from the tag name


        BlameResult blameResult = new Git(repository).blame()
                .setFilePath(filePath)
                .setStartCommit(to)
                .call();

        Set<String> authors = new HashSet<>();
        for (int i = 0; i < blameResult.getResultContents().size(); i++) {

            authors.add(blameResult.getSourceAuthor(i).getName());
            //System.out.println(blameResult.getSourceAuthor(i).getName());
        }

        authorsCount = authors.size();
        return authorsCount;
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
        if(prevRelease != null) {
            objprevId = repository.resolve(prevRelease);
        }

        curRelCommit = walk.parseCommit(objcurId);
        if(objprevId != null){
            prevRelCommit = walk.parseCommit(objprevId);
        }

        if(prevRelCommit != null) {
            log = git.log().addRange(prevRelCommit, curRelCommit);
        }else{
            log = git.log().add(curRelCommit);
        }

        Iterable<RevCommit> commits = log.addPath(file).call();

        ArrayList<RevCommit> relCommits = new ArrayList<>();
        for(RevCommit commit : commits){
            relCommits.add(commit);
        }

        for(RevCommit com : relCommits){

            nComm++; //conto i commit che toccano il mio file

            if(!authors.contains(com.getAuthorIdent().getName())) {
                nAuth++; //conto gli autori che hanno toccato il file
                authors.add(com.getAuthorIdent().getName());
            }

        }
        //System.out.println("Authors: " + nAuth + " Revisions: " + nComm);
        ret.put("authors", nAuth);
        ret.put("revisions", nComm);
        return ret;
    }



    /**
     * devi farlo fra due release poichè se glie ne passi una sola prende tutti i commit fino a quel punto (partendo dal primo di sempre) --> DONE
     *
     * */
    public static int countCommits(Repository repository, String file, String currentRelease, String prevRelease) throws IOException, GitAPIException {
        RevWalk walk = new RevWalk(repository);
        Git git = new Git(repository);
        int c = 0;

        LogCommand log;

        ObjectId objcurId = null;
        ObjectId objprevId = null;
        RevCommit curRelCommit = null;
        RevCommit prevRelCommit = null;

        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();

        // prendi gli object id delle release
        objcurId = repository.resolve(currentRelease);
        if(prevRelease != null) {
            objprevId = repository.resolve(prevRelease);
        }

        curRelCommit = walk.parseCommit(objcurId);
        if(objprevId != null){
            prevRelCommit = walk.parseCommit(objprevId);
        }



        // fai il log dell'ultima release e prendi tutti i commit
        //LogCommand log = git.log().add(curRelCommit);

        if(prevRelCommit != null) {
            log = git.log().addRange(prevRelCommit, curRelCommit);
        }else{
            log = git.log().add(curRelCommit); //add prende lo start commit e quindi fa da la fino alla fine.
        }

        Iterable<RevCommit> commits = log.call();

        ArrayList<RevCommit> relCommits = new ArrayList<>();
        for(RevCommit com : commits){
            relCommits.add(com);
        }

        for(RevCommit commit : relCommits) {

            if(relCommits.indexOf(commit) == relCommits.size()-1)
                break;


            RevTree tree1 = commit.getTree();
            newTreeIter.reset(reader, tree1);

            RevTree tree2 = relCommits.get(relCommits.indexOf(commit) + 1).getTree();
            oldTreeIter.reset(reader, tree2);

            DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE );
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            //System.out.println(commit + "     c: " + c);
            for( DiffEntry entry : entries ) {
                if(entry.getNewPath().equals(file)){
                    //System.out.println(entry.getNewPath());
                    c++;
                }

            }
        }

        //System.out.println(c);

        return c;
    }


    /**
     * Returns:
     * - the number of touched LOCs (added + removed)
     * - the number of added LOCs
     * These values are put in a Map which is returned.
     * */

    /* OK */
    public static Map<String, Integer> locTouched(Repository repository, String startRel, String endRel, String fileName) throws GitAPIException, IOException {
        RevWalk walk = new RevWalk(repository);
        int LOCTouched = 0;
        int LOCAdded = 0;
        int bA;
        int eA;
        int bB;
        int eB;
        Map<String, Integer> ret = new HashMap<>();

        // get the commit id
        ObjectId startCommit = repository.resolve(startRel);
        ObjectId endCommit = repository.resolve(endRel);

        RevCommit start = walk.parseCommit(startCommit);
        RevCommit end = walk.parseCommit(endCommit);

        /*System.out.println("Start commit: " + start.getName() + " -> " + start.abbreviate(6).name());
        System.out.println("End commit: " + end.getName() + " -> "+ end.abbreviate(6).name());*/

        // Obtain tree iterators to traverse the tree of the old/new commit
        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, start.getTree());
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, end.getTree());

        // Use a DiffFormatter to compare new and old tree and return a list of changes
        DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE );
        diffFormatter.setRepository(repository);
        diffFormatter.setContext(0);
        List<DiffEntry> entries = diffFormatter.scan( oldTreeIter, newTreeIter );

        // Print the contents of the DiffEntries
        for( DiffEntry entry : entries ) {
            if(!entry.getNewPath().equals(fileName))
                continue;

            //System.out.println(entry.getNewPath());

            FileHeader fileHeader = diffFormatter.toFileHeader(entry);
            ArrayList<Edit> edits = fileHeader.toEditList();


            int locsA = 0, locsD = 0; // mi servono per mantenere le LOCs added e deleted
            for (Edit e : edits) {
                bA = e.getBeginA();
                eA = e.getEndA();
                bB = e.getBeginB();
                eB = e.getEndB();

                //System.out.println(e);

                if (e.toString().contains("INSERT")){
                    locsA += eB-bB;

                }else if (e.toString().contains("DELETE")){
                    locsD += eA-eB;

                }else { //"REPLACE"
                    if(eA < eB) {
                        // es. A(26,27) B(26,29) -> added = (eB - eA) = 29 - 27 = 2 ; deleted = (eA - bA) = 27 - 26 = 1;
                        locsA += (eB - eA); //righe aggiunte
                        locsD += (eA - bA); //righe tolte
                    }else{
                        // es. A(26,29) B(26,27) -> (eA - eB) = 29 - 27 = 2
                        locsD += (eA - eB);
                    }
                }
            }
            LOCTouched += locsA + locsD;
            LOCAdded += locsA;
            break; //ogni DiffEntry corrisponde alle modifiche di un singolo file, quidni se si arriva qui quelle del file d'interesse sono state controllate tutte

        }
        //System.out.println("Total LOCs touched: " + resA - resD);

        ret.put("touched", LOCTouched);
        ret.put("added", LOCAdded);
        return ret;
    }



    public static void main(String[] args) throws IOException, GitAPIException {
        int relNum = 0;
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder
                .setGitDir(new File(repo_path)).readEnvironment()
                .findGitDir().build();


        retrieveReleases();


        long start = System.currentTimeMillis();


        // per ogni release (tag) lista i file
        for(String releaseName : relNames) {
            //per ogni branch cerca tutti i file - excludi HEAD e master
            relNum++;
            listRepositoryContents(releaseName, relNum);
        }

        // Scrivi il file
        writeOnFile();

        long end = System.currentTimeMillis();

        System.out.println("Elapsed time: " + (end - start)/1000.2f);

        repository.close();

    }

    /*public static void main(String[] args) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        repository = builder
                .setGitDir(new File(repo_path)).readEnvironment()
                .findGitDir().build();

        //retrieveTags();
    }*/

}
