package com.dianping.cosmos.hive.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.client.css.TableResources;
import com.dianping.cosmos.hive.client.service.HiveQueryServiceAsync;
import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.dianping.cosmos.hive.client.widget.CustomDialogBox;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

@UrlPatternEntryPoint("hivequery.html(\\\\?gwt.codesvr=127.0.0.1:9997)?")
public class HiveQuery extends LoginComponent implements EntryPoint {
	private static HiveQueryUiBinder uiBinder = GWT
			.create(HiveQueryUiBinder.class);

	interface HiveQueryUiBinder extends UiBinder<Widget, HiveQuery> {
	}

	@UiField(provided = true)
	CellTable<String[]> cellTable;
	@UiField(provided = true)
	SimplePager pager;
	@UiField
	ListBox dbListBox;
	@UiField
	TextArea hqlTextArea;
	@UiField
	TextArea progressTextArea;
	@UiField
	CheckBox isStoreFile;
	@UiField
	Button submitBut;
	@UiField
	Button submitQPBut;

	private CustomDialogBox cusDialog;

	private List<String[]> data = new ArrayList<String[]>();
	private List<IndexedColumn> indexedColumns = new ArrayList<IndexedColumn>();

	private AsyncDataProvider<String[]> provider = new AsyncDataProvider<String[]>() {
		@Override
		protected void onRangeChanged(HasData<String[]> display) {
			int start = display.getVisibleRange().getStart();
			int end = start + display.getVisibleRange().getLength();
			end = end >= data.size() ? data.size() : end;
			List<String[]> sub = data.subList(start, end);
			updateRowData(start, sub);
		}
	};

	private final HiveQueryServiceAsync hiveQueryService = HiveQueryServiceAsync.Util
			.getInstance();
	private final LoginServiceAsync loginService = LoginServiceAsync.Util
			.getInstance();

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

		hiveQueryService.getDatabases(getTokenid(),
				new AsyncCallback<List<String>>() {

					@Override
					public void onSuccess(List<String> result) {
						if (result != null) {
							dbListBox.clear();
							for (String res : result) {
								dbListBox.addItem(res);
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
				});
		
		hqlTextArea.addKeyPressHandler(new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				NativeEvent ne = event.getNativeEvent();
				if (ne.getCtrlKey()
						&& (ne.getKeyCode() == KeyCodes.KEY_ENTER)) {
					hqlSubmitButtonHandleClick(null);
				}
			}
		});
	}

	@UiHandler("submitBut")
	void hqlSubmitButtonHandleClick(ClickEvent e) {
		if ("".equals(hqlTextArea.getValue())) {
			cusDialog
					.setBodyContent("Please write your hql, it shoud not be empty!");
			cusDialog.center();
			return;
		}

		HiveQueryInputBo hqInputBo = new HiveQueryInputBo();
		hqInputBo.setHql(hqlTextArea.getValue());
		hqInputBo.setDatabase(dbListBox.getItemText(dbListBox
				.getSelectedIndex()));
		hqInputBo.setResultLimit(300);
		hqInputBo.setTimestamp(new Date().getTime());
		hqInputBo.setUsername(getUsername());
		hqInputBo.setTokenid(getTokenid());
		hqInputBo.setStoreResult(isStoreFile.getValue());

		submitBut.setEnabled(false);
		progressTextArea.setText("Executing Hive Query .........");
		hiveQueryService.getQueryResult(hqInputBo,
				new AsyncCallback<HiveQueryOutputBo>() {

					@Override
					public void onSuccess(HiveQueryOutputBo result) {
						submitBut.setEnabled(true);
						String errorMessage = result.getErrorMsg();
						if (errorMessage != null && !errorMessage.equals("")) {
							progressTextArea.setText(errorMessage);
						} else {
							progressTextArea.setText("Query Result Returned!");
							removeCellTableAllColumns(cellTable, indexedColumns);
							indexedColumns.clear();
							int rowCount = result.getData().size();
							int columnCount = result.getFieldSchema().length;
							for (int i = 0; i < columnCount; i++) {
								IndexedColumn col = new IndexedColumn(i);
								cellTable.addColumn(col,
										result.getFieldSchema()[i]);
								indexedColumns.add(col);
							}
							data = result.getData();
							cellTable.setRowCount(rowCount);
							provider = new AsyncDataProvider<String[]>() {
								@Override
								protected void onRangeChanged(
										HasData<String[]> display) {
									int start = display.getVisibleRange()
											.getStart();
									int end = start
											+ display.getVisibleRange()
													.getLength();
									end = end >= data.size() ? data.size()
											: end;
									List<String[]> sub = data.subList(start,
											end);
									updateRowData(start, sub);
								}
							};
							provider.addDataDisplay(cellTable);
							provider.updateRowCount(data.size(), true);
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						submitBut.setEnabled(true);
						progressTextArea
								.setText("Something Wrong With The Query!");
						caught.printStackTrace();
					}
				});
	}

	@UiHandler("submitQPBut")
	void queryPlanSubmitButtonHandleClick(ClickEvent e) {
		if ("".equals(hqlTextArea.getValue())) {
			cusDialog
					.setBodyContent("Please write your hql, it shoud not be empty!");
			cusDialog.center();
			return;
		}

		progressTextArea.setText("Retrieving Hive Query Plan .........");

		hiveQueryService.getQueryPlan(getTokenid(), hqlTextArea.getValue(),
				dbListBox.getItemText(dbListBox.getSelectedIndex()),
				new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						progressTextArea.setText(result);
					}

					@Override
					public void onFailure(Throwable caught) {
						progressTextArea
								.setText("Retrieving Hive Query Plain Failed !");
						caught.printStackTrace();
					}
				});
	}

	private void removeCellTableAllColumns(CellTable<String[]> celltable,
			List<IndexedColumn> columns) {
		if (columns != null) {
			for (IndexedColumn col : columns) {
				celltable.removeColumn(col);
			}
		}
	}

	private void initialize() {
		cellTable = new CellTable<String[]>(50,
				GWT.<TableResources> create(TableResources.class));

		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		cellTable.setWidth("100%", false);

		// constructSampleData();

		provider.addDataDisplay(cellTable);
		provider.updateRowCount(data.size(), true);

		SimplePager.Resources pagerResources = GWT
				.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0,
				true);
		pager.setDisplay(cellTable);

		cusDialog = new CustomDialogBox("text");
		Widget widget = uiBinder.createAndBindUi(this);
		hqlTextArea.setText("select * from hippolog limit 30");
		RootPanel.get("HiveQuery").add(widget);
	}

	private void constructSampleData() {
		String[] headers = new String[] { "column1", "column2", "column3", "column4",
				"column5", "column6" };

		for (int i = 0; i < 6; i++) {
			IndexedColumn col = new IndexedColumn(i);
			indexedColumns.add(col);
			cellTable.addColumn(col, headers[i]);
		}

		for (int i = 0; i < 30; i++) {
			data.add(new String[] { "", "", "", "", "", "" });
		}
		cellTable.setRowCount(data.size());
	}
}
