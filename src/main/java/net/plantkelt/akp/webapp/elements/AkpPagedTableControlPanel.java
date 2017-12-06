package net.plantkelt.akp.webapp.elements;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import net.plantkelt.akp.webapp.models.PagedListModel;

public class AkpPagedTableControlPanel<T> extends Panel {

	private static final long serialVersionUID = 1L;

	public AkpPagedTableControlPanel(String id,
			final PagedListModel<T> pagedListModel) {
		super(id);

		Link<?> prevLink = new Link<T>("prev") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				pagedListModel.prevPage();
			}

			@Override
			public boolean isEnabled() {
				return pagedListModel.getPageNumber() > 0;
			}
		};
		add(prevLink);

		Link<?> nextLink = new Link<T>("next") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				pagedListModel.nextPage();
			}

			@Override
			public boolean isEnabled() {
				return pagedListModel.getPageNumber() < pagedListModel
						.getPageCount();
			}
		};
		add(nextLink);

		Label fromLbl = new Label("from",
				new LoadableDetachableModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					protected String load() {
						return "" + pagedListModel.getFromIndex();
					}
				});
		add(fromLbl);

		Label toLbl = new Label("to", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected String load() {
				return "" + pagedListModel.getToIndex();
			}
		});
		add(toLbl);

		Label totalLbl = new Label("total",
				new LoadableDetachableModel<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					protected String load() {
						return "" + pagedListModel.getTotalCount();
					}
				});
		add(totalLbl);
	}
}
