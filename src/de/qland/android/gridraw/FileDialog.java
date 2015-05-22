package de.qland.android.gridraw;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileDialog extends ListActivity 
{
	public static String EXTENSION = ".gdr";
	private static int MINFILELEN = 5;
	
	private static String ICICLE_KEY = "fileDialog-view";
	private Menu mMenu;
	private String dialogKind;
	
	private List<String> name = null;
	private List<String> pafi = null;
	private List<String> size = null;
	private List<String> date = null;
	private List<String> item = null;
	
	private String curPath = "";
	private String curFile = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		// actionbar gradient
		GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, 
		new int[] {Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, 
				   Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.GRAY});
			
		ActionBar ab = getActionBar();
		
		Bundle bundle = getIntent().getExtras();
		dialogKind = bundle.getString(MainActivity.EXTRA_DIALOGKIND);
		curPath = bundle.getString(MainActivity.EXTRA_PATH);
		curFile = bundle.getString(MainActivity.EXTRA_FILE);
		
		ab.setBackgroundDrawable(gd);
		String title = getResources().getString(R.string.title_file_dialog) + " " + dialogKind;
		
		if(dialogKind.equals(MainActivity.RENAME))
			title += " <" + curFile + ">";
		
		ab.setTitle(title);
	}
	
	/**
	 * restore prefs from system induced breaks and app finish
	 */
	@Override
    public void onResume()
	{
        super.onResume();

        setContentView(R.layout.activity_file_dialog);
        
        Boolean edit = dialogKind.equals(MainActivity.SAVEAS) || dialogKind.equals(MainActivity.RENAME);
        ((EditText)findViewById(R.id.file)).setFocusable(edit);
        
        invalidateOptionsMenu();		// for power button and history restart 
	}
	
	/**
	 * save prefs to endure system induced breaks and app finish
	 */
	@Override
	public void onPause()
	{
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();		// save prefs
		editor.putBoolean("sortAlpha", mMenu.findItem(R.id.sort_alpha).isChecked());
		editor.putBoolean("sortSize", mMenu.findItem(R.id.sort_size).isChecked());
		editor.putBoolean("sortTime", mMenu.findItem(R.id.sort_time).isChecked());
		editor.putBoolean("sortReverse", mMenu.findItem(R.id.sort_reverse).isChecked());
		editor.commit();
		
		super.onPause();
	}
	
	/**
	 * save state to endure system induced breaks
	 */
    @Override
    public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		
		Bundle map = new Bundle();
		map.putString("curPath", curPath);
		map.putString("curFile", curFile);
		outState.putBundle(ICICLE_KEY, map);
    }
	
    /**
	 * restore state from system induced breaks
	 */
    @Override
	public void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		super.onRestoreInstanceState(savedInstanceState);
		
		Bundle map = savedInstanceState.getBundle(ICICLE_KEY);	

        if(map != null){
        	curPath = map.getString("curPath");
        	curFile = map.getString("curFile");
        }
	}
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		if(!dialogKind.equals(MainActivity.RENAME)){
			File file = new File(pafi.get(position));
			
			if(file.isDirectory()){
				if(dialogKind.equals(MainActivity.OPEN))
					curFile = "";
				
				if(file.canRead())
					getDir(pafi.get(position));
				else
					Toast.makeText(this, "File not readable", Toast.LENGTH_SHORT).show();
			}
			else{
				curFile = name.get(position);
			}
		
			setFileText();
		}
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.file_dialog, menu);
		mMenu = menu;
		
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);		// get menu prefs
		menu.findItem(R.id.sort_alpha).setChecked(sharedPref.getBoolean("sortAlpha", true));
		menu.findItem(R.id.sort_size).setChecked(sharedPref.getBoolean("sortSize", false));
		menu.findItem(R.id.sort_time).setChecked(sharedPref.getBoolean("sortTime", false));
		menu.findItem(R.id.sort_reverse).setChecked(sharedPref.getBoolean("sortReverse", false));
		
		initDialog();	// onCreateOptionsMenu() is done after onResume()
		
		return super.onCreateOptionsMenu(menu);
	}
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {		
    	switch(item.getItemId()){
        	case R.id.sort_alpha:
        		checker(item);
        		break;
        	case R.id.sort_size:
           		checker(item);
	            break;
        	case R.id.sort_time:
           		checker(item);
	            break;
        	case R.id.sort_reverse:
        		item.setChecked(!item.isChecked());
        		getDir(curPath);
	            break;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
	
    private void checker(MenuItem item)
    {
    	item.setChecked(!item.isChecked());
    	
		if(item.isChecked()){
			MenuItem i;
			
			i = mMenu.findItem(R.id.sort_alpha);
			i.setChecked(i==item ? true : false);
			
			i = mMenu.findItem(R.id.sort_size);
			i.setChecked(i==item ? true : false);		
			
			i = mMenu.findItem(R.id.sort_time);
			i.setChecked(i==item ? true : false);
		}
		
		getDir(curPath);
    }
    
	private void initDialog()
	{	
		((Button)findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				finish();
			}
		});   
		
		((Button)findViewById(R.id.ok)).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				if(dialogKind.equals(MainActivity.SAVEAS) || dialogKind.equals(MainActivity.RENAME))
					curFile = ((EditText)findViewById(R.id.file)).getText().toString();
				
				if(checkFile()){
					Intent resultIntent = new Intent();
					resultIntent.putExtra(MainActivity.EXTRA_PATH, curPath);
					resultIntent.putExtra(MainActivity.EXTRA_FILE, curFile);
					setResult(Activity.RESULT_OK, resultIntent);
					finish();	
				}
			}
		});		
		
		setFileText();
		getDir(curPath);
	}
	
	private void setFileText()
	{
		String cf;
		
		if(curFile.equals(""))
			cf = getResources().getString(R.string.nofile);
		else
			cf = curFile;
		
		EditText fileText = ((EditText)findViewById(R.id.file));
		fileText.setText(cf);
		
		int stop = cf.lastIndexOf(".");
		
		if(stop != -1)
			fileText.setSelection(0, stop);
	}
	
	private void getDir(String dirPath)
	{
		curPath = dirPath;
		((TextView)findViewById(R.id.path)).setText(dirPath);
		
		name = new ArrayList<String>();
		pafi = new ArrayList<String>();		// path + filename
		size = new ArrayList<String>();
		date = new ArrayList<String>();
		item = new ArrayList<String>();
		
		File f = new File(dirPath);
		File[] files = f.listFiles();
		sortFiles(files);
		
		int i0 = 0;
		
		if(!dirPath.equals("/")){
			name.add("..");
			pafi.add(f.getParent()); 
			size.add("");
			date.add("");
			i0++;
		}
 
		int maxname = 12;
		int maxsize = 8;
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
		
		for(int i=0; i<files.length; i++){
			File file = files[i];
			pafi.add(file.getPath());
			
			if(file.isDirectory()){
				name.add(file.getName() + "/");
				size.add("");
				date.add(sdf.format(new Date(file.lastModified())));
			}
			else{
				name.add(file.getName());
				size.add(String.valueOf(file.length()));
				date.add(sdf.format(new Date(file.lastModified())));
			}
			
			if(name.get(i0+i).length() > maxname)
				maxname = name.get(i0+i).length();
			
			if(size.get(i0+i).length() > maxsize)
				maxsize = size.get(i0+i).length();
		}
		
		for(int i=0; i<name.size(); i++){
			String s = name.get(i) + space(maxname - name.get(i).length() + 2);
			s += space(maxsize - size.get(i).length()) + size.get(i) + "  ";
			s += date.get(i) ;
			item.add(s);
		}
		
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
		setListAdapter(fileList);
	}

	private String space(int n)
	{
		if(n < 0) n = 0;
		String s = new String(new char[n]).replace("\0", " ");
		return s;
	}
	
	private void sortFiles(File[] files)
	{
		if(mMenu == null)
			return;
		
		final boolean reverse = mMenu.findItem(R.id.sort_reverse).isChecked();
		
    	if(mMenu.findItem(R.id.sort_alpha).isChecked()){
    		Arrays.sort(files, new Comparator<File>(){
    		    public int compare(File f1, File f2){
    		    	String n1 = f1.getName().toLowerCase(Locale.ENGLISH);
    		    	String n2 = f2.getName().toLowerCase(Locale.ENGLISH);
    		    	if(f1.isDirectory()) n1 = " " + n1;		// directories before filenames
    		    	if(f2.isDirectory()) n2 = " " + n2;
    		    	return reverse ? n2.compareTo(n1) : n1.compareTo(n2);
    		    } 
    		});    		
    	}
    	else if(mMenu.findItem(R.id.sort_size).isChecked()){
      		Arrays.sort(files, new Comparator<File>(){
      			public int compare(File f1, File f2){
    		    	int result;
    		    	
    		    	if(f1.isDirectory() && f2.isDirectory()){
        		    	String n1 = f1.getName().toLowerCase(Locale.ENGLISH);
        		    	String n2 = f2.getName().toLowerCase(Locale.ENGLISH);
    		    		result = reverse ? n2.compareTo(n1) : n1.compareTo(n2);
    		    	}
    		    	else{
    		    		Long s1 = f1.length();
        		    	Long s2 = f2.length();
        		    	if(f1.isDirectory()) s1 = 0L;		// directories before filenames
        		    	if(f2.isDirectory()) s2 = 0L;   
        		    	result = reverse ? s2.compareTo(s1) : s1.compareTo(s2);
    		    	}
    		    	
    		    	return result;  		    	
    		    } 
    		});
    	}
    	else if(mMenu.findItem(R.id.sort_time).isChecked()){
     		Arrays.sort(files, new Comparator<File>(){
      			public int compare(File f1, File f2){
    		    	Long m1 = f1.lastModified();
    		    	Long m2 = f2.lastModified();		    	
    		    	return reverse ? m2.compareTo(m1) : m1.compareTo(m2); 		    	
    		    } 
    		});
    	}		   
	}
	
	private boolean checkFile()
	{
		if(curFile.lastIndexOf(".") == -1) 
			curFile += EXTENSION;
		
		int len = curFile.length();
		
		if(len < MINFILELEN){
			Toast.makeText(this, "error: invalid filename", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(!curFile.substring(len-EXTENSION.length()).equals(EXTENSION)){
			Toast.makeText(this, "error: wrong extension", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(dialogKind.equals(MainActivity.SAVEAS) || dialogKind.equals(MainActivity.RENAME)){
			File fh = new File(curPath, curFile);
			
			if(fh.exists()){
				Toast.makeText(this, "error: file already exists", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		
		return true;
	}
}

//File file = new File(context.getExternalFilesDir(null), "subdir"));
//
//if(!file.mkdirs())		// make a subdir in apps public directory
//	return true;	

//new AlertDialog.Builder(this)
//.setIcon(R.drawable.ic_delete)
//.setTitle("[" + file.getName() + "] folder can't be read!")
//.setPositiveButton("OK", new DialogInterface.OnClickListener(){
//	@Override
//	public void onClick(DialogInterface dialog, int which){
//		
//	}
//}).show();
//
//new AlertDialog.Builder(this)
//.setIcon(R.drawable.ic_delete)
//.setTitle("[" + file.getName() + "]")
//.setPositiveButton("OK", new DialogInterface.OnClickListener(){
//	@Override
//	public void onClick(DialogInterface dialog, int which) {
//		
//	}
//}).show();











