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
import android.widget.Toast;

public class VideoRecordModifyPwd extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_modify_pwd);
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
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.btn_ok: {
			EditText oldPwd = (EditText) findViewById(R.id.et_old_pwd);
			oldPwd.setError(null);
			String pwdString = oldPwd.getText().toString();
			if (!pwd.equals(pwdString)) {
				oldPwd.setError("原始密码不正确");
				return;
			} else {
				EditText pwdEditText = (EditText) findViewById(R.id.et_pwd);
				pwdString = pwdEditText.getText().toString();
				if (TextUtils.isEmpty(pwdString)) {
					pwdEditText.setError("请输入密码");
					return;
				}
				EditText confirmEditText = (EditText) findViewById(R.id.et_pwd_confirm);
				confirmEditText.setError(null);
				String pwdConfirm = confirmEditText.getText().toString();
				if (!pwdString.equals(pwdConfirm)) {
					confirmEditText.setError("您输入的密码不一致");
					return;
				}
				sp.edit().putString("key_pwd", pwdString).apply();
				setResult(RESULT_OK);
				Toast.makeText(this, "密码已经修改，请使用新密码登录", Toast.LENGTH_SHORT)
						.show();
				finish();
			}
		}
			break;
		case R.id.btn_modify: {
			Intent intent = new Intent(this, VideoRecordModifyPwd.class);
			startActivity(intent);
			sp.edit().remove("key_pwd").apply();
			setContentView(R.layout.setting);
			findViewById(R.id.first_use).setVisibility(View.GONE);
		}
			break;
		default:
			break;
		}
	}

}
