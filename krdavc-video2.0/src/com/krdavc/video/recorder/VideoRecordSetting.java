package com.krdavc.video.recorder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class VideoRecordSetting extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
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

	@Override
	public void onClick(View arg0) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
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
				sp.edit().putString("key_pwd", pwdString).apply();
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
