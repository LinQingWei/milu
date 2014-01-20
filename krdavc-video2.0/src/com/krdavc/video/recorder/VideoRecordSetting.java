package com.krdavc.video.recorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;

public class VideoRecordSetting extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		if (!checkGlobalPwd(this)) {
			EditText tView = new EditText(this);
			tView.setGravity(Gravity.CENTER);
			tView.setHint("请输入公司密码");
			tView.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					if (s.toString().equals("894436")) {
						initByGlobalPwdValid();
						setGlobalPwd(VideoRecordSetting.this);
					}
				}
			});
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
					android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			setContentView(tView, params);
			return;
		} else
			initByGlobalPwdValid();
	}

	private void initByGlobalPwdValid() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String pwd = sp.getString("key_pwd", null);
		if (pwd == null) {
			setContentView(R.layout.setting);
		} else {
			setContentView(R.layout.setting1);
			findViewById(R.id.btn_modify).setOnClickListener(this);
		}
		findViewById(R.id.btn_ok).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
	}

	private boolean checkGlobalPwd(Context c) {
		return getPreferences(MODE_PRIVATE).getBoolean("contex.getSystemService(", false);
	}

	private void setGlobalPwd(Context c) {
		getPreferences(MODE_PRIVATE).edit().putBoolean("contex.getSystemService(", true).commit();
	}

	@Override
	public void onClick(View arg0) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String pwd = sp.getString("key_pwd", null);
		int id = arg0.getId();
		switch (id) {
		case R.id.btn_cancel:
			finish();
			break;
		case R.id.btn_ok: {
			EditText pwdEditText = (EditText) findViewById(R.id.et_pwd);
			pwdEditText.setError(null);
			EditText confirmEditText = (EditText) findViewById(R.id.et_pwd_confirm);
			pwdEditText.setError(null);
			String pwdString = pwdEditText.getText().toString();
			if (pwd == null) {
				if (TextUtils.isEmpty(pwdString)) {
					pwdEditText.setError("请输入密码");
					return;
				}
				String pwdConfirm = confirmEditText.getText().toString();
				if (!pwdString.equals(pwdConfirm)) {
					confirmEditText.setError("您输入的密码不一致");
					return;
				}
				sp.edit().putString("key_pwd", pwdString).commit();
			} else {
				if (!pwd.equals(pwdString)) {
					pwdEditText.setError("密码不正确");
					return;
				}
			}
			Intent intent = new Intent(this, VideoRecordActivity.class);
			startActivity(intent);
			finish();
		}
			break;
		case R.id.btn_modify: {
			Intent intent = new Intent(this, VideoRecordModifyPwd.class);
			startActivityForResult(intent, 1111);
			EditText pwdEditText = (EditText) findViewById(R.id.et_pwd);
			pwdEditText.setText("");
			pwdEditText.setError(null);
		}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1111) {
			if (resultCode == RESULT_OK) {

			} else if (resultCode == RESULT_CANCELED) {

			}
		}
	}

}
