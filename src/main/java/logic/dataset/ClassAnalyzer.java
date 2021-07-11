package logic.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;

import excption.MetricException;
import logic.dataset.metrics.Metric;
import logic.model.AnalyzedClass;
import logic.model.Ticket;

public class ClassAnalyzer {
	private List<AnalyzedClass> classes;
	
	public ClassAnalyzer (List<AnalyzedClass> classes) {
		this.classes = classes;
	}
	
	
	public List<AnalyzedClass> getClasses(){
		return this.classes;
	}
	
	public void analyzeMetric(Metric metric) throws MetricException {
		metric.startAnalysis(this.classes);
	}
	
	
	public void setBuggyFromTicket(Ticket ticket,GitQuery query) throws GitAPIException, IOException {
		List<DiffEntry> entries;
		List<String> javaClasses = new ArrayList<>();
		entries = query.getChangedFileList(ticket.getCommit());
		
		//get file list
		for (DiffEntry entry : entries) {
			
			String entryPath = FilenameUtils.separatorsToSystem(entry.getNewPath());
			if (entryPath.endsWith(".java")) {
				javaClasses.add(entryPath);
			}
		}
		
		//modify relative class
		for (String string : javaClasses) {
			for (AnalyzedClass class1 : this.classes) {
				if (class1.getPath().equals(string)) {
					if (!class1.isBuggy())
						class1.setBuggy(true);
					else 
						class1.addBug();
						
					break;
				}
			}
		}	
		
	}
	

}
