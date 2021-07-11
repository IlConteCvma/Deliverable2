package logic.analysis.model;

import logic.analysis.state.AnalysisState;

public class AnalysisResult {
	private AnalysisState state;
	private WekaResult wekaResult;
	
	public AnalysisResult(AnalysisState state, WekaResult wekaResult) {
		this.state = state;
		this.wekaResult = wekaResult;
	}
	
	public AnalysisState getState() {
		return state;
	}

	public void setState(AnalysisState state) {
		this.state = state;
	}

	public WekaResult getWekaResult() {
		return wekaResult;
	}

	public void setWekaResult(WekaResult wekaResult) {
		this.wekaResult = wekaResult;
	}
}
