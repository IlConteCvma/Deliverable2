package logic.utils;

import java.io.File;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class AttributeRemover {
	private Remove remove;
	private ArffSaver saver;
	
	public AttributeRemover() {
		this.remove = new Remove();
		this.saver = new ArffSaver();
	}
	
	public String remove(File file,int attributeIndex) throws Exception {
		//load dataset
		DataSource source = new DataSource(file.getAbsolutePath());
		Instances dataset = source.getDataSet();
		//setup options
		String[] opts = new String[]{ "-R", String.valueOf(attributeIndex) };
		//set the filter options
		remove.setOptions(opts);
		//pass the dataset to the filter
		remove.setInputFormat(dataset);
		//apply the filter
		Instances newData = Filter.useFilter(dataset, remove);
		
		
		String destination = "rm_"+file.getName();
		saver.setInstances(newData);
		saver.setFile(new File(destination));
		saver.writeBatch();
		
		return destination;
		
	}
}
