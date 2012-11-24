package com.dianping.cosmos.hive.client;

import java.util.List;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.cosmos.hive.client.service.HiveQueryServiceAsync;
import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.dianping.cosmos.hive.shared.util.StringUtils;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

//@UrlPatternEntryPoint(value = "home.html")
@UrlPatternEntryPoint(value = "home([^.]*).html(\\\\?.*)?")
public class HomePage extends LoginComponent implements EntryPoint {

	private CaptionPanel latestQueryHistoryStastics;
	private CaptionPanel hiveQueryCap;
	private CaptionPanel tableSchemaCap;
	private CaptionPanel queryHistoryCap;

	private CaptionPanel descStmt;

	private HIVEMessages messages = (HIVEMessages) GWT
			.create(HIVEMessages.class);

	private final HiveQueryServiceAsync hiveQueryService = HiveQueryServiceAsync.Util
			.getInstance();

	private final static LoginServiceAsync loginService = LoginServiceAsync.Util
			.getInstance();

	public void onModuleLoad() {
		if (getTokenid() == null || getTokenid().equals("")) {
			cleanup();
		} else {
			loginService.isAuthenticated(getTokenid(),
					new AsyncCallback<Boolean>() {

						@Override
						public void onSuccess(Boolean result) {
							if (result == true) {
								initialize();
							} else {
								cleanup();
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
							cleanup();
						}
					});
		}
	}

	private void initialize() {
		latestQueryHistoryStastics = new CaptionPanel(messages.homepage_stats());
		latestQueryHistoryStastics.setWidth("100%");
		latestQueryHistoryStastics.setStyleName("caption");

		hiveQueryCap = new CaptionPanel("<a href = '"
				+ messages.homepage_HiveQueryURL() + "'> "
				+ messages.homepage_HiveQueryLabel() + " </a>", true);
		hiveQueryCap.setStyleName("caption");

		tableSchemaCap = new CaptionPanel("<a href = '"
				+ messages.homepage_TableSchemaURL() + "'> "
				+ messages.homepage_TableSchemaLabel() + " </a>", true);
		tableSchemaCap.setStyleName("caption");

		queryHistoryCap = new CaptionPanel("<a href = '"
				+ messages.homepage_QueryHistoryURL() + "'> "
				+ messages.homepage_QueryHistoryLabel() + " </a>", true);
		queryHistoryCap.setStyleName("caption");

		descStmt = new CaptionPanel("");
		descStmt.setStyleName("caption");

		final HTML introHQ = new HTML(messages.homepage_HiveQueryDesc(), true);
		final VerticalPanel hiveQueryVP = new VerticalPanel();
		hiveQueryVP.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hiveQueryVP.add(introHQ);
		hiveQueryCap.add(hiveQueryVP);

		final HTML introTS = new HTML(messages.homepage_TableSchemaDesc(), true);
		final VerticalPanel tableSchemaVP = new VerticalPanel();
		tableSchemaVP.add(introTS);
		tableSchemaCap.add(tableSchemaVP);

		final HTML introQH = new HTML(messages.homepage_TableSchemaDesc(), true);
		final VerticalPanel queryHistoryVP = new VerticalPanel();
		queryHistoryVP.add(introQH);
		queryHistoryCap.add(queryHistoryVP);

		final VerticalPanel descVP = new VerticalPanel();
		final HTML demoStmt = new HTML("<i>" + messages.homepage_info()
				+ "</i>" + messages.homepage_contact(), true);
		descVP.add(demoStmt);
		descStmt.add(descVP);

		final VerticalPanel vp = new VerticalPanel();
		vp.setWidth("400px");
		vp.add(hiveQueryCap);
		vp.add(tableSchemaCap);
		vp.add(queryHistoryCap);
		vp.add(descStmt);

		final HorizontalPanel hp = new HorizontalPanel();
		hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		hp.add(vp);
		hp.add(latestQueryHistoryStastics);
		hp.setSpacing(10);

		this.bind();
		RootPanel.get("majorPart").add(hp);
	}

	private void bind() {
		hiveQueryService.getLatestNQuery(new AsyncCallback<List<String>>() {

			@Override
			public void onSuccess(List<String> result) {
				latestQueryHistoryStastics.add(new HTML(StringUtils
						.listToString(result)));
			}

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
			}
		});
	}
}