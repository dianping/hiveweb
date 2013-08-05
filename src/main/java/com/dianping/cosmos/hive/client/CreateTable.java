package com.dianping.cosmos.hive.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.cosmos.hive.client.bo.ResultStatusBo;
import com.dianping.cosmos.hive.client.service.HiveQueryServiceAsync;
import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.dianping.cosmos.hive.client.widget.CustomPopupPanel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

@UrlPatternEntryPoint(value = "createtable([^.]*).html(\\\\?.*)?")
public class CreateTable extends LoginComponent implements EntryPoint {

	private static CreateTableUiBinder uiBinder = GWT
			.create(CreateTableUiBinder.class);

	interface CreateTableUiBinder extends UiBinder<Widget, CreateTable> {
	}

	private final HiveQueryServiceAsync hiveQueryService = HiveQueryServiceAsync.Util
			.getInstance();
	private final static LoginServiceAsync loginService = LoginServiceAsync.Util
			.getInstance();

	@UiField
	TabPanel tabPanel;
	@UiField
	Grid grid;
	@UiField(provided = true)
	SuggestBox dbname;
	@UiField
	TextBox tablename;
	@UiField
	TextBox tableDesc;
	@UiField
	ListBox fieldTerminator;
	@UiField
	TextBox location;
	@UiField
	CheckBox externalTable;
	@UiField
	ListBox fileFormat;
	@UiField
	TextBox inputformat;
	@UiField
	TextBox outputformat;
	@UiField
	ScrollPanel fieldScrollPanel;
	@UiField
	Grid fieldsGrid;
	@UiField
	Button addField;
	@UiField
	TextArea hqlTextArea;
	@UiField
	Button genHql;
	@UiField
	Button execHql;
	// upload tab widgets
	@UiField
	HTMLPanel uploadFilePanel;
	
	private CustomPopupPanel popupPanel = new CustomPopupPanel();

	private static Set<String> fieldDelimiterSet = null;
	private static Set<String> fileFormatSet = null;
	private static List<String> columnTypeList = null;

	static {
		fieldDelimiterSet = new HashSet<String>(Arrays.asList("\\001", "\\t",
				","));
		fileFormatSet = new HashSet<String>(Arrays.asList("TextFile",
				"SequenceFile", "RCFile", "InputFormat"));
		columnTypeList = new ArrayList<String>(Arrays.asList("TINYINT",
				"SMALLINT", "INT", "BIGINT", "BOOLEAN", "FLOAT", "DOUBLE",
				"STRING", "BINARY", "TIMESTAMP"));
	}

	@Override
	public void onModuleLoad() {
		if ("".equals(getTokenid())) {
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
							cleanup();
						}
					});
		}
	}

	@UiHandler({ "addField" })
	void onAddFieldGotClick(ClickEvent e) {
		insertOneFieldRow(fieldsGrid);
		fieldScrollPanel.scrollToBottom();
	}

	@UiHandler({ "genHql" })
	void onGenerateHqlGotClick(ClickEvent e) {
		StringBuilder hql = new StringBuilder();
		hql.append("CREATE ");
		if (externalTable.getValue()) {
			hql.append("EXTERNAL ");
		}
		hql.append("TABLE IF NOT EXISTS `").append(dbname.getValue())
				.append(".").append(tablename.getValue()).append("`\n(\n    ");

		int rowCount = fieldsGrid.getRowCount();
		List<String> fieldList = new LinkedList<String>();
		for (int i = 0; i < rowCount - 1; i++) {
			String fName = ((TextBox) fieldsGrid.getWidget(i, 1)).getValue()
					.trim();
			int selectedIdx = ((ListBox) fieldsGrid.getWidget(i, 3))
					.getSelectedIndex();
			if (selectedIdx == -1) {
				continue;
			}
			String fType = ((ListBox) fieldsGrid.getWidget(i, 3))
					.getValue(selectedIdx);
			String fDesc = ((TextBox) fieldsGrid.getWidget(i, 5)).getValue()
					.trim();
			if (!"".equals(fName) && columnTypeList.contains(fType)) {
				StringBuilder sb = new StringBuilder();
				sb.append(fName).append("      ").append(fType);
				if (!"".equals(fDesc)) {
					sb.append("      COMMENT '").append(fDesc).append("'");
				}
				fieldList.add(sb.toString());
			}
		}
		hql.append(join(fieldList, ",\n    ")).append("\n)\n");
		if (!"".equals(tableDesc.getValue())) {
			hql.append("COMMENT '").append(tableDesc.getValue()).append("'\n");
		}
		hql.append("ROW FORMAT DELIMITED\n FIELDS TERMINATED BY '")
				.append(fieldTerminator.getValue(fieldTerminator
						.getSelectedIndex()))
				.append("'\n LINES TERMINATED BY '\\n'\n").append("STORED AS ");
		String ff = fileFormat.getValue(fileFormat.getSelectedIndex());
		if ("InputFormat".equalsIgnoreCase(ff)) {
			hql.append("\n     INPUTFORMAT   '").append(inputformat.getValue())
					.append("'\n     OUTPUTFORMAT   '")
					.append(outputformat.getValue()).append("'\n");
		} else {
			hql.append(ff).append("\n");
		}
		if (!"".equals(location.getValue())) {
			hql.append("LOCATION '").append(location.getValue()).append("'");
		}
		hqlTextArea.setText(hql.toString());
	}

	@UiHandler({ "execHql" })
	void onExecuteHqlGotClick(ClickEvent e) {
		String hql = hqlTextArea.getValue().trim();
		if (!hql.toLowerCase().startsWith("create ")) {
			return;
		}
		hiveQueryService.createTable(getTokenid(), hql,
				new AsyncCallback<ResultStatusBo>() {
					@Override
					public void onSuccess(ResultStatusBo result) {
						if (result.isSuccess()) {
							popupPanel.setMessage("创建表成功!");
							popupPanel.center();
						} else {
							popupPanel.setMessage("创建表失败!" + result.getMessage());
							popupPanel.center();
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						popupPanel.setMessage("创建表失败!");
						popupPanel.center();
					}
				});
	}

	@UiHandler("fileFormat")
	void onFileFormatChange(ChangeEvent event) {
		if ("InputFormat".equalsIgnoreCase(fileFormat.getValue(fileFormat
				.getSelectedIndex()))) {
			inputformat.setEnabled(true);
			outputformat.setEnabled(true);
		} else {
			inputformat.setEnabled(false);
			outputformat.setEnabled(false);
		}
	}

	private void initialize() {
		dbname = new SuggestBox(createDatabaseNamesOracle());
		Widget widget = uiBinder.createAndBindUi(this);

		tabPanel.selectTab(0);
		inputformat.setEnabled(false);
		outputformat.setEnabled(false);

		insertOneFieldRow(fieldsGrid);
		insertOneFieldRow(fieldsGrid);
		insertOneFieldRow(fieldsGrid);
		for (String d : fieldDelimiterSet) {
			fieldTerminator.addItem(d);
		}
		fieldTerminator.setSelectedIndex(0);
		for (String f : fileFormatSet) {
			fileFormat.addItem(f);
		}
		fileFormat.setSelectedIndex(1);

		uploadFilePanel.add(new UploadFilePanel(""));
		RootPanel.get("createtable").add(widget);
	}

	private void insertOneFieldRow(Grid grid) {
		int insertRowNum = grid.getRowCount() - 1;
		grid.insertRow(insertRowNum);
		grid.resizeColumns(6);

		grid.setWidget(insertRowNum, 0, new Label("*字段名:"));
		grid.setWidget(insertRowNum, 1, new TextBox());
		grid.setWidget(insertRowNum, 2, new Label("*字段类型:"));
		grid.setWidget(insertRowNum, 3, getColumnTypeListBox());
		grid.setWidget(insertRowNum, 4, new Label("*字段注释:"));
		grid.setWidget(insertRowNum, 5, new TextBox());
	}

	private ListBox getColumnTypeListBox() {
		ListBox box = new ListBox(false);
		for (String col : columnTypeList) {
			box.addItem(col);
		}
		return box;
	}

	private MultiWordSuggestOracle createDatabaseNamesOracle() {
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
		oracle.addAll(Arrays.asList("default", "bi", "arch", "algo", "dpdp",
				"search", "ba", "tuangou", "mobile"));
		return oracle;
	}

	private <E> String join(Collection<E> c, String separator) {
		if (c == null) {
			return "";
		}
		Iterator<E> iterator = c.iterator();
		if (iterator == null) {
			return "";
		}
		if (!iterator.hasNext()) {
			return "";
		}
		Object first = iterator.next();
		if (!iterator.hasNext()) {
			return first.toString();
		}
		// two or more elements
		StringBuilder buf = new StringBuilder(256);
		if (first != null) {
			buf.append(first);
		}
		while (iterator.hasNext()) {
			buf.append(separator);
			Object obj = iterator.next();
			if (obj != null) {
				buf.append(obj);
			}
		}
		return buf.toString();
	}
}
