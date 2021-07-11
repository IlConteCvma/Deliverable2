package logic.dataset;


import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import logic.model.Branch;
import logic.model.Release;
import logic.model.Ticket;





public class GitQuery {
	
	private Git git;
	private Path folderPath;
	private File localRepoFolder;
	private String projectName ;
	private static final Logger LOG = LoggerFactory.getLogger(GitQuery.class);
	private String actualBranch;
	
	public GitQuery(String project) {
		
		this.projectName = project;
		this.folderPath = Paths.get(System.getProperty("user.dir"));
		this.actualBranch = null;
		
	}
	
	public void cloneRepositoryFromURL(String url) throws GitAPIException, IOException  {
		this.localRepoFolder = Files.createTempDirectory(folderPath, projectName).toFile();
		this.git = Git.cloneRepository().setURI(url)
				.setDirectory(localRepoFolder).call();
	}
	
	public void openRepository(String path) throws IOException {
		this.localRepoFolder = new File(path);
		LOG.info("OPEN: {}.", localRepoFolder);
		git = Git.open(localRepoFolder);
	}
	
	public File getLocalRepoFolder() {
		return this.localRepoFolder;
	}
	
	public Repository getRepository() {
		return git.getRepository();
	}
	
	public void deleteRepository() throws IOException {
		
		FileUtils.delete(localRepoFolder, FileUtils.RECURSIVE);
		
	}
	
	
	public void changeBranch(String target) throws GitAPIException {
		if(actualBranch == null) {
			actualBranch = target;
		}else if (!actualBranch.equals(target)) {
			git.checkout().setName(target).call();
		}
		
	}
	
	public List<RevCommit> getCommitsForAFile(String path) throws GitAPIException {
		List<RevCommit> returnCommits = new ArrayList<>();
		Repository repo = this.git.getRepository();
		Git gita = new Git(repo);

		Iterable<RevCommit> tmp = git.log().addPath(FilenameUtils.separatorsToUnix(path)).call();
		
		for (RevCommit revCommit : tmp) {
			returnCommits.add(revCommit);
		}
		
		gita.close();
		return returnCommits;
	}
	
	//return null if revCommit has no parents
	public List<DiffEntry> getChangedFileList(RevCommit revCommit) throws GitAPIException, IOException  {
		Repository repo = this.git.getRepository();
		List<DiffEntry> returnDiffs = null;
		int count = revCommit.getParentCount();
		if(count==0)
			return returnDiffs;
			
		ObjectId head=revCommit.getTree().getId();
		ObjectId oldHead = null;
		if (revCommit.getParentCount()>0) {
			oldHead=revCommit.getParent(0).getTree().getId();
		}else {
			return returnDiffs;
		}
		 

        // prepare the two iterators to compute the difference between
		ObjectReader reader = repo.newObjectReader();
		CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
		oldTreeIter.reset(reader, oldHead);
		CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
		newTreeIter.reset(reader, head);
 
        // finally get the list of changed files
    	Git git2 = new Git(repo);
    	
        returnDiffs =  git2.diff()
		                    .setNewTree(newTreeIter)
		                    .setOldTree(oldTreeIter)
		                    .call();
        
		git2.close();
		
        return returnDiffs;  		
		
	}
	
	
	public List<Branch> getBranchs() throws GitAPIException{
		List<Branch> branches = new ArrayList<>();
		List<Ref> call = this.git.branchList().setListMode(ListMode.ALL).call();
		Branch branch;
		
		for (Ref ref : call) {
			String refName = ref.getName();
			branch = new Branch(refName,ref.getObjectId());
			branches.add(branch);
		}
		return branches;
		
	}

	public void setBranchForReleases(List<Release> releases) throws GitAPIException {
		List<Ref> call = this.git.branchList().setListMode(ListMode.ALL).call();
		Branch branch;
		int tmp;
		
		for (Release release : releases) {
			
			
			if (release.getVersionName().length() == 5) {
				tmp = 3;
			}else {
				tmp = 4;
			}
			
			//control if exist exact branch for the actual release
			for (Ref ref : call) {
				String refName = ref.getName();
				if (refName.endsWith(release.getVersionName().subSequence(0, tmp).toString())) {
					branch = new Branch(refName,ref.getObjectId());
					
					release.setBranch(branch);
					
					break;
				}
			}
				
		}
		
		
		
	}

	
	public List<Branch> getBranchesForReleases(List<Release> releases) throws GitAPIException {
		List<Branch> branches = new ArrayList<>();
		List<Ref> call = this.git.branchList().setListMode(ListMode.ALL).call();
		Branch branch;
		int tmp;
		for (Ref ref : call) {
			String refName = ref.getName();
			if (refName.contains("origin/master")) {
				branch = new Branch(refName,ref.getObjectId());
				branches.add(branch);
			}
			
			//filtering on releases
			for (Release release : releases) {
				
				
				if (release.getVersionName().length() == 5) {
					tmp = 3;
				}else {
					tmp = 4;
				}
				
				if (refName.endsWith(release.getVersionName().subSequence(0, tmp).toString())) {
					branch = new Branch(refName,ref.getObjectId());
					branches.add(branch);
					break;
				}
			}
		}		
		return branches;
	}
	
	
	
	/*
	 * For BOOKKEEPER ticket.getKey()+":"
	 * For ZOOKEEPER ticket.getKey()+"."
	 * */
	public boolean checkCommit(Ticket ticket) throws GitAPIException {
		
		
		Iterable<RevCommit> commits = this.git.log().call();
		
		Iterator<RevCommit> itr = commits.iterator();
		while (itr.hasNext()) {
			RevCommit element = itr.next();
			String comment = element.getFullMessage();

			if (comment.contains(ticket.getKey()+".") || comment.contains(ticket.getKey()+":") 
					|| comment.contains(ticket.getKey()+" :")) {
				
				PersonIdent authorIdent = element.getAuthorIdent();
				Date date = authorIdent.getWhen();
				LocalDate date2 = date.toInstant().atZone(authorIdent.getTimeZone().toZoneId()).toLocalDate();
				ticket.setResolutionDate(date2);
				ticket.setCommit(element);
				
				return true;
			}
			
		}
		
		
		
		
		
		return false;
		
	}	
	

	

	

		
	

	
	
	
}
