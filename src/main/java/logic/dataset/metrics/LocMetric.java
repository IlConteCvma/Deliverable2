package logic.dataset.metrics;

import java.io.File;
import java.io.FileReader;

import java.io.LineNumberReader;
import java.util.List;

import excption.MetricException;
import logic.model.AnalyzedClass;

public class LocMetric extends Metric {
	
	

	public LocMetric(String path) {
		super(path);
	}


	@Override
	public void startAnalysis(List<AnalyzedClass> classes) throws MetricException {
		Integer loc;
		for (AnalyzedClass analyzedClass : classes) {
			loc = cLoc(analyzedClass);
			analyzedClass.setLoc(loc);
			
		}

	}
	
	
	private Integer cLoc(AnalyzedClass class1) throws MetricException {
		
		String classPath = absolutePath + class1.getPath();
		
		@SuppressWarnings("unused")
		String line = null;

		try (LineNumberReader reader = new LineNumberReader(new FileReader(new File(classPath)))){
			
			while ((line = reader.readLine()) != null);
			return reader.getLineNumber() + 1 ;
		} catch (Exception e) {
			
			throw new MetricException("File not found: "+classPath,e.getStackTrace());
		}
				
	
	}

}
