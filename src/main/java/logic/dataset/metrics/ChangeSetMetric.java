package logic.dataset.metrics;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import excption.MetricException;
import logic.dataset.GitQuery;
import logic.model.AnalyzedClass;

public class ChangeSetMetric extends Metric {
	
	private GitQuery query;
	
	public ChangeSetMetric(String path,GitQuery query) {
		super(path);
		this.query = query;
	}

	private void analysis(AnalyzedClass analyzedClass, List<RevCommit> commits) throws GitAPIException, IOException  {
		int chgSetSize = 0;
		int maxchgSet = 0;
		int nr = 0;
		int tmpChgSet;
		List<DiffEntry> entries;
		
		for (RevCommit revCommit : commits) {
			//obtain list of file
			entries = query.getChangedFileList(revCommit);
			if (entries == null) {
	    		nr++;
				break;
			}
			//number of file excluding analyzedClass
			tmpChgSet = entries.size() - 1;
			
			//check max chgSet
			if (tmpChgSet > maxchgSet) {
				maxchgSet = tmpChgSet;
			}
			
			//add tmpChgSet to total
			chgSetSize += tmpChgSet;
					
			// add a revision
			nr++;
		}
		
		//set analyzedClass chgSetSize
		analyzedClass.setChgSetSize(chgSetSize);
		//set analyzedClass maxchgSet
		analyzedClass.setMaxChgSet(maxchgSet);
		//set analyzedClass avgchgSet
		if (nr == 0) {
			throw new IOException("Number of revision egual to 0");
		}else {
			analyzedClass.setAvgChgSet(chgSetSize/nr);
		}
		
	}
	
	
	
	@Override
	public void startAnalysis(List<AnalyzedClass> classes) throws MetricException {
		List<RevCommit> commits;
		
		
		for (AnalyzedClass analyzedClass : classes) {
			try {
				//get the revisions for analyzedClass
				commits = query.getCommitsForAFile(analyzedClass.getPath());
				if (commits.isEmpty()) {
					setdefault(analyzedClass);
				}
				else {
					analysis(analyzedClass, commits);
				}
						
			} catch (GitAPIException | IOException e) {
				throw new MetricException("Error get commits for class: "+ analyzedClass.getPath()+", "+ e.getMessage() , e.getStackTrace());
			}


		}

	}

	private void setdefault(AnalyzedClass analyzedClass) {
		//set analyzedClass chgSetSize
		analyzedClass.setChgSetSize(0);
		//set analyzedClass maxchgSet
		analyzedClass.setMaxChgSet(0);
		//set analyzedClass avgchgSet
		analyzedClass.setAvgChgSet(0);
		
		
	}

}
