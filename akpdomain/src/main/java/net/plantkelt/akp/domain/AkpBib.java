package net.plantkelt.akp.domain;

public class AkpBib implements Comparable<AkpBib> {

	private String xid;
	private String title;
	private String author;
	private String date;
	private String isbn;
	private String comments;
	private String editor;

	public String getXid() {
		return xid;
	}

	public void setXid(String xid) {
		this.xid = xid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public String getShortName() {
		String stitle = title;
		if (stitle.length() > 50)
			stitle = stitle.substring(0, 47) + "…";
		return getXid() + " - " + stitle;
	}

	@Override
	public boolean equals(Object another) {
		if (another == null)
			return false;
		if (another instanceof AkpBib) {
			AkpBib anotherBib = (AkpBib) another;
			return anotherBib.getXid().equals(getXid());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getXid().hashCode();
	}

	@Override
	public int compareTo(AkpBib o) {
		/*
		 * I'm not sure that with this compare method we will have a stable sort
		 * (rely on compare to follow an ordering relation, ie symetric,
		 * transitive and? idempotent?), but normally bib XID are 4 digits
		 * (edition year) plus 4 letters (title?/author? code).
		 */
		try {
			if (getXid().length() < 4 || o.getXid().length() < 4)
				return getXid().compareTo(o.getXid());
			Integer xid1 = Integer.parseInt(getXid().substring(0, 4));
			Integer xid2 = Integer.parseInt(o.getXid().substring(0, 4));
			int diff = xid1 - xid2;
			if (diff == 0)
				return getXid().compareTo(o.getXid());
			else
				return diff;
		} catch (NumberFormatException e) {
			return getXid().compareTo(o.getXid());
		}
	}

}
