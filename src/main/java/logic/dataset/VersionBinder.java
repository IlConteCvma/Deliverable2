package logic.dataset;

import java.time.LocalDate;
import java.util.List;

import excption.BinderException;
import logic.model.Release;

public class VersionBinder {
	private List<Release> releases;
	
	public VersionBinder(List<Release> releases) {
		this.releases = releases;
	}
	

	
	public Integer bindDateToIndex(LocalDate date) throws BinderException {

		for (Release release : releases) {
			int cmp = date.compareTo(release.getDate());
			// comparing date with release date while date > release
			if (cmp < 0) {
				return release.getIndex();
			}
		}
		
		
		throw new BinderException("release not found for date: "+date.toString());
	}
	
}
