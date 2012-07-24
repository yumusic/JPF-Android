package com.example.com;

import android.app.Activity;
import android.app.ActivityThread;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SampleProjectActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		System.out.println("act1");

		Button b1 = (Button) findViewById(R.id.buttonPrint1);
		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("Button 1 Clicked!!!");
				// Intent i = new Intent(SampleProjectActivity.this,
				// com.example.vdm.SampleProjectActivity.class);
				// startActivity(i);
			}
		});

		Button b2 = (Button) findViewById(R.id.buttonPrint2);
		b2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("Button 2 Clicked!!!");
				Intent i = new Intent(SampleProjectActivity.this,
						com.example.vdm.SampleProjectActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

	}

	@Override
	protected void onStart() {
		super.onStart();
		System.out.println("1 onStart()");

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		System.out.println("1 onRestart()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("1  onResume()");

	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		System.out.println("1  onPostResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("1  onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		System.out.println("1  onStop()");

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("1  onDestroy()");

	}

	@Override
	public Intent getIntent() {
		System.out.println("1  getIntent()");
		return super.getIntent();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		System.out.println("1  onRestoreInstanceState()");
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		System.out.println("1  onNewIntent()");

		super.onNewIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		System.out.println("1  onSaveInstanceState()");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("1  onActivityResult()");
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * The main entry point to the application
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ActivityThread.main(null);
	}
}