package logic.dataset.metrics;

import java.util.List;

import excption.MetricException;
import logic.model.AnalyzedClass;

public abstract class Metric {
	
	protected String absolutePath;
	
	protected Metric(String path) {
		this.absolutePath = path;
	}
	
	



	public abstract void startAnalysis(List<AnalyzedClass> classes) throws MetricException;



}
