package logic.analysis.state;


public class AnalysisState {
	private FeatureSelection featureSelection;
	private Balancing balancing;
	private Sensitive sensitive;
	private Classifier classifier;
	private Integer trainingNumber;
	
	public AnalysisState() {
		// void 
	}
	
	public AnalysisState(AnalysisState state) {
		//create a clone from a state
		this.featureSelection = state.getFeatureSelection();
		this.balancing = state.getBalancing();
		this.sensitive = state.getSensitive();
		this.classifier = state.getClassifier();
		this.trainingNumber = state.getTrainingNumber();
	}
	
	public FeatureSelection getFeatureSelection() {
		return featureSelection;
	}
	public void setFeatureSelection(FeatureSelection featureSelection) {
		this.featureSelection = featureSelection;
	}
	public Balancing getBalancing() {
		return balancing;
	}
	public void setBalancing(Balancing balancing) {
		this.balancing = balancing;
	}
	public Sensitive getSensitive() {
		return sensitive;
	}
	public void setSensitive(Sensitive sensitive) {
		this.sensitive = sensitive;
	}
	public Classifier getClassifier() {
		return classifier;
	}
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	public Integer getTrainingNumber() {
		return trainingNumber;
	}
	public void setTrainingNumber(Integer trainingNumber) {
		this.trainingNumber = trainingNumber;
	}
	
	
	
	
	
}
