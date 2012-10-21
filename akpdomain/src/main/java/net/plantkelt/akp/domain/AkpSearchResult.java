package net.plantkelt.akp.domain;

import java.util.ArrayList;
import java.util.List;

public class AkpSearchResult {

	public static class AkpSearchResultColumn {

		private String displayValue;
		private String className;

		public AkpSearchResultColumn(String displayValue, String className) {
			this.className = className;
			this.displayValue = displayValue;
		}

		public String getDisplayValue() {
			return displayValue;
		}

		public String getClassName() {
			return className;
		}
	}

	public static class AkpSearchResultRow {
		private Integer plantXid;
		private String langXid;
		private Integer correct;
		private List<AkpSearchResultColumn> columns;

		public AkpSearchResultRow(Integer plantXid, String langXid, Integer correct) {
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
		
		public List<AkpSearchResultColumn> getColumns() {
			return columns;
		}

		public void addColumn(AkpSearchResultColumn column) {
			columns.add(column);
		}
	}

	private List<String> headerKeys;
	private List<AkpSearchResultRow> rows;

	public AkpSearchResult(int estimatedRows) {
		rows = new ArrayList<AkpSearchResultRow>(estimatedRows);
		headerKeys = new ArrayList<String>();
	}

	public AkpSearchResult() {
		this(32);
	}

	public List<AkpSearchResultRow> getRows() {
		return rows;
	}

	public List<String> getHeaderKeys() {
		return headerKeys;
	}

	public void addHeaderKey(String headerKey) {
		headerKeys.add(headerKey);
	}

	public void addRow(AkpSearchResultRow row) {
		rows.add(row);
	}

}
