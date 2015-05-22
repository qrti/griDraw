package de.qland.android.gridraw;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.Locale;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.Editable;
import android.util.*;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private final float ACTION_ICON_SPACE = 0.75f;		
	private static String ICICLE_KEY = "gridraw-view";
	
	private String curPath = "";
	private String curFile = "";
	private int selItemId;
	
	public GraficPanel mGraficPanel;					
	private Menu mMenu = null;
	public WeakRefHandler mHandler = new WeakRefHandler(this);

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		
		// actionbar gradient
		GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, 
		new int[] {Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, 
				   Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.LTGRAY, Color.GRAY});
			
		getActionBar().setBackgroundDrawable(gd);	
    }

	/**
	 * restore prefs from system induced breaks and app finish
	 */
    @Override
    public void onResume()
	{
        super.onResume();
        
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);	// get prefs
		curPath = sharedPref.getString("curPath", "");
		curFile = sharedPref.getString("curFile", "");
		selItemId = sharedPref.getInt("selItemId", R.id.menu_pointer);
		
		mGraficPanel = new GraficPanel(this); 
    	checkMenuItem(selItemId);
    	
		if(!restoreData())
			getActionBar().setTitle(curFile);
		
		setContentView(mGraficPanel);
	}

	/**
	 * save prefs to endure system induced breaks and app finish
	 */
	@Override
	public void onPause()
	{
		saveProject(curPath, curFile, false);			// save project
		putPrefs(curPath, curFile, getSelItemId());		//      prefs
				
		super.onPause();
	}
	
	@Override
	public void onDestroy()
	{
		putPrefs(null, null, R.id.menu_pointer);		// select pointer for next app start
		
		super.onDestroy();
	}
	
	/**
	 * save state to endure system induced breaks
	 */
    @Override
    public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		
       	Bundle map = new Bundle();
       	
       	if(map != null){
       		map.putInt("selItemId", getSelItemId());	// onDestroy() resets selItemId on prefs
       		outState.putBundle(ICICLE_KEY, map);		// so save it 
       	}
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
        	int id = map.getInt("selItemId", R.id.menu_pointer);
        	putPrefs(null, null, id);	// onResume() is next and restores selItemId from prefs
        }
	}
	
//    /**
//     * active if manifest.xml section activity includes
//     * android:configChanges="orientation|keyboardHidden|screenSize"
//     */
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) 
//	{
//		Toast.makeText(this, "configchanged", Toast.LENGTH_SHORT).show();
//		super.onConfigurationChanged(newConfig);
//	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		super.onWindowFocusChanged(hasFocus);
		
		if(prepCount == 0)
			invalidateOptionsMenu();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		mMenu = menu;	
		checkMenuItem(selItemId);
		
		return super.onCreateOptionsMenu(menu);
	}	
	
	private int prepCount = 0;
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) 
    {				   	
    	if(prepCount++ == 1)
    		adaptActionIcons(menu);	

 		return super.onPrepareOptionsMenu(menu);
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {		
    	int id = item.getItemId();
    	
    	switch(id){
        	case R.id.menu_pointer:
				checkMenuItem(id);					
	            return true;

        	case R.id.menu_changegrid:
				checkMenuItem(id);													
	            return true;

        	case R.id.menu_drawline:
				checkMenuItem(id);						
	            return true;

        	case R.id.menu_drawrect:
				checkMenuItem(id);						
	            return true;

        	case R.id.menu_drawcircle:
				checkMenuItem(id);														
	            return true;
				
        	case R.id.menu_drawfree:
				checkMenuItem(id);	
	            return true;
				
        	case R.id.menu_drawtext:
				checkMenuItem(id);													
	            return true;
        
        	case R.id.menu_new:
        		newProject();
        		return true;   
        		
        	case R.id.menu_open:
        		openProject();
        		return true;            

        	case R.id.menu_save:
				saveProject(curPath, curFile, true);
        		return true;            

        	case R.id.menu_saveas:
        		saveAsProject();
        		return true;  

        	case R.id.menu_rename:
        		renameProject();
        		return true; 
        		
        	case R.id.menu_exit:
        		finish();	            
        		return true;
        		
        	default:
        		return super.onOptionsItemSelected(item);
    	}
    }    
	
	private void checkMenuItem(int id)
	{	
		checker(id, R.id.menu_pointer, R.drawable.ic_pointer, R.drawable.ic_pointer_sel, Tool.POINTER);
		checker(id, R.id.menu_changegrid, R.drawable.ic_changegrid, R.drawable.ic_changegrid_sel, Tool.CHANGE_GRID);		
		checker(id, R.id.menu_drawline, R.drawable.ic_drawline, R.drawable.ic_drawline_sel, Tool.DRAW_LINE);
		checker(id, R.id.menu_drawrect, R.drawable.ic_drawrect, R.drawable.ic_drawrect_sel, Tool.DRAW_RECT);
		checker(id, R.id.menu_drawcircle, R.drawable.ic_drawcircle, R.drawable.ic_drawcircle_sel, Tool.DRAW_ARC);
		checker(id, R.id.menu_drawfree, R.drawable.ic_drawfree, R.drawable.ic_drawfree_sel, Tool.DRAW_FREE);
		checker(id, R.id.menu_drawtext, R.drawable.ic_drawtext, R.drawable.ic_drawtext_sel, Tool.DRAW_TEXT);
	}
	
	private void checker(int id, int cid, int unselected, int selected, Tool tool)
	{
		if(mMenu != null){
			MenuItem item = mMenu.findItem(cid);
			
			if(cid == id){
				item.setChecked(true);
				item.setIcon(selected);	
				mGraficPanel.setTool(tool);
			}
			else if(item.isChecked()){
				item.setChecked(false);
				item.setIcon(unselected);
			}		
		}
		else{
			if(cid == id)
				mGraficPanel.setTool(tool);
		}
	}
	
    private int getSelItemId()
    {
    	int id = 0;

    	for(int i=0; i<mMenu.size(); i++){
    		MenuItem item = mMenu.getItem(i);
    		
    		if(item.isCheckable() && item.isChecked()){
    			id = item.getItemId();
    			break;
    		}
    	}
    	
    	return id;
    }
    
	private void adaptActionIcons(Menu menu)
	{		
		if(menu != null){
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			int width = (int)(metrics.widthPixels * ACTION_ICON_SPACE);
			int w = 0;

			for(int i=0; i<menu.size(); i++){
				MenuItem item = menu.getItem(i);		

				if(item.isCheckable()){
					w += item.getIcon().getBounds().width() * 2;
					item.setShowAsAction(w<width ? MenuItem.SHOW_AS_ACTION_ALWAYS : MenuItem.SHOW_AS_ACTION_NEVER);				
				}
			}	
		}
	}
	
	private boolean restoreData()
	{
		boolean error;
		
		if(checkFile(curPath, curFile)){
			error = mGraficPanel.readFile(this, curPath + "/" + curFile);
			
			if(error){
				Toast.makeText(this, "error: data not readable", Toast.LENGTH_SHORT).show();
				curFile = "";
			}
		}
		else{
			error = newFilename();
		}
		
		return error;
	}
	
	private boolean newFilename()
	{
		curFile = "";
		
		if(!new File(curPath, "").exists()){
			String state = Environment.getExternalStorageState();		// check if external storage is available for read and write

			if(!Environment.MEDIA_MOUNTED.equals(state)){
				Toast.makeText(this, "error: internal sd-memory not found", Toast.LENGTH_SHORT).show();
				return true;
			}
			
			File file = new File(this.getExternalFilesDir(null), "");		// get handle to files in apps public directory, creates it if necessary
			
			if(!file.exists()){
				Toast.makeText(this, "error: AppDir problem", Toast.LENGTH_SHORT).show();
				return true;
			}
		
			curPath = file.getPath();
		}
		
		for(int i=1; i<100; i++){
			String name = String.format(Locale.US, "%s %02d%s", getResources().getString(R.string.unnamed), i, FileDialog.EXTENSION);
			
			if(!new File(curPath, name).exists()){
				 curFile = name;
				 break;
			}
		}
		
		if(curFile.equals("")){
			Toast.makeText(this, "error: filename number overflow", Toast.LENGTH_SHORT).show();
			return true;
		}
		
		return false; 	// no error
	}
	
	private void newProject()
	{
		saveProject(curPath, curFile, false);
		mGraficPanel.cleanPanel();
		newFilename();
		getActionBar().setTitle(curFile);
	}
	
	final static int CODE_OPEN = 1;
	final static int CODE_SAVEAS = 2;
	final static int CODE_RENAME =3;
	
	public static String EXTRA_DIALOGKIND = "kind";
	public static String EXTRA_PATH = "path";
	public static String EXTRA_FILE = "file";
	
	public static String OPEN = "Open";
	public static String SAVEAS = "Save As";
	public static String RENAME = "Rename";
	
	private void openProject()
	{
		Intent fileDialogIntent = new Intent(MainActivity.this, FileDialog.class);
		fileDialogIntent.putExtra(EXTRA_DIALOGKIND, OPEN); 
		fileDialogIntent.putExtra(EXTRA_PATH, curPath); 
		fileDialogIntent.putExtra(EXTRA_FILE, ""); 
		startActivityForResult(fileDialogIntent, CODE_OPEN);
	}
	
	private void saveAsProject()
	{
		Intent fileDialogIntent = new Intent(MainActivity.this, FileDialog.class);
		fileDialogIntent.putExtra(EXTRA_DIALOGKIND, SAVEAS); 
		fileDialogIntent.putExtra(EXTRA_PATH, curPath); 
		fileDialogIntent.putExtra(EXTRA_FILE, curFile); 
		startActivityForResult(fileDialogIntent, CODE_SAVEAS);
	}
	
	private void renameProject()
	{
		Intent fileDialogIntent = new Intent(MainActivity.this, FileDialog.class);
		fileDialogIntent.putExtra(EXTRA_DIALOGKIND, RENAME); 
		fileDialogIntent.putExtra(EXTRA_PATH, curPath); 
		fileDialogIntent.putExtra(EXTRA_FILE, curFile); 
		startActivityForResult(fileDialogIntent, CODE_RENAME);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{     
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){ 
			case CODE_OPEN:  
				if(resultCode == Activity.RESULT_OK){
					String path = data.getStringExtra(EXTRA_PATH);	
					String file = data.getStringExtra(EXTRA_FILE);
					
					if(checkFile(path, file))
						putPrefs(path, file, -1);	// file loaded in onResume() with restoreData();
					else
						Toast.makeText(this, "error: can't open project", Toast.LENGTH_SHORT).show();
				}
				
				break;
				
			case CODE_SAVEAS:  
				if(resultCode == Activity.RESULT_OK){ 
					String path = data.getStringExtra(EXTRA_PATH);
					String file = data.getStringExtra(EXTRA_FILE);
					
					if(!saveProject(path, file, true))
						putPrefs(path, file, -1);
				}
				
				break; 

			case CODE_RENAME:  
				if(resultCode == Activity.RESULT_OK){ 
					String path = data.getStringExtra(EXTRA_PATH);
					String file = data.getStringExtra(EXTRA_FILE);
					
					if(!renameProject(path, file))
						putPrefs(path, file, -1);
				}
				
				break; 
		} 
	}
	
	private boolean saveProject(String path, String file, boolean okToast)
	{
		boolean error = mGraficPanel.writeFile(this, path + "/" + file);
		
		if(error)
			Toast.makeText(this, "error: couldn't save project", Toast.LENGTH_SHORT).show();
		else
			if(okToast)
				Toast.makeText(this, "saving project ok", Toast.LENGTH_SHORT).show();
		
		return error;
	}
	
	private boolean renameProject(String path, String file)
	{
		boolean error = true;					// assume error
		
	    File from = new File(path, curFile);	// path stays the same
	    File to = new File(path, file);
	    
	     if(from.exists())
	        error = !from.renameTo(to);
		
		if(error)
			Toast.makeText(this, "error: couldn't rename project", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, "renaming project ok", Toast.LENGTH_SHORT).show();
		
		return error;
	}
	
	private void putPrefs(String path, String file, int id)
	{
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();	
		
		if(path != null){
			curPath = path; 
			editor.putString("curPath", curPath);
		}
		
		if(file != null){
			curFile = file;
			editor.putString("curFile", curFile);
		}
		
		if(id != -1)
			editor.putInt("selItemId", id);
		
		editor.commit();		
	}
	
	private boolean checkFile(String path, String file)
	{
		boolean valid = false;
		
		File fh = new File(path, file);
		
		if(!fh.isDirectory() && fh.exists()){
			try{
				FileInputStream fis = new FileInputStream(fh);
				ObjectInputStream ois = new ObjectInputStream(fis);
				
				if(((String)ois.readObject()).equals(GraficPanel.FILEIDENTIER))
					valid = true;
				
				ois.close();			
				fis.close();
			}
			catch(Exception e){
			}
		}
		
		return valid;
	}
	
	public static final int OPENTEXTINPUT = 1;
 
	public static class WeakRefHandler extends Handler 
 	{
		private WeakReference<MainActivity> ref;
		    
		public WeakRefHandler(MainActivity ref)
		{
			this.ref = new WeakReference<MainActivity>(ref);
		}
		    
		@Override
		public void handleMessage(Message msg)
		{
			MainActivity context = ref.get();
			
			if(context == null)
				return;
				
			switch(msg.what){
				case OPENTEXTINPUT:
					textInputAlert(context, (String)msg.obj);
					break;
			}
		}
	}
    
	private static void textInputAlert(final MainActivity context, String text)
	{
	    AlertDialog.Builder alert = new AlertDialog.Builder(context);
	    
	    alert.setTitle("gridraw");
	    alert.setMessage("Enter Text");
	    
	    final EditText input = new EditText(context);
	    input.setText(text);
	    input.setSelectAllOnFocus(true);
	    alert.setView(input);
	    
	    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
	        public void onClick(DialogInterface dialog, int whichButton){
	            Editable eText = input.getText();
	            context.mGraficPanel.setDrawText(eText.toString());
	        }
	    });
	    
	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
	        public void onClick(DialogInterface dialog, int whichButton){
	        }
	    });
	    
	    alert.show();	
	}
}

// sc query intelhaxm
// sc stop intelhaxm

//Toast.makeText(this, String.format("%d", ) , Toast.LENGTH_SHORT).show();
//Toast.makeText(getApplicationContext(), "-> aha", Toast.LENGTH_SHORT).show();

//	private void setActionBarTextColor()
//	{
//		int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
//		TextView actionBarTextView = (TextView)findViewById(actionBarTitleId); 
//		actionBarTextView.setTextColor(Color.BLUE);				
//	}

//--

//getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
//
//getActionBar().setCustomView(mGraficPanel, new ActionBar.LayoutParams(
//								 ActionBar.LayoutParams.WRAP_CONTENT,
//								 ActionBar.LayoutParams.WRAP_CONTENT,
//								 Gravity.CENTER_VERTICAL | Gravity.LEFT));

//getActionBar().hide();
//
//getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);	// dim status bar
//
//View mainscreen = getLayoutInflater().inflate(R.layout.main, null, false);
//ViewGroup.LayoutParams generalLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//addContentView(mainscreen, generalLayoutParam);		

//--

//RelativeLayout rlay = new RelativeLayout(this);				// prepare relative layout
//rlay.setId(1234);
//
//RelativeLayout.LayoutParams param;
//Button btn = null;
//
//param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
//param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);		
//btn = new Button(this);
//btn.setText("Test");
//btn.setId(1000);
//btn.setWidth(100);
//rlay.addView(btn, param);

//setContentView(mGraficPanel);	
//ViewGroup.LayoutParams generalLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//addContentView(rlay, generalLayoutParam);

//--
//
//map.putInt("curTool", mGraficPanel.curTool.ordinal());
//curTool = (Tool.values()[map.getInt("curTool")]);
