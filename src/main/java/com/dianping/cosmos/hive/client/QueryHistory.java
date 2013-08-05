package com.dianping.cosmos.hive.client;

import java.util.Date;
import java.util.List;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.cosmos.hive.client.bo.QueryHistoryBo;
import com.dianping.cosmos.hive.client.service.HiveQueryServiceAsync;
import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

@UrlPatternEntryPoint("queryhistory.html(\\\\?gwt.codesvr=127.0.0.1:9997)?")
public class QueryHistory extends LoginComponent implements EntryPoint {
	private static QueryHistoryUiBinder uiBinder = GWT
			.create(QueryHistoryUiBinder.class);

	interface QueryHistoryUiBinder extends UiBinder<Widget, QueryHistory> {
	}

	private final HiveQueryServiceAsync hiveQueryService = HiveQueryServiceAsync.Util
			.getInstance();

	private final static LoginServiceAsync loginService = LoginServiceAsync.Util
			.getInstance();

	@UiField(provided = true)
	CellTable<QueryHistoryBo> cellTable;
	@UiField(provided = true)
	SimplePager pager;

	public void onModuleLoad() {
		if (getTokenid() == null || getTokenid().equals("")) {
			cleanup();
		} else {
			loginService.isAuthenticated(getTokenid(),
					new AsyncCallback<Boolean>() {

						@Override
						public void onSuccess(Boolean result) {
							if (result) {
								drawPanel();
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

	private void drawPanel() {
		this.initialize();
		this.bind();
		Widget widget = uiBinder.createAndBindUi(this);
		RootPanel.get("QueryHistory").add(widget);
	}

	private void initialize() {
		cellTable = new CellTable<QueryHistoryBo>();
		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		cellTable.setWidth("100%", true);

		TextColumn<QueryHistoryBo> usernameColumn = new TextColumn<QueryHistoryBo>() {
			@Override
			public String getValue(QueryHistoryBo o) {
				return o.getUsername();
			}
		};
		cellTable.addColumn(usernameColumn, "用户名");

		DateTimeFormat dateFormat = DateTimeFormat
				.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
		Column<QueryHistoryBo, Date> addtimeColumn = new Column<QueryHistoryBo, Date>(
				new DateCell(dateFormat)) {

			@Override
			public Date getValue(QueryHistoryBo o) {
				return o.getAddtime();
			}

		};
		cellTable.addColumn(addtimeColumn, "查询时间");

		TextColumn<QueryHistoryBo> hqlColumn = new TextColumn<QueryHistoryBo>() {
			@Override
			public String getValue(QueryHistoryBo o) {
				return o.getHql();
			}
		};
		cellTable.addColumn(hqlColumn, "查询语句");
		
		TextColumn<QueryHistoryBo> modeColumn = new TextColumn<QueryHistoryBo>() {
			@Override
			public String getValue(QueryHistoryBo o) {
				return o.getMode();
			}
		};
		cellTable.addColumn(modeColumn, "查询方式");
		
		TextColumn<QueryHistoryBo> exectimeColumn = new TextColumn<QueryHistoryBo>() {
			@Override
			public String getValue(QueryHistoryBo o) {
				return String.valueOf(o.getExectime());
			}
		};
		cellTable.addColumn(exectimeColumn, "运行时间");

		ButtonCell downloadButton = new ButtonCell();
		Column<QueryHistoryBo, String> downloadColumn = new Column<QueryHistoryBo, String>(
				downloadButton) {
			public String getValue(QueryHistoryBo o) {
				String fileLocation = o.getFilename();

				if (fileLocation == null || fileLocation.equals("")) {
					return "未保存";
				} else {
					return "下载";
				}
			}
		};

		downloadColumn
				.setFieldUpdater(new FieldUpdater<QueryHistoryBo, String>() {
					@Override
					public void update(int index, QueryHistoryBo object,
							String value) {
						String fileLocation = object.getFilename();
						GWT.log("Downloading " + fileLocation);
						String fileName = fileLocation.substring(fileLocation
								.lastIndexOf('/') + 1);
						String link = GWT.getModuleBaseURL()
								+ "myfiledownload/" + fileName;
						Window.open(link, "_blank", "");
					}
				});
		cellTable.addColumn(downloadColumn, "下载结果文件");

		cellTable.setColumnWidth(usernameColumn, "10%");
		cellTable.setColumnWidth(addtimeColumn, "10%");
		cellTable.setColumnWidth(modeColumn, "10%");
		cellTable.setColumnWidth(exectimeColumn, "10%");
		cellTable.setColumnWidth(hqlColumn, "45%");
		
		cellTable.setColumnWidth(downloadColumn, "15%");

		SimplePager.Resources pagerResources = GWT
				.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0,
				true);
		pager.setDisplay(cellTable);
	}

	private void bind() {
		hiveQueryService.getQueryHistory(getRealuser(),
				new AsyncCallback<List<QueryHistoryBo>>() {

					@Override
					public void onSuccess(final List<QueryHistoryBo> result) {
						AsyncDataProvider<QueryHistoryBo> provider = new AsyncDataProvider<QueryHistoryBo>() {
							@Override
							protected void onRangeChanged(
									HasData<QueryHistoryBo> display) {
								int start = display.getVisibleRange()
										.getStart();
								int end = start
										+ display.getVisibleRange().getLength();
								end = end >= result.size() ? result.size()
										: end;
								List<QueryHistoryBo> sub = result.subList(
										start, end);
								updateRowData(start, sub);
							}
						};
						provider.addDataDisplay(cellTable);
						provider.updateRowCount(result.size(), true);
						pager.setDisplay(cellTable);
					}

					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
				});

	}

	public void initQueryHistoryList(
			AsyncDataProvider<QueryHistoryBo> dataProvider) {
		dataProvider.addDataDisplay(cellTable);
	}

}
