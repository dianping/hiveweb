package com.dianping.cosmos.hive.client;

import gwtupload.client.IUploadStatus.Status;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.MultiUploader;
import gwtupload.client.PreloadedImage;
import gwtupload.client.PreloadedImage.OnLoadPreloadedImageHandler;

import com.dianping.cosmos.hive.client.bo.ResultStatusBo;
import com.dianping.cosmos.hive.client.service.HiveQueryServiceAsync;
import com.dianping.cosmos.hive.client.widget.CustomPopupPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;

public class UploadFilePanel extends HTMLPanel {

	private FlexTable descLayout = new FlexTable();
	private TextBox dbname = new TextBox();
	private TextBox tbname = new TextBox();
	private CheckBox overwrite = new CheckBox("是");
	private TextBox partitionCondition = new TextBox();
	private FlowPanel panelImages = new FlowPanel();
	private MultiUploader defaultUploader = new MultiUploader();
	private Button submitBut = new Button("提 交");
	private CustomPopupPanel popupPanel = new CustomPopupPanel();

	private final HiveQueryServiceAsync hiveQueryService = HiveQueryServiceAsync.Util
			.getInstance();

	private String uploadFileLocation = "";
	private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {
		public void onFinish(IUploader uploader) {
			if (uploader.getStatus() == Status.SUCCESS) {

				new PreloadedImage(uploader.fileUrl(), showImage);

				// The server sends useful information to the client by default
				UploadedInfo info = uploader.getServerInfo();
				if (info.message != null) {
					uploadFileLocation = info.message;
				}
			}
		}
	};

	// Attach an image to the pictures viewer
	private OnLoadPreloadedImageHandler showImage = new OnLoadPreloadedImageHandler() {
		public void onLoad(PreloadedImage image) {
			image.setWidth("75px");
			panelImages.add(image);
		}
	};

	public UploadFilePanel(String html) {
		super(html);
		descLayout.setCellSpacing(6);
		descLayout.setWidth("350px");
		FlexCellFormatter cellFormatter = descLayout.getFlexCellFormatter();

		// Add a title to the form
		descLayout.setHTML(0, 0, "上传表数据");
		cellFormatter.setColSpan(0, 0, 2);
		cellFormatter.setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);

		// Add some standard form options
		descLayout.setHTML(1, 0, "* 数据库名:");
		descLayout.setWidget(1, 1, dbname);
		descLayout.setHTML(2, 0, "* 表名:");
		descLayout.setWidget(2, 1, tbname);
		descLayout.setHTML(3, 0, "* 是否覆盖表数据:");
		overwrite.setValue(true);
		descLayout.setWidget(3, 1, overwrite);
		descLayout.setHTML(4, 0, "分区条件(可选):");
		descLayout.setWidget(4, 1, partitionCondition);

		add(descLayout);
		add(panelImages);
		
		// Add a finish handler which will load the image once the upload
		// finishes
		defaultUploader.addOnFinishUploadHandler(onFinishUploaderHandler);
		defaultUploader.setMaximumFiles(1);
		
		add(defaultUploader);
		add(submitBut);

		bind();
	}

	private void bind() {
		submitBut.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				String db = dbname.getValue().trim();
				String table = tbname.getValue().trim();
				if (!"".equals(db) && !"".equals(table)) {
					
					// only if the uploading file is done
					if (defaultUploader.getSuccessUploads() == 1 && !"".equals(uploadFileLocation)){
						submitBut.setEnabled(false);
						hiveQueryService.uploadTableFile(LoginComponent.getTokenid(),
								LoginComponent.getUsername(), db, table, uploadFileLocation, 
								overwrite.getValue(), partitionCondition.getValue().trim(),
								new AsyncCallback<ResultStatusBo>() {
									@Override
									public void onSuccess(ResultStatusBo result) {
										if (result.isSuccess()) {
											popupPanel.setMessage("上传数据成功!"
													+ result.getMessage());
											popupPanel.center();
											submitBut.setEnabled(true);
											defaultUploader.reset();
											uploadFileLocation = "";
										} else {
											popupPanel.setMessage("上传数据失败!\n"
													+ result.getMessage());
											popupPanel.center();
											submitBut.setEnabled(true);
										}
									}

									@Override
									public void onFailure(Throwable caught) {
										popupPanel.setMessage("上传数据失败!!");
										popupPanel.center();
										submitBut.setEnabled(true);
									}
								});
					} else {
						popupPanel.setMessage("上传文件不能为空!");
						popupPanel.center();
					}
				} else {
					popupPanel.setMessage("数据库名和表名不能为空!");
					popupPanel.center();
				}
			}
		});
	}
}
