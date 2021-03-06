package com.qinzi.activity;

import java.io.File;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;

import com.qinzi.dialog.CommonActivityDialog;
import com.qinzi.dialog.DialogFactory;

public class MainActivity extends ActivityGroup {
	
	private CommonActivityDialog dialog;
	
	private Button cameraButton;
	
	private Button albumButton;
	
	enum TAB_TAG {
		HOME, HOT, ACCOUNT, LOGIN
	}
	
	private String tempFilePath = Environment.getExternalStorageDirectory() + File.separator + "qinzi/temp";
	
	private String imageSavePath = Environment.getExternalStorageDirectory() + File.separator + "qinzi/user";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup(this.getLocalActivityManager());

		TabHost.TabSpec spec = tabHost.newTabSpec(TAB_TAG.HOME.name());
		spec.setContent(new Intent(this, ListActivity.class));
		spec.setIndicator(TAB_TAG.HOME.name());
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec(TAB_TAG.HOT.name());
		spec.setContent(new Intent(this, HotActivity.class));
		spec.setIndicator(TAB_TAG.HOT.name());
		tabHost.addTab(spec);
		/*
		spec = tabHost.newTabSpec("TAB3");
        spec.setContent(new Intent(this, CameraActivity.class));
        spec.setIndicator("照相");
        tabHost.addTab(spec);
        */
        spec = tabHost.newTabSpec(TAB_TAG.ACCOUNT.name());
        spec.setContent(new Intent(this, AccountActivity.class));
        spec.setIndicator(TAB_TAG.ACCOUNT.name());
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec(TAB_TAG.LOGIN.name());
        spec.setContent(new Intent(this, WeiboAuthorizeActivity.class));
        spec.setIndicator(TAB_TAG.LOGIN.name());
        tabHost.addTab(spec);

		tabHost.setCurrentTabByTag(TAB_TAG.HOME.name());
		
		//检查文件夹是否创建
		File tempFileDir = new File(tempFilePath);
		if (!tempFileDir.exists()) {
			tempFileDir.mkdirs();
		}
		File imageSaveDir = new File(imageSavePath);
		if (!imageSaveDir.exists()) {
			imageSaveDir.mkdirs();
		}
		ImageButton camera = (ImageButton)super.findViewById(R.id.tab_camera);
		dialog = DialogFactory.getInstance().getCameraDialog(MainActivity.this);
		cameraButton = (Button) dialog.getDialog().findViewById(R.id.cameraButton);
		albumButton = (Button) dialog.getDialog().findViewById(R.id.albumButton);
		
		camera.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				dialog.show();
			}
		});	
		cameraButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(tempFilePath, "temp.jpg")));
				startActivityForResult(intent, PhotoEditActivity.CODE_ACTION_IMAGE_CAPTURE);
			}
		});
		albumButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, PhotoEditActivity.CODE_ACTION_PICK);
			}
		});
		
		ImageButton tab_home = (ImageButton)super.findViewById(R.id.tab_home);
		tab_home.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				TabHost tab = (TabHost) findViewById(R.id.tabhost);
				tab.setCurrentTabByTag(TAB_TAG.HOME.name());
				TextView title = (TextView)findViewById(R.id.title);
				title.setText(getText(R.string.app_name));
			}
		});
		ImageButton tab_hot = (ImageButton)super.findViewById(R.id.tab_hot);
		tab_hot.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				TabHost tab = (TabHost) findViewById(R.id.tabhost);
				tab.setCurrentTabByTag(TAB_TAG.HOT.name());
				TextView title = (TextView)findViewById(R.id.title);
				title.setText(getText(R.string.hot));
			}
		});
		ImageButton tab_account = (ImageButton)super.findViewById(R.id.tab_account);
		tab_account.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				TabHost tab = (TabHost) findViewById(R.id.tabhost);
				tab.setCurrentTabByTag(TAB_TAG.ACCOUNT.name());
				TextView title = (TextView)findViewById(R.id.title);
				title.setText(getText(R.string.account));
			}
		});
		
		ImageButton tab_login = (ImageButton)super.findViewById(R.id.tab_login);
		tab_login.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				TabHost tab = (TabHost) findViewById(R.id.tabhost);
				tab.setCurrentTabByTag(TAB_TAG.LOGIN.name());
				TextView title = (TextView)findViewById(R.id.title);
				title.setText(getText(R.string.login));
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PhotoEditActivity.CODE_NONE)
			return;
		
		if (resultCode != Activity.RESULT_OK)
			return;
		
		if (requestCode == PhotoEditActivity.CODE_ACTION_IMAGE_CAPTURE) {
			File picture = new File(tempFilePath, "temp.jpg");
			cropPhoto(Uri.fromFile(picture));
		}
		
		if (data == null)
			return;
		
		Uri uri = data.getData();
		if (requestCode == PhotoEditActivity.CODE_ACTION_PICK) {
			cropPhoto(uri);
		}

		if (requestCode == PhotoEditActivity.CODE_FINISHED) {
			String imagePath = data.getAction().replace("file:///", "");
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, PhotoEditActivity.class);
			intent.setDataAndType(uri, "image/*");
			intent.putExtra("imagePath", imagePath);
			startActivity(intent);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	public void cropPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);    
//		intent.putExtra("outputX", 128);
//		intent.putExtra("outputY", 128);
//		intent.putExtra("return-data", true);
		String filePath = imageSavePath + File.separator + "jeffreyzhang";
		String fileName = System.currentTimeMillis() + ".jpg";
		File outputFilePath = new File(filePath);
		if (!outputFilePath.exists()) {
			outputFilePath.mkdirs();
		}
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath, fileName)));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(intent, PhotoEditActivity.CODE_FINISHED);
	}

}
