package logic.dataset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import excption.FolderAnalizerException;
import excption.MetricException;
import logic.dataset.metrics.ChangeSetMetric;
import logic.dataset.metrics.LocAdvancedMetric;
import logic.dataset.metrics.LocMetric;
import logic.dataset.metrics.NRMetric;
import logic.model.AnalyzedClass;
import logic.model.Release;
import logic.model.Ticket;

public class DatasetMain {
	private String project ;
	private FileWriter fileWriter;
	private String fileName;
	private static final Logger LOG = LoggerFactory.getLogger(DatasetMain.class);

	/*
	 * Ottenere le realese di un progetto
	 * Analizzare i Ticket su Jira confrontando i commit per trovare quelli relativi a bug
	 * 
	 * Per ogni versione analizzare le classi java e capire la loro buggyness con AF o proportion szz
	 * calcolare le metriche per ogni classe 
	 * costruire file di report (costruirlo dinamicamente)
	 * */
	
	public DatasetMain(String project) {
		this.project = project;
		this.fileName = project + "_dataset.csv";
	}
	
	
	public void datasetCore(String uri,boolean existDirectory) throws IOException, GitAPIException, JSONException, FolderAnalizerException, MetricException  {
		List<Release> releases;
		List<Ticket> tickets;
		
		
		GetProjetInfo info = new GetProjetInfo(project);
		JiraTicket jiraTicket = new JiraTicket(project);
		GitQuery query  = new GitQuery(project);
		
		if (existDirectory) {
			query.openRepository(uri);
		}
		else {
			query.cloneRepositoryFromURL(uri);
		}
		
		//Get release of the project
		releases = info.getAllReleases();
		LOG.info("Releases info acquired");
		LOG.info("Numer of releases: {}",releases.size());
		
		//Get tickets
		tickets = jiraTicket.getTickets(query,releases);
		LOG.info("Tickets info acquired");
		//Get branch list
		query.setBranchForReleases(releases);
		LOG.info("Branch list acquired");
		
		
		//prepare output file
		prepareOutputFile();
		LOG.info("Prepare output file complete");
		//stop point hard coded, take only first half of releases 
		int stopPoint = releases.size()/2;
		
		LOG.info("Stop point set to: {}",stopPoint);
		List<Ticket> relativeTickets;
		
		 
		for (Release release : releases) {
			if (release.getIndex()>stopPoint) {
				break;
			}
			LOG.info("Analyzing release number: {} ",release.getIndex());
			relativeTickets = jiraTicket.getTicketsForRelease(tickets, release);
			try {
				versionAnalisys(release,query,relativeTickets);
			} catch (GitAPIException e) {
				LOG.error("Skipping release bacuse error whit msg: {}, {}",e.getMessage(),e.getCause().toString());
				LOG.error("Release info: verion name: {}, date {}",release.getVersionName(),release.getDate());
			}
			
			
		}


		
		
		//close file
		closeFile();
		
		
				
	}
	
	private void closeFile() throws IOException {
		fileWriter.flush();
		fileWriter.close();
	}
	public String getFile() {
		return this.fileName;
	}
	
	private void prepareOutputFile() throws IOException {
		
		fileWriter = new FileWriter(fileName);
		//first line of csv file
		fileWriter.append("Version,File Name,LOC,LOC_touched,NR,NFix,LOC_added,MAX_LOC_added,AVG_LOC_added,Churn,MAX_Churn,AVG_Churn,ChgSetSize,MAX_ChgSet,AVG_ChgSize,Buggy");
        fileWriter.append("\n");
	}
	
	private void versionAnalisys(Release release,GitQuery query,List<Ticket> tickets) throws FolderAnalizerException, GitAPIException, IOException, MetricException {
		String releaseID = release.getIndex().toString();
		File localRepo = query.getLocalRepoFolder();
		ClassAnalyzer analyzer;
		//impostare cartella alla branch corretta
		String branchName = release.getBranch().getName();
		query.changeBranch(branchName);
		LOG.info("Branch changed");
		
		//analizzare il folder e ottenere la lista di classi java
		List<AnalyzedClass> classes = new FolderAnalyzer(localRepo).getFileList(true);
		analyzer = new ClassAnalyzer(classes);
		
		//per ogni ticket con affected version release impostare buggy le relative classi
		for (Ticket ticket : tickets) {
			analyzer.setBuggyFromTicket(ticket,query);	
		}
		LOG.info("Bugginess set");
		//per ogni classe computare le metriche
		String path = FilenameUtils.separatorsToSystem(localRepo.getAbsolutePath())+File.separator;
		//loc
		LOG.info("LocMetric start");
		analyzer.analyzeMetric(new LocMetric(path));
		//loc advanced
		LOG.info("LocAdvancedMetric start");
		analyzer.analyzeMetric(new LocAdvancedMetric(path, query));
		LOG.info("NrMetric start");
		analyzer.analyzeMetric(new NRMetric(path, query));
		LOG.info("ChangeSetMetric");
		analyzer.analyzeMetric(new ChangeSetMetric(path, query));
		//number of fixed bug count during set buggy
		LOG.info("Metric analysis complete");
		//aggiungere ogni classe al file tramite fileWriter
		List<AnalyzedClass> classesAnalyzed = analyzer.getClasses();
		for (AnalyzedClass analyzedClass : classesAnalyzed) {
			fileWriter.append(releaseID);
			fileWriter.append(",");
			fileWriter.append(analyzedClass.getPath());
			fileWriter.append(",");
			
			//metric 1
			fileWriter.append(analyzedClass.getLoc().toString());
			fileWriter.append(",");
			//metric 2
			fileWriter.append(analyzedClass.getLocTouched().toString());
			fileWriter.append(",");
			//metric 3
			fileWriter.append(analyzedClass.getNR().toString());
			fileWriter.append(",");
			//metric 4
			fileWriter.append(analyzedClass.getNFixedBug().toString());
			fileWriter.append(",");
			//metric 5
			fileWriter.append(analyzedClass.getLocAdded().toString());
			fileWriter.append(",");
			//metric 6
			fileWriter.append(analyzedClass.getMaxLocAdded().toString());
			fileWriter.append(",");
			//metric 7
			fileWriter.append(analyzedClass.getAvgLocAdded().toString());
			fileWriter.append(",");
			//metric 8
			fileWriter.append(analyzedClass.getChurn().toString());
			fileWriter.append(",");
			//metric 9
			fileWriter.append(analyzedClass.getMaxChurn().toString());
			fileWriter.append(",");
			//metric 10
			fileWriter.append(analyzedClass.getAvgChurn().toString());
			fileWriter.append(",");
			//metric 11
			fileWriter.append(analyzedClass.getChgSetSize().toString());
			fileWriter.append(",");
			//metric 12
			fileWriter.append(analyzedClass.getMaxChgSet().toString());
			fileWriter.append(",");
			//metric 13
			fileWriter.append(analyzedClass.getAvgChgSet().toString());
			fileWriter.append(",");
			
			//buggy
			if (analyzedClass.isBuggy())
				fileWriter.append("Yes");
			else
				fileWriter.append("No");
			
			//close line
			fileWriter.append("\n");
			
			
		}
		LOG.info("Saving complete");
		
	}
	
	
	
}
