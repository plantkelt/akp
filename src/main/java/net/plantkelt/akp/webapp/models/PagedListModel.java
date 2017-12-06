package net.plantkelt.akp.webapp.models;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

public abstract class PagedListModel<T>
		extends LoadableDetachableModel<List<T>> {
	private static final long serialVersionUID = 1L;

	private int totalCount;
	private int pageSize = 100;
	private int pageNumber = 0;

	public PagedListModel(int totalCount, int pageSize) {
		this.totalCount = totalCount;
		this.pageSize = pageSize;
	}

	public void nextPage() {
		if ((pageNumber + 1) * pageSize < totalCount)
			pageNumber++;
	}

	public void prevPage() {
		if (pageNumber > 0)
			pageNumber--;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public int getPageCount() {
		return (totalCount + pageSize - 1) / pageSize - 1;
	}

	public int getFromIndex() {
		return pageNumber * pageSize + 1;
	}

	public int getToIndex() {
		int toIndex = pageNumber * pageSize + pageSize - 1;
		if (toIndex > totalCount - 1)
			toIndex = totalCount - 1;
		return toIndex + 1;
	}

	public void setPageSize(int pageSize) {
		// TODO Recompute new page number...
		// I'm too lazy to do it now
		this.pageNumber = 0;
		this.pageSize = pageSize;
	}

	@Override
	protected List<T> load() {
		return fetchPage(pageNumber, pageSize);
	}

	protected abstract List<T> fetchPage(int pageNumber, int pageSize);

}
