package logic.utils;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import excption.WekaException;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class WalkForward {
	private String folder;
	private String originalArff;
	
	public WalkForward(String folder,String originalArffFile ) {
		
		this.folder = folder;
		this.originalArff = originalArffFile;
	}

	public String getTestingSet(int testingSetNumber) throws WekaException, IOException {
		//load dataset
		DataSource source;
		Instances dataset;
		try {
			source = new DataSource(originalArff);
			dataset = source.getDataSet();
		} catch (Exception e) {
			throw new WekaException("Error get DataSourceof: "+originalArff,e.getStackTrace());
		}
		
		//remove upper part
		dataset = removeWhitValues(dataset, testingSetNumber + 1, true);
		//remove lower part
		dataset = removeWhitValues(dataset, testingSetNumber, false);
		
		//save dataSet
		String target = folder + File.separator + String.valueOf(testingSetNumber) + "_testingSet.arff";
		
		
	    try(BufferedWriter writer = new BufferedWriter(new FileWriter(target)) ){
			writer.write(dataset.toString());
			writer.flush();
		}
		
		return target;
	}
	
	public String getTrainingSet(int trainingTestNumber) throws WekaException, IOException {
		//load dataset
		DataSource source;
		Instances dataset;
		try {
			source = new DataSource(originalArff);
			dataset = source.getDataSet();
		} catch (Exception e) {
			throw new WekaException("Error get DataSourceof: "+originalArff,e.getStackTrace());
		}
		//remove upper part
		dataset = removeWhitValues(dataset, trainingTestNumber + 1, true);
		//save dataSet
		String target = folder + File.separator + String.valueOf(trainingTestNumber) + "_trainingSet.arff";
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(target)) ){
			writer.write(dataset.toString());
			writer.flush();
		}
		
		return target;
	}
	
	private Instances removeWhitValues(Instances dataset,int splitPoint,boolean invertSelection) throws WekaException {
		Instances newData;
		String[] opts = null;
		
		//create filter
		RemoveWithValues remove = new RemoveWithValues();
		
		if (invertSelection) {
			opts = new String[7];
			opts[0] = "-S"; opts[1] = String.valueOf(splitPoint);
			//This custom method work only on first attribute (in case study: Version)
			opts[2] = "-C"; opts[3] = "1";
			opts[4] = "-L"; opts[5] = "first-last";
			//set inversion
			opts[6] = "-V";
		}else {
			opts = new String[6];
			opts[0] = "-S"; opts[1] = String.valueOf(splitPoint);
			//This custom method work only on first attribute (in case study: Version)
			opts[2] = "-C"; opts[3] = "1";
			opts[4] = "-L"; opts[5] = "first-last";
		}
		
		try {
			remove.setOptions(opts);
			remove.setInputFormat(dataset);
			//apply filter
			newData = Filter.useFilter(dataset, remove);
		} catch (Exception e) {
			throw new WekaException("RemoveWithValues setOptions error",e.getStackTrace());
		}
		
		
		
		
		return newData;		
	}
	

	
	 
}
