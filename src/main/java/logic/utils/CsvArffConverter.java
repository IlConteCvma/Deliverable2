package logic.utils;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class CsvArffConverter {
	private CSVLoader loader;
	private ArffSaver saver;
	
	public CsvArffConverter() {
		loader = new CSVLoader();
		saver = new ArffSaver();
	}
	 	
	public String convert(String fileName) throws IOException {
		String target = fileName + ".csv";
		String destination = fileName + ".arff";
		loader.setSource(new File(target));
		
		//Instances
		Instances data = loader.getDataSet();
		saver.setInstances(data);
		
		saver.setFile(new File(destination));
	    saver.writeBatch();
		return destination;
	}
}
