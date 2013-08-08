package net.plantkelt.akp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AkpSearchResult {

	public static class AkpSearchResultColumn {

		private String displayValue;
		private String className;
		private boolean isValueKey;

		public AkpSearchResultColumn(String displayValue, String className) {
			this(displayValue, className, false);
		}

		public AkpSearchResultColumn(String displayValue, String className,
				boolean isValueKey) {
			this.className = className;
			this.displayValue = displayValue;
			this.isValueKey = isValueKey;
		}

		public String getDisplayValue() {
			return displayValue;
		}

		public String getClassName() {
			return className;
		}

		public boolean isValueKey() {
			return isValueKey;
		}
	}

	public static class AkpSearchResultRow {
		private Integer plantXid;
		private String langXid;
		private Integer correct;
		private String sortKey;
		private List<AkpSearchResultColumn> columns;

		public AkpSearchResultRow(Integer plantXid, String langXid,
				Integer correct) {
			this.plantXid = plantXid;
			this.langXid = langXid;
			this.correct = correct;
			columns = new ArrayList<AkpSearchResultColumn>();
		}

		public Integer getPlantXid() {
			return plantXid;
		}

		public String getLangXid() {
			return langXid;
		}

		public Integer getCorrect() {
			return correct;
		}

		public void setSortKey(String sortKey) {
			this.sortKey = sortKey;
		}

		public String getSortKey() {
			return sortKey;
		}

		public List<AkpSearchResultColumn> getColumns() {
			return columns;
		}

		public void addColumn(AkpSearchResultColumn column) {
			columns.add(column);
		}
	}

	private List<String> headerKeys;
	private List<AkpSearchResultRow> rows;
	private Map<String, String> authorRenameMap;
	private boolean sorted = false;

	public AkpSearchResult(int estimatedRows) {
		rows = new ArrayList<AkpSearchResultRow>(estimatedRows);
		headerKeys = new ArrayList<String>();
	}

	public AkpSearchResult() {
		this(32);
	}

	public List<AkpSearchResultRow> getRows() {
		if (!sorted) {
			Collections.sort(rows, new Comparator<AkpSearchResultRow>() {
				@Override
				public int compare(AkpSearchResultRow o1, AkpSearchResultRow o2) {
					String k1 = o1.getSortKey();
					String k2 = o2.getSortKey();
					if (k1 == null || k2 == null)
						return 0;
					return k1.compareTo(k2);
				}
			});
			sorted = true;
		}
		return rows;
	}

	public boolean isEmpty() {
		return rows.isEmpty();
	}

	public List<String> getHeaderKeys() {
		return headerKeys;
	}

	public void addHeaderKey(String headerKey) {
		headerKeys.add(headerKey);
	}

	public void addRow(AkpSearchResultRow row) {
		sorted = false;
		rows.add(row);
	}

	public Map<String, String> getAuthorRenameMap() {
		return authorRenameMap;
	}

	public void setAuthorRenameMap(Map<String, String> authorRenameMap) {
		this.authorRenameMap = authorRenameMap;
	}

}
