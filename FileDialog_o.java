package de.qland.android.gridraw;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FileDialog_o extends ListView
{
	private Context mainActivity;
	
	public FileDialog_o(Context context) 
	{
		super(context);
		mainActivity = context;
		
		final RelativeLayout rlay = new RelativeLayout(context);	// prepare relative layout
		rlay.setId(1000);

		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);		
		
		String state = Environment.getExternalStorageState();		// check if external storage is available for read and write

		if(!Environment.MEDIA_MOUNTED.equals(state))
			return;													// error external storage not available

		File fh = new File(context.getExternalFilesDir(null), "");	// get handle to files in apps public directory

		if(!fh.exists())
			return;
		
		File[] files = fh.listFiles();
		List<String> item = new ArrayList<String>();
		List<String> path = new ArrayList<String>();
		
		for(int i=0; i<files.length; i++){
			File f = files[i];
			path.add(fh.getPath());
			
			if(f.isDirectory())
				item.add(f.getName() + "/");
			else
				item.add(f.getName());
	    }
		
		for(int i=0; i<50; i++){
			item.add("item" + i);
	    }
		
		this.setId(1001);
		
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(context, R.layout.filedialog, item);
		this.setAdapter(fileList);
		rlay.addView(this, param);	
		
//		Button btn = null;
//		btn = new Button(context);
//		btn.setText("Test");
//		btn.setId(1001);
//		btn.setWidth(100);
//		rlay.addView(btn, param);
		
		ViewGroup.LayoutParams generalLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		((MainActivity)context).addContentView(rlay, generalLayoutParam);
		
		setupListeners();
	}
	
	private void setupListeners()
	{
	    setOnItemClickListener(new OnItemClickListener(){
	        @Override
	        public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3){
//	        	toaster("onItemClick -> " + arg2);
	        	toaster(((TextView)view).getText().toString());
	        	setSelection(pos);
	        	
	        }
	    });
	    
	    setOnItemSelectedListener(new ListView.OnItemSelectedListener(){
	        @Override
	        public void onItemSelected(AdapterView<?> a, View v, int i, long l){
	        	toaster("onItemSelected -> " + i);
	        }

	        @Override
	        public void onNothingSelected(AdapterView<?> arg0){
	        	toaster("onNothingSelected -> ");
	        }
	    });	    
	    
//		((Button)findViewById(1001)).setOnClickListener(new View.OnClickListener(){		// setup listeners
//		public void onClick(View view){
//			Button b = (Button)findViewById(1001);
//			b.setOnClickListener(null);				// remove listener
//			rlay.removeView(b);						// remove button
//			
//			ViewGroup vg = (ViewGroup)(rlay.getParent());
//			vg.removeView(rlay);
//		}
//		});
	}

	public void toaster(final String text)
	{	   
		new Thread(){
			public void run(){
				((Activity)mainActivity).runOnUiThread(new Runnable(){
                    public void run(){
                    	Toast.makeText(mainActivity, text, Toast.LENGTH_SHORT).show();
                    }
                });
	        }
	    }.start();	
	}
	

	 new AlertDialog.Builder(this)
 	.setIcon(R.drawable.ic_delete)
 	.setTitle("[" + file.getName() + "] folder can't be read!")
 	.setPositiveButton("OK", new DialogInterface.OnClickListener(){
 		@Override
 		public void onClick(DialogInterface dialog, int which){
 			// TODO Auto-generated method stub
 		}
 	}).show();
	
     new AlertDialog.Builder(this)
    .setIcon(R.drawable.ic_delete)
    .setTitle("[" + file.getName() + "]")
    .setPositiveButton("OK", new DialogInterface.OnClickListener(){
    	@Override
    	public void onClick(DialogInterface dialog, int which) {
    		// TODO Auto-generated method stub
    	}
    }).show();
	
	
	
	
	
	
	
	
	
	
	
}
