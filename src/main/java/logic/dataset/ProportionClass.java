package logic.dataset;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import excption.BinderException;
import logic.model.Release;
import logic.model.Ticket;

/*
 * Using a ** proportion 
 */
public class ProportionClass {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProportionClass.class);
	private static final Double P_COLDSTART = 1.8089;
	private Double p;
	private List<Release> releases;
	private VersionBinder binder;
	
	public ProportionClass(List<Release> releases) {
		this.releases = releases;
		this.binder = new VersionBinder(releases);
		this.p = P_COLDSTART;
		
	}
	
	
	
	private Integer evaluateIntroductionVersion(Integer ov, Integer fv) {
		Double iv;
		
		iv = fv-(fv-ov)*p;
		
		//cannot go under 1 for release indexing
		if (iv < 1) {
			iv = 1.0;
		}
		
		return iv.intValue();
	}
	
	
	
	public boolean setAffectedVersions(Ticket ticket) {
		
		Integer fv;
		Integer ov;
		try {
			fv = binder.bindDateToIndex(ticket.getResolutionDate());
			ov = binder.bindDateToIndex(ticket.getCreated());
			
			Integer iv = evaluateIntroductionVersion(ov, fv);
			
			
			//add to ticket the affected versions
			for (int i = 0; i < fv-iv; i++) {
				//there is -1 because version start from 1 but java array index start from 0
				
				ticket.addAffectedVersions(releases.get(iv+i-1).getVersionName());
			}
			
		} catch (BinderException e) {
			LOG.info("error on bind to date for: {}",e.getMessage());
			return false;
		}
		
		return true;
		
	}
	
	

}
