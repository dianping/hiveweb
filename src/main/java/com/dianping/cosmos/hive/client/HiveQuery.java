package com.dianping.cosmos.hive.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.client.service.HiveQueryServiceAsync;
import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.dianping.cosmos.hive.client.widget.CustomDialogBox;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

@UrlPatternEntryPoint("HiveQuery.html(\\\\?gwt.codesvr=127.0.0.1:9997)?")
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

		hiveQueryService.getQueryResult(hqInputBo,
				new AsyncCallback<HiveQueryOutputBo>() {

					@Override
					public void onSuccess(HiveQueryOutputBo result) {
						removeCellTableAllColumns(cellTable, indexedColumns);
//						cellTable = new CellTable<String[]>();
//						cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
//						cellTable.setWidth("100%", false);
						indexedColumns.clear();
						int rowCount = result.getData().size();
						int columnCount = result.getFieldSchema().length;
						for (int i = 0; i < columnCount; i++) {
							IndexedColumn col = new IndexedColumn(i);
							cellTable.addColumn(col, result.getFieldSchema()[i]);
							indexedColumns.add(col);
						}
						data = result.getData();
						cellTable.setRowCount(rowCount);
						provider = new AsyncDataProvider<String[]>() {
							@Override
							protected void onRangeChanged(HasData<String[]> display) {
								int start = display.getVisibleRange().getStart();
								int end = start + display.getVisibleRange().getLength();
								end = end >= data.size() ? data.size() : end;
								List<String[]> sub = data.subList(start, end);
								updateRowData(start, sub);
							}
						};
						provider.addDataDisplay(cellTable);
						provider.updateRowCount(data.size(), true);
						//pager.setDisplay(cellTable);
					}

					@Override
					public void onFailure(Throwable caught) {
						//removeCellTableAllColumns(cellTable, indexedColumns);
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

		hiveQueryService.getQueryPlan(getTokenid(),
				hqlTextArea.getValue(),
				dbListBox.getItemText(dbListBox.getSelectedIndex()),
				new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						progressTextArea.setText(result);
					}

					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
				});
	}

	private void removeCellTableAllColumns(CellTable<String[]> celltable ,List<IndexedColumn> columns) {
		for (IndexedColumn col : columns) {
			celltable.removeColumn(col);
		}
	}

	private void initialize() {
		cellTable = new CellTable<String[]>();
		cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		cellTable.setWidth("100%", false);

		constructSampleData();

		provider.addDataDisplay(cellTable);
		provider.updateRowCount(data.size(), true);

		SimplePager.Resources pagerResources = GWT
				.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0,
				true);
		pager.setDisplay(cellTable);

		cusDialog = new CustomDialogBox("text");
		Widget widget = uiBinder.createAndBindUi(this);
		hqlTextArea
				.setText("select * from hippolog limit 30");
		RootPanel.get("HiveQuery").add(widget);
	}

	private void constructSampleData() {
		String[] headers = new String[] {"guid", "user_id", "host", "user_ip", "city", "source"};
		
		for (int i = 0; i < 6; i++){
			IndexedColumn col = new IndexedColumn(i);
			indexedColumns.add(col);
			cellTable.addColumn(col, headers[i]);
		}
		
//		cellTable.addColumn(col0, "guid");
//		cellTable.addColumn(col1, "user_id");
//		cellTable.addColumn(col2, "host");
//		cellTable.addColumn(col3, "user_ip");
//		cellTable.addColumn(col4, "city");
//		cellTable.addColumn(col5, "source");

		for (int i = 0; i < 30; i++) {
			data.add(new String[] { "0000097d-eecd-4b35-88d0-e344c9f06d35",
					"0", "t.dianping.com", "175.170.160.202", "19", "so.360.cn" });
		}
		cellTable.setRowCount(data.size());
	}
}
