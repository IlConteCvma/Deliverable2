package logic.dataset.metrics;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;


import logic.dataset.GitQuery;
import logic.model.AnalyzedClass;

public class ChangeSetMetric extends AdvanceMetric{
	
	
	public ChangeSetMetric(String path,GitQuery query) {
		super(path,query);
		
	}

	@Override
	protected void analysis(AnalyzedClass analyzedClass, List<RevCommit> commits) throws GitAPIException, IOException  {
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
	protected void setdefault(AnalyzedClass analyzedClass) {
		//set analyzedClass chgSetSize
		analyzedClass.setChgSetSize(0);
		//set analyzedClass maxchgSet
		analyzedClass.setMaxChgSet(0);
		//set analyzedClass avgchgSet
		analyzedClass.setAvgChgSet(0);
		
		
	}

}
