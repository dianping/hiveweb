package com.dianping.cosmos.hive.client;

import java.util.ArrayList;
import java.util.List;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.cosmos.hive.client.bo.TableSchemaBo;
import com.dianping.cosmos.hive.client.css.TableResources;
import com.dianping.cosmos.hive.client.service.HiveQueryServiceAsync;
import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

@UrlPatternEntryPoint("tableschema.html(\\\\?gwt.codesvr=127.0.0.1:9997)?")
public class TableSchema extends LoginComponent implements EntryPoint {
	private static TableSchemaUiBinder uiBinder = GWT
			.create(TableSchemaUiBinder.class);

	interface TableSchemaUiBinder extends UiBinder<Widget, TableSchema> {
	}

	@UiField
	ListBox dbListBox;
	@UiField
	ListBox tableListBox;
	@UiField(provided = true)
	CellTable<TableSchemaBo> tableSchemaTable;
	@UiField
	TextArea tableDetail;

	private List<String> dbs = new ArrayList<String>();
	private List<String> tables = new ArrayList<String>();

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
		Widget widget = uiBinder.createAndBindUi(this);
		this.bind();
		RootPanel.get("TableSchema").add(widget);
	}

	private void initialize() {
		tableSchemaTable = new CellTable<TableSchemaBo>(15,
				GWT.<TableResources> create(TableResources.class));
		tableSchemaTable
				.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		TextColumn<TableSchemaBo> fieldNameColumn = new TextColumn<TableSchemaBo>() {
			@Override
			public String getValue(TableSchemaBo ts) {
				return ts.getFieldName();
			}
		};
		tableSchemaTable.addColumn(fieldNameColumn, "Field Name");

		TextColumn<TableSchemaBo> fieldTypeColumn = new TextColumn<TableSchemaBo>() {

			@Override
			public String getValue(TableSchemaBo ts) {
				return ts.getFieldType();
			}
		};
		tableSchemaTable.addColumn(fieldTypeColumn, "Field Type");

		TextColumn<TableSchemaBo> fieldCommentColumn = new TextColumn<TableSchemaBo>() {

			@Override
			public String getValue(TableSchemaBo ts) {
				return ts.getFieldComment();
			}
		};
		tableSchemaTable.addColumn(fieldCommentColumn, "Field Comment");
	}

	private void bind() {
		hiveQueryService.getDatabases(getTokenid(),
				new AsyncCallback<List<String>>() {

					@Override
					public void onSuccess(List<String> result) {
						dbListBox.clear();
						dbs = result;
						for (String db : dbs) {
							dbListBox.addItem(db);
						}
						String dbname = dbListBox.getValue(dbListBox
								.getSelectedIndex());
						getTables(dbname);
					}

					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
				});

		dbListBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				String dbname = dbListBox.getValue(dbListBox.getSelectedIndex());
				getTables(dbname);
			}
		});
		
		tableListBox.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				getTableSchema();
			}
		});
	}

	private void getTables(String dbname) {
		if (dbname == null || dbname.equals("")) {
			return;
		} else {
			hiveQueryService.getTables(getTokenid(), dbname,
					new AsyncCallback<List<String>>() {

						@Override
						public void onSuccess(List<String> result) {
							tableListBox.clear();
							tables = result;
							for (int i = 0; i < tables.size(); i++) {
								tableListBox.addItem(tables.get(i));
							}
							getTableSchema();
						}

						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
						}
					});
		}
	}

	private void getTableSchema() {
		final String dbName = dbListBox.getValue(dbListBox.getSelectedIndex());
		final String tableName = tableListBox.getValue(tableListBox
				.getSelectedIndex());
		if (!dbName.equals("") && !tableName.equals("")) {
			hiveQueryService.getTableSchema(getTokenid(), dbName, tableName,
					new AsyncCallback<List<TableSchemaBo>>() {

						@Override
						public void onSuccess(List<TableSchemaBo> result) {
							if (result != null && result.size() > 0){
								hiveQueryService.getTableSchemaDetail(getTokenid(), dbName, tableName,
										new AsyncCallback<String>() {

											@Override
											public void onSuccess(String result) {
												tableDetail.setText(result);
											}

											@Override
											public void onFailure(Throwable caught) {
												caught.printStackTrace();
											}
										});
								
								tableSchemaTable.setRowCount(result.size(), true);
								tableSchemaTable.setRowData(0, result);
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
						}
					});
		}
	}
}