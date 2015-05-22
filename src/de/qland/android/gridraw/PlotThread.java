package de.qland.android.gridraw;

import android.graphics.Canvas;
import android.graphics.Region;
import android.view.SurfaceHolder;

class PlotThread extends Thread 
{
    private SurfaceHolder mSurfaceHolder;
    private boolean  mRun = false;  
    private GraficPanel panel;
    private SideBar sideBar;
    private Grid grid;
    private rectF draw;
    
    public PlotThread(SurfaceHolder surfaceHolder, GraficPanel graficPanel){
        mSurfaceHolder = surfaceHolder;
        panel = graficPanel;
        sideBar = panel.sideBar;
    	grid = panel.drawGrid;
    	draw = panel.draw;
    }
 
    public void setRunning(boolean run) 
    {
    	synchronized(mSurfaceHolder){
    		mRun = run;
    	}
    }
    
    @Override
    public void run() 
    {
        while(mRun){
            Canvas c = null;
            
            try{
                c = mSurfaceHolder.lockCanvas(null);
                
                synchronized(mSurfaceHolder){
                	if(c != null)
                		doDraw(c);
                }
            } 
            finally{
                if(c != null)
                    mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }
        
    public void doDraw(Canvas canvas) 
    {
    	canvas.drawColor(panel.backPaint.getColor());	// clear screen with background color
		panel.touchHandling();
    	
    	if(draw.left > 0){								// sidebar area
    		canvas.drawRect(0f, 0f, draw.left, draw.top+draw.height, panel.sidePaint);
  
			for(Feature f : sideBar.curFeats)			// sidebar features
				canvas.drawBitmap(f.selected ? f.bmSel : f.bmUnsel, draw.left-f.pos.x, f.pos.y, null);
    	}
    	
		canvas.clipRect(draw.left, draw.top, draw.left+draw.width, draw.top+draw.height, Region.Op.REPLACE);
		
		float gsx = draw.left + (grid.left<0 ? grid.left%grid.wx : grid.left);
		float gex = draw.left + (grid.left+grid.width>draw.left+draw.width ? draw.left+draw.width : grid.left+grid.width);
					
		float gsy = draw.top + (grid.top<0 ? grid.top%grid.wy : grid.top);
		float gey = draw.top + (grid.top+grid.height>draw.top+draw.height ? draw.top+draw.height : grid.top+grid.height);
		
		for(float x=gsx; x<gex+0.1f; x+=grid.wx)		// grid x-lines
			canvas.drawLine(x, gsy, x, gey, panel.gridPaint);	
		
		for(float y=+gsy; y<gey+0.1f; y+=grid.wy)		// grid y-lines
			canvas.drawLine(gsx, y, gex, y, panel.gridPaint);		
		
		for(DrawObject d : panel.drawObj)				// draw objects
			if(d.flags != Flags.DELETED)
				d.draw(canvas);
    }	
}	

//Iterator<DrawObject> ite = panel.drawObj.iterator();
//
//while(ite.hasNext()){
//	DrawObject d = ite.next();
//	
//	if(d.flags != Flags.DELETED)
//		d.draw(canvas);











