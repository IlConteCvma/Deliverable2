package logic.dataset.metrics;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import excption.MetricException;
import logic.dataset.GitQuery;
import logic.model.AnalyzedClass;

public abstract class AdvanceMetric extends Metric{
	
	protected GitQuery query;
	
	protected AdvanceMetric(String path,GitQuery query) {
		super(path);
		this.query = query;
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
	
	protected abstract void analysis(AnalyzedClass analyzedClass, List<RevCommit> commits) throws GitAPIException, IOException;
	
	protected abstract void setdefault(AnalyzedClass analyzedClass);

}
