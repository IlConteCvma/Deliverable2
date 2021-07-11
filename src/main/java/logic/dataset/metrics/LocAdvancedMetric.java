package logic.dataset.metrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import logic.dataset.GitQuery;
import logic.model.AnalyzedClass;

public class LocAdvancedMetric extends AdvanceMetric {
	
	
	public LocAdvancedMetric(String path, GitQuery query) {
		super(path,query);
		
	}
	
	private List<Integer> getAnalysLines(DiffFormatter df, DiffEntry diff) throws IOException {
		List<Integer> result = new ArrayList<>();
		int localLinesAdded = 0;
		int localLinesDeleted = 0;
		int localLinesModified = 0;
			
		//get modified region detected between two versions of roughly the same content
		for (Edit edit : df.toFileHeader(diff).toEditList()) {
			int beginA = edit.getBeginA();
			int beginB = edit.getBeginB();
			int endA =	edit.getEndA();
			int endB = edit.getEndB();
			
			if (beginA == endA && beginB < endB) {
				//is an insert edit, that is sequence B inserted the elements in region [beginB, endB) at beginA
				linesAdded += endB - beginB;
			}
			else if (beginA < endA && beginB == endB) {
				//is a delete edit, that is sequence B has removed the elements between [beginA, endA)
				linesDeleted += endA - beginA;
			}
			else if (beginA < endA && beginB < endB) {
				//is a replace edit, that is sequence B has replaced the range of elements between [beginA, endA) with those found in [beginB, endB)
				linesModified += endA - beginA;
				int total = (endB - beginB) - (endA - beginA);
				//control if change + add
				if (total > 0 ) 
					linesAdded += total;
								 
			}
 
        }//close edit
		
		//return lines
		result.add(localLinesAdded);
		result.add(localLinesDeleted);
		result.add(localLinesModified);
		
		return result;
		
	}
	private int nr;
	private int maxLinesAdded;
	private int linesAdded;
	private int linesDeleted;
	private int linesModified;
	private int churnSum;
	private int maxChurn;
	
	
	private DiffEntry getEntryClass (List<DiffEntry> entries,AnalyzedClass analyzedClass) {
		
		for (DiffEntry diffEntry : entries) {
			//filter only file analyzedClass
			if (analyzedClass.getPath().equals(FilenameUtils.separatorsToSystem(diffEntry.getNewPath()))) {
				return diffEntry;
				
			}
		}
		return null;
	}
	private void getResult(List<Integer> resultLines) {
		
		int revisionChurn;
		//add the result lines
		linesAdded    += resultLines.get(0);
		linesDeleted  += resultLines.get(1);
		linesModified += resultLines.get(2);
		
		//control max LinesAdded
		if (resultLines.get(0) > maxLinesAdded) {
			maxLinesAdded = resultLines.get(0);
		}
		// churn = added - deleted Loc 
		revisionChurn = resultLines.get(0) -resultLines.get(1);
		
		if (revisionChurn > maxChurn) {
			maxChurn = revisionChurn;
		}
		churnSum += revisionChurn;
		
		nr ++;
	}
	
	private void saveResult(AnalyzedClass analyzedClass) throws IOException {
		//setLocTouched to analyzedClass
		analyzedClass.setLocTouched(linesAdded + linesDeleted + linesModified);
		
		//set LocAdded to analyzedClass
		analyzedClass.setLocAdded(linesAdded);
		
		//set max LocAdded to analyzedClass
		analyzedClass.setMaxLocAdded(maxLinesAdded);
		
		//set churn to analyzedClass
		analyzedClass.setChurn(churnSum);
		
		//set max churn to analyzedClass
		analyzedClass.setMaxChurn(maxChurn);
		
		//set average LocAdded and Churn to analyzedClass		
		if (nr == 0) {
			throw new IOException("Number of revision egual to 0");
		}else {
			analyzedClass.setAvgLocAdded(linesAdded/nr);
			analyzedClass.setAvgChurn(churnSum/nr);
		}
	}
	
	@Override
	protected void analysis(AnalyzedClass analyzedClass, List<RevCommit> commits) throws GitAPIException, IOException  {
		List<DiffEntry> entries;
		List<Integer> resultLines;
		DiffEntry entryClass = null;
		 nr = 0;
		 maxLinesAdded = 0;
		 linesAdded = 0;
		 linesDeleted = 0;
		 linesModified = 0;
		 churnSum = 0;
		 maxChurn = 0;
		
		
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
	    df.setRepository(query.getRepository());
	    df.setDiffComparator(RawTextComparator.DEFAULT);
	    df.setDetectRenames(true);
	    
	    for (RevCommit commit : commits) {
	    	entries = query.getChangedFileList(commit);
	    	//get File touched by commit
	    	if (entries == null) {
	    		nr++;
				break;
			}
	    	
	    	entryClass = getEntryClass(entries,analyzedClass);
			
			if (entryClass == null) {
				df.close();
				throw new IOException("EntryClass is null");
			}
			
			//get lines for this revision
			resultLines = getAnalysLines(df, entryClass);
			
			getResult(resultLines);
			
		}
	    df.close();
	    
	    
		saveResult(analyzedClass);
		

	}
	
	

	@Override
	protected void setdefault(AnalyzedClass analyzedClass) {
		
		//setLocTouched to analyzedClass
		analyzedClass.setLocTouched(0);
		
		//set LocAdded to analyzedClass
		analyzedClass.setLocAdded(0);
		
		//set max LocAdded to analyzedClass
		analyzedClass.setMaxLocAdded(0);
		
		//set churn to analyzedClass
		analyzedClass.setChurn(0);
		
		//set max churn to analyzedClass
		analyzedClass.setMaxChurn(0);
		
		//set average LocAdded and Churn to analyzedClass		
		
		analyzedClass.setAvgLocAdded(0);
		analyzedClass.setAvgChurn(0);
		
	}

}
