package logic;


import java.io.File;
import java.io.IOException;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import excption.FolderAnalizerException;
import excption.MetricException;
import logic.analysis.AnalysisMain;
import logic.dataset.DatasetMain;
import logic.utils.AttributeRemover;
import logic.utils.CsvArffConverter;



public class MainClass {
	
	private static final String PROJECT = "BOOKKEEPER";
	private static final String URI = "C:\\Users\\marca\\Desktop\\bookkeeper";
	//true if URI it's a local directory , false URI it's a repository URL
	private static final boolean ISDIRECTORY = true;

	public static void main(String[] args) {
		
		
		DatasetMain datasetMain =new DatasetMain(PROJECT);
		
		
		try {
			datasetMain.datasetCore(URI, ISDIRECTORY);
		} catch (IOException | GitAPIException | JSONException  | FolderAnalizerException
				| MetricException e) {
			e.printStackTrace();
		}
		
		CsvArffConverter converter = new CsvArffConverter();
		String conversion = null;
		try {
			conversion =  converter.convert(PROJECT+"_dataset");
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
		
		AttributeRemover remover = new AttributeRemover();
		File file = null;
		if (conversion != null) {
			file = new File(conversion);
		}
		 
		try {
			String removed = remover.remove(file, 2);
			AnalysisMain main = new AnalysisMain(PROJECT,removed,0);

			main.analysisCore();
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
		
	
		
		
		
		
	}
}
