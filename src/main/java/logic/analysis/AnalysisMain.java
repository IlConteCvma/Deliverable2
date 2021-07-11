package logic.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import excption.WekaException;
import logic.analysis.model.AnalysisResult;
import logic.analysis.model.WekaResult;
import logic.analysis.state.AnalysisState;
import logic.analysis.state.Balancing;
import logic.analysis.state.FeatureSelection;
import logic.analysis.state.Sensitive;
import logic.utils.WalkForward;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.supervised.instance.SMOTE;

public class AnalysisMain {
	private String arffFile ;
	private String project ;
	private File localAnalysisFolder;
	private DataSource source;
	private FileWriter fileWriter;
	private String fileName;
	private AnalysisState state;
	private int positiveIndex;
	private static final Logger LOG = LoggerFactory.getLogger(AnalysisMain.class);
	
	public AnalysisMain(String project, String arffFile,int positive) throws WekaException{
		this.arffFile = arffFile;
		this.project = project;
		this.positiveIndex = positive;
		this.fileName = project + "_result.csv";
		this.state = new AnalysisState();
		try {
			this.localAnalysisFolder = Files.createTempDirectory(Paths.get(System.getProperty("user.dir")), project).toFile();
			this.source = new DataSource(arffFile);
		} catch (Exception e) {
			throw new WekaException("Error getting DataSource of: " + arffFile,e.getStackTrace());
		}
	}
	

	
	public void analysisCore() throws WekaException, IOException {
		int numberOfReleases = getNumberOfReleases();
		int testingSetNumber;
		List<AnalysisResult> results;
		WalkForward walk = new WalkForward(localAnalysisFolder.getAbsolutePath(), arffFile);
		String testingSet;
		String trainingSet;
		//prepare output file
		prepareOutputFile();
		LOG.info("START ANALYSIS");
		
		//walk forward
		for (int i = 1; i < numberOfReleases; i++) {
			this.state.setTrainingNumber(i);
			testingSetNumber = i + 1;
			LOG.info("Testing set number: {}",testingSetNumber);
			try {
				testingSet = walk.getTestingSet(testingSetNumber);
				trainingSet = walk.getTrainingSet(i);
			} catch (Exception e) {
				throw new WekaException("Error walk forward: " + this.state.getTrainingNumber() +" msg: " + e.getMessage() , e.getStackTrace());
			}
			
			
			//Classifier randomForest
			LOG.info("RANDOMFOREST");
			this.state.setClassifier(logic.analysis.state.Classifier.RANDOMFOREST);
			results = firstSelection(trainingSet, testingSet, new RandomForest());
			saveResult(results);
			
			//Classifier naiveBase
			LOG.info("NAIVEBAYES");
			this.state.setClassifier(logic.analysis.state.Classifier.NAIVEBAYES);
			results = firstSelection(trainingSet, testingSet, new NaiveBayes());
			saveResult(results);
			
			//Classifier ibk
			LOG.info("IBK");
			this.state.setClassifier(logic.analysis.state.Classifier.IBK);
			results = firstSelection(trainingSet, testingSet, new IBk());
			saveResult(results);
			
		}
		try {
			TimeUnit.SECONDS.sleep(15);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new WekaException("Timer error: "+e.getMessage(),e.getStackTrace());
		}
		
		closeFile();
		deleteFolder();
		LOG.info("END");
			
	}
	
	
	private List<AnalysisResult> firstSelection(String training,String testing,Classifier classifier) throws WekaException {
		List<AnalysisResult> results = new ArrayList<>();
	
		DataSource sourceTraining;
		Instances noFilterTraining;
		DataSource sourceTesting;
		Instances noFilterTesting;
		
		try {
			sourceTraining = new DataSource(training);
			noFilterTraining = sourceTraining.getDataSet();
			sourceTesting = new DataSource(testing);
			noFilterTesting = sourceTesting.getDataSet();
			int numAttrNoFilter = noFilterTraining.numAttributes();
			noFilterTraining.setClassIndex(numAttrNoFilter - 1);
			noFilterTesting.setClassIndex(numAttrNoFilter - 1);

		} catch (Exception e) {
			throw new WekaException("Some error in firstSelection: " + this.state.getTrainingNumber() , e.getStackTrace());
		}
		
		//----No selection
		LOG.info("NOSELECTION");
		this.state.setFeatureSelection(FeatureSelection.NOSELECTION);
		//start second part of analysis
		secondBalancing(results,noFilterTraining,noFilterTesting,classifier);
		
		//----Feature selection
		LOG.info("BESTFIRST");
		this.state.setFeatureSelection(FeatureSelection.BESTFIRST);
		Instances filteredTesting;
		Instances filteredTraining;
		//create AttributeSelection object
		AttributeSelection filter = new AttributeSelection();
		//create evaluator and search algorithm objects
		CfsSubsetEval eval = new CfsSubsetEval();
		BestFirst search = new BestFirst();
		filter.setEvaluator(eval);
		filter.setSearch(search);

		try {
			filter.setInputFormat(noFilterTraining);
			filteredTraining = Filter.useFilter(noFilterTraining, filter);
			filteredTraining.setClassIndex(filteredTraining.numAttributes() - 1);
			filteredTesting = Filter.useFilter(noFilterTesting, filter);
			filteredTesting.setClassIndex(filteredTesting.numAttributes() - 1);
			//start second part of analysis
			secondBalancing(results,filteredTraining,filteredTesting,classifier);
			
		} catch (Exception e) {
			throw new WekaException("Some error in firstSelection(BestFirst): " + this.state.getTrainingNumber() , e.getStackTrace());
		}
		
		
		return results;
	}
	
	private void secondBalancing(List<AnalysisResult> results, Instances training, Instances testing, Classifier classifier) throws WekaException {
		//4 case of balancing no,oversampling,undersampling,smote
		Instances balancedTraining;
		FilteredClassifier filterClassifier;
		//----No balancing
		LOG.info("NOBALANCING");
		this.state.setBalancing(Balancing.NOBALANCING);
		thirdSensitive(results,training,testing,classifier);
		
		//----Oversampling
		LOG.info("OVERSAMPLING");
		this.state.setBalancing(Balancing.OVERSAMPLING);
		Resample resample = new Resample();
		String[] opts1 = new String[4];
		
		double sampleSizePercent = getSampleSizePercent(training);
		opts1[0] = "-B"; opts1[1] = "1.0";
		opts1[2] = "-Z"; opts1[3] = String.valueOf(sampleSizePercent);
		
		try {
			resample.setOptions(opts1);
			resample.setInputFormat(training);
			filterClassifier = new FilteredClassifier();
			filterClassifier.setClassifier(classifier);
			filterClassifier.setFilter(resample);
			balancedTraining = Filter.useFilter(training, resample);
		} catch (Exception e) {
			throw new WekaException("Get error secondBalancing(Oversampling): "+e.getMessage(), e.getStackTrace());	
		}
		thirdSensitive(results, balancedTraining, testing,filterClassifier);
		
 		
		//----Undersampling
		LOG.info("UNDERSAMPLING");
		this.state.setBalancing(Balancing.UNDERSAMPLING);
		SpreadSubsample  spreadSubsample = new SpreadSubsample();
		String[] opts2 = new String[]{ "-M", "1.0"};
		try {
			spreadSubsample.setOptions(opts2);
			spreadSubsample.setInputFormat(training);
			filterClassifier = new FilteredClassifier();
			filterClassifier.setClassifier(classifier);
			filterClassifier.setFilter(spreadSubsample);
			balancedTraining = Filter.useFilter(training, spreadSubsample);
		} catch (Exception e) {
			throw new WekaException("Error in secondBalancing(Undersampling): "+ this.state.getTrainingNumber(), e.getStackTrace());
		}
		thirdSensitive(results, balancedTraining, testing,filterClassifier);
		
		//----SMOTE
		LOG.info("SMOTE");
		this.state.setBalancing(Balancing.SMOTE);
		SMOTE smote=new SMOTE();
		String[] opts3 = new String[]{ "-P", String.valueOf(sampleSizePercent)};
		try {
			smote.setOptions(opts3);
			smote.setInputFormat(training);
			filterClassifier = new FilteredClassifier();
			filterClassifier.setClassifier(classifier);
			filterClassifier.setFilter(smote);
			balancedTraining = Filter.useFilter(training, smote);
			
		} catch (Exception e) {
			throw new WekaException("Get error secondBalancing(SMOTE): "+e.getMessage(), e.getStackTrace());
		}       
		thirdSensitive(results, balancedTraining, testing,filterClassifier);
		
		
	}
	
	private void thirdSensitive(List<AnalysisResult> results,Instances training, Instances testing, Classifier classifier) throws WekaException {
		//3 case No cost sensitive , Sensitive Threshold , Sensitive Learning (CFN = 10 * CFP)
				
		CostMatrix matrix = createCostMatrix(1.0, 10.0);

		//----No Sensitive
		LOG.info("NOSENSITIVE");
		this.state.setSensitive(Sensitive.NOSENSITIVE);
		evaluationAndAddResults(results, training, testing, classifier,false);
		
		
		
		
		//----Sensitive Threshold
		this.state.setSensitive(Sensitive.THRESHOLD);
		LOG.info("THRESHOLD");
		CostSensitiveClassifier threshold = new CostSensitiveClassifier();
		String[] opt = new String[]{"-M"};
		try {
			threshold.setOptions(opt);
			threshold.setClassifier(classifier);
			threshold.setCostMatrix(matrix);
			
			
		} catch (Exception e) {
			throw new WekaException("Get error thirdSensitive(Threshold): "+e.getMessage(), e.getStackTrace());
		}
		//save value
		evaluationAndAddResults(results, training, testing, threshold,true);

		
		//----Sensitive Learning
		LOG.info("LEARNING");
		this.state.setSensitive(Sensitive.LEARNING);
		CostSensitiveClassifier learning = new CostSensitiveClassifier();
		try {
			learning.setClassifier(classifier);
			learning.setCostMatrix(matrix);
			
			
		} catch (Exception e) {
			throw new WekaException("Get error thirdSensitive(Threshold): "+e.getMessage(), e.getStackTrace());
		}
		//save value
		evaluationAndAddResults(results, training, testing, learning,true);
	}
	
	private void evaluationAndAddResults(List<AnalysisResult> results, Instances training, Instances testing, Classifier classifier, boolean sensitive ) throws WekaException  {
		Evaluation evalClass;
		try {
			classifier.buildClassifier(training);
			if(sensitive) {
				evalClass = new Evaluation(testing, ((CostSensitiveClassifier)classifier).getCostMatrix());
			}else {
				evalClass = new Evaluation(testing);
			}
			evalClass.evaluateModel(classifier, testing);
			
		} catch (Exception e) {
			throw new WekaException("Get error evaluation(start): "+e.getMessage(), e.getStackTrace());
		}
		LOG.info("Evaluation complete");
		results.add(getWekaAnalysisResult(evalClass, training, testing));
	}
	
	private AnalysisResult getWekaAnalysisResult(Evaluation evaluation,Instances training, Instances testing) {
		LOG.info("Extract results");
		WekaResult wekaResult = new WekaResult();
		wekaResult.setTrainingPercentage(100*((double)testing.size()/(training.size()+testing.size())));
		wekaResult.setDefectiveTrainingPercentage(getDefectivePercentage(training));
		wekaResult.setDefectiveTestingPercentage(getDefectivePercentage(testing));
		wekaResult.setTruePositive(evaluation.numTruePositives(this.positiveIndex));
		wekaResult.setTrueNegative(evaluation.numTrueNegatives(this.positiveIndex));
		wekaResult.setFalseNegative(evaluation.numFalseNegatives(this.positiveIndex));
		wekaResult.setFalsePositive(evaluation.numFalsePositives(this.positiveIndex));
		wekaResult.setPrecision(evaluation.precision(this.positiveIndex));
		wekaResult.setRecall(evaluation.recall(this.positiveIndex));
		wekaResult.setAUC(evaluation.areaUnderROC(this.positiveIndex));
		wekaResult.setKappa(evaluation.kappa());
		return new AnalysisResult(new AnalysisState(this.state), wekaResult);
	}
	
	private CostMatrix createCostMatrix (double weightFalsePositive , double weightFalseNegative ) {
		/*
		 * Buggy if positive is not on position 1
		 * */
		
		CostMatrix costMatrix = new CostMatrix(2);
		costMatrix.setCell (0, 0, 0.0);
		costMatrix.setCell (1, 0, weightFalsePositive);
		costMatrix.setCell(0, 1, weightFalseNegative);
		costMatrix.setCell (1, 1,0.0);
		
		return costMatrix;
	}


	private double getDefectivePercentage(Instances instances) {
		int buggyAtt = instances.numAttributes() - 1;
		AttributeStats as = instances.attributeStats(buggyAtt);
		
		double defective = as.nominalCounts[this.positiveIndex];
		
		return 100*(defective/instances.size());
	}
	
	
	private double getSampleSizePercent(Instances instances) {
		int buggyAtt = instances.numAttributes() - 1;
		AttributeStats as = instances.attributeStats(buggyAtt);
		int majority;
		int minority;
		if (as.nominalCounts[0] > as.nominalCounts[1] ) {
			majority = as.nominalCounts[0];
			minority = as.nominalCounts[1];
		}else {
			majority = as.nominalCounts[1];
			minority = as.nominalCounts[0];
		}
		
	
		
		return 100*((majority-minority)/(double)minority);
	}
	
	//releases start from 1
	private int getNumberOfReleases() throws WekaException {
		Instances data;
		try {
			data = source.getDataSet();
		} catch (Exception e) {
			throw new WekaException("Error getting DataSource of: " + arffFile,e.getStackTrace());
		}
		AttributeStats as = data.attributeStats(0);  
		
		return as.distinctCount;
		
	}
	private void prepareOutputFile() throws IOException {
		
		fileWriter = new FileWriter(fileName);
		//first line of csv file
		fileWriter.append("Dataset,#Training release,%traning,%Defective in training,%Defective in testing,Classifier,"
							+ "Balancing,Feature Selection,Sensitivity,TP,FP,TN,FN,Precision,Recall,AUC,Kappa");
        fileWriter.append("\n");
	}
	
	
	private void deleteFolder() throws IOException {
		FileUtils.deleteDirectory(localAnalysisFolder);
		
	}
	private void closeFile() throws IOException {
		fileWriter.flush();
		fileWriter.close();
	}
	
	private void saveResult(List<AnalysisResult> results) throws IOException {
		LOG.info("Save results");
		for (AnalysisResult result : results) {
			fileWriter.append(this.project);
			fileWriter.append(",");
			//Training release
			fileWriter.append(result.getState().getTrainingNumber().toString());
			fileWriter.append(",");
			//training percentage
			fileWriter.append(String.valueOf(result.getWekaResult().getTrainingPercentage()));
			fileWriter.append(",");
			//%Defective in training
			fileWriter.append(String.valueOf(result.getWekaResult().getDefectiveTrainingPercentage()));
			fileWriter.append(",");
			//%Defective in testing
			fileWriter.append(String.valueOf(result.getWekaResult().getDefectiveTestingPercentage()));
			fileWriter.append(",");
			//Classifier
			fileWriter.append(result.getState().getClassifier().toString());
			fileWriter.append(",");
			//Balancing
			fileWriter.append(result.getState().getBalancing().toString());
			fileWriter.append(",");
			//Feature Selection
			fileWriter.append(result.getState().getFeatureSelection().toString());
			fileWriter.append(",");
			//Sensitivity
			fileWriter.append(result.getState().getSensitive().toString());
			fileWriter.append(",");
			//TP
			fileWriter.append(String.valueOf(result.getWekaResult().getTruePositive()));
			fileWriter.append(",");
			//FP
			fileWriter.append(String.valueOf(result.getWekaResult().getFalsePositive()));
			fileWriter.append(",");
			//TN
			fileWriter.append(String.valueOf(result.getWekaResult().getTrueNegative()));
			fileWriter.append(",");
			//FN
			fileWriter.append(String.valueOf(result.getWekaResult().getFalseNegative()));
			fileWriter.append(",");
			//Precision
			fileWriter.append(String.valueOf(result.getWekaResult().getPrecision()));
			fileWriter.append(",");
			//Recall
			fileWriter.append(String.valueOf(result.getWekaResult().getRecall()));
			fileWriter.append(",");
			//AUC
			fileWriter.append(String.valueOf(result.getWekaResult().getAUC()));
			fileWriter.append(",");
			//Kappa
			fileWriter.append(String.valueOf(result.getWekaResult().getKappa()));
			fileWriter.append(",");
			
			//end line
			fileWriter.append("\n");
		}
		
		LOG.info("Saving complete");
	}
	
}
