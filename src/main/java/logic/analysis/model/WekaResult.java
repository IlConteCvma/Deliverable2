package logic.analysis.model;

public class WekaResult {
	private double trainingPercentage;
	private double defectiveTrainingPercentage;
	private double defectiveTestingPercentage;
	private double truePositive;
	private double trueNegative;
	private double falsePositive;
	private double falseNegative;
	private double recall;
	private double precision;
	private double auc;
	private double kappa;

	public double getTrainingPercentage() {
		return trainingPercentage;
	}

	public void setTrainingPercentage(double trainingPercentage) {
		this.trainingPercentage = trainingPercentage;
	}

	public double getDefectiveTrainingPercentage() {
		return defectiveTrainingPercentage;
	}

	public void setDefectiveTrainingPercentage(double defectiveTrainingPercentage) {
		this.defectiveTrainingPercentage = defectiveTrainingPercentage;
	}

	public double getDefectiveTestingPercentage() {
		return defectiveTestingPercentage;
	}

	public void setDefectiveTestingPercentage(double defectiveTestingPercentage) {
		this.defectiveTestingPercentage = defectiveTestingPercentage;
	}

	public double getTruePositive() {
		return truePositive;
	}

	public void setTruePositive(double truePositive) {
		this.truePositive = truePositive;
	}

	public double getTrueNegative() {
		return trueNegative;
	}

	public void setTrueNegative(double trueNegative) {
		this.trueNegative = trueNegative;
	}

	public double getFalsePositive() {
		return falsePositive;
	}

	public void setFalsePositive(double falsePositive) {
		this.falsePositive = falsePositive;
	}

	public double getFalseNegative() {
		return falseNegative;
	}

	public void setFalseNegative(double falseNegative) {
		this.falseNegative = falseNegative;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getAUC() {
		return auc;
	}

	public void setAUC(double auc) {
		this.auc = auc;
	}

	public double getKappa() {
		return kappa;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}
	
}
