package logic.dataset.metrics;


import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import excption.MetricException;
import logic.dataset.GitQuery;
import logic.model.AnalyzedClass;

public class NRMetric extends Metric {
	private GitQuery query;
	
	public NRMetric(String path, GitQuery query) {
		super(path);
		this.query = query;
	}

	@Override
	public void startAnalysis(List<AnalyzedClass> classes) throws MetricException {
		List<RevCommit> commits;
		
		for (AnalyzedClass analyzedClass : classes) {
			try {
				commits = query.getCommitsForAFile(analyzedClass.getPath());
				analyzedClass.setNR(commits.size());
			} catch (GitAPIException e) {
				throw new MetricException("Error get commits for class: "+ analyzedClass.getPath(), e.getStackTrace());
			}

			
			
		}

	}

}
