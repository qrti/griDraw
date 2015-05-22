// todo
// - zoom and move grid in construction mode?

package de.qland.android.gridraw;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Message;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.util.*;
//import static java.lang.Math.*;

enum Tool
{
	NO_TOOL, 
	POINTER,
	CHANGE_GRID,
	DRAW_LINE,
	DRAW_RECT,
	DRAW_ARC,
	DRAW_FREE,
	DRAW_TEXT,
	DRAW_ROTCENT
};

final class Flags
{
	public static final int NULL 		=	0<<0;
	public static final int SELECTED 	=	1<<0;
	public static final int MOVEOBJECT	=	1<<1;
	public static final int MOVEHANDLE	=	1<<2;
	public static final int ROTATE		=	1<<3;
	public static final int HA_P1		=	1<<4;
	public static final int HA_P3		=	1<<5;
	public static final int HA_P2		=	1<<6;
	public static final int HA_P4		=	1<<7;
	public static final int HA_ALPHA	=	1<<8;
	public static final int HA_BETA		=	1<<9;
	public static final int HA_CENTER	=	1<<10;
	public static final int DELETED		=	1<<11;
	
	public static final int CHANGEMASK 	=	MOVEOBJECT | MOVEHANDLE | ROTATE | HA_P1 | HA_P3 | HA_P2 | HA_P4 | HA_CENTER | HA_ALPHA | HA_BETA;
};

final class Touch
{
	public static final int NONE 		= 	1<<0;		// touch variables
	public static final int TAP 		= 	1<<1;					
	public static final int DRAG 		= 	1<<2;
	public static final int TAP2 		= 	1<<3;
	public static final int ZOOM 		= 	1<<4;
	public static final int AUTO 		= 	1<<5;
}

class rectF 
{
	public float left, top, width, height;
}

class GraficPanel extends SurfaceView implements SurfaceHolder.Callback
{	
	public static final String FILEIDENTIER = "qland.de gridDraw data v0.56";
	
	private final float GBOMM = 5f;						// grip box oversize in mm
	public final float CESI = 4f;						// center size in pixel	
	
	public int touchMode = Touch.NONE;
	public PointF touchStart = new PointF();
	private PointF touchPos = new PointF();
	public PointF touchDrag = new PointF();
	private PointF touchMid = new PointF();	
	private float touchDist;
	private float touchDistS = 1f;
//	private float touchAngle;
	
	private final int DOUBLETAPTIME = 500;				// in ms
	private int clickCount = 0;							// double tap
	private long startTime;
	private boolean doubleTap = false;
	boolean touched = false;
	
	private rectF screen = new rectF();					// screen area
	public rectF draw = new rectF();					// draw	area
	
	private PointF conStart = new PointF();				// construction start
	private PointF conEnd = new PointF();				//				end
	
	public float hs;									// handle size for draw objects
	public float gbo;									// grip box oversize
	
	public float left;									// border draw.left + drawGrid.left
	public float top;									// 		  draw.top + drawGrid.top
	public float gfx;									// grid factor drawGrid.wx / grid.wx
	public float gfy;									// 			   drawGrid.wy / grid.wy	
	
	public Paint conPaint = new Paint();				// construction paint
	public Paint dotsPaint = new Paint();				// dotline paint
	public Paint backPaint = new Paint();				// background paint
	public Paint gridPaint = new Paint();				// grid paint
	public Paint sidePaint = new Paint();				// side paint
	public Paint rocPaint = new Paint();				// rotation center paint
	public Paint textRepPaint = new Paint();			// text represantation paint
	
	public float pmX, pmY;								// display pixel per mm
	public Tool curTool;								// current tool
	
	public Grid grid = new Grid();						// grid	
	public Grid drawGrid = new Grid();					// draw grid
	private Grid tGrid = new Grid();					//  		 temp 
	private float dgLimit;								// 			 move limit
	
	private PlotThread mPlotThread;
	public ArrayList<DrawObject> drawObj = new ArrayList<DrawObject>();	// draw object list
	
	private DrawObject curObj;							// current object
	public DrawObject rocObj;							// rotation center object
	public SideBar sideBar;								// sidebar
		
	public Context mainActivity;
	
	public GraficPanel(Context context) 
	{
	    super(context); 
	    
	    mainActivity = context;
	    
	    conPaint.setStyle(Paint.Style.STROKE);			// construction style
	    conPaint.setColor(Color.rgb(20, 20, 20));		//       		color dark grey	        
	    
	    dotsPaint.setStyle(Paint.Style.STROKE);		// dotline style
	    dotsPaint.setColor(Color.rgb(20, 20, 20));	//         color dark grey	 
	    dotsPaint.setPathEffect(new DashPathEffect(new float[] {10, 20}, 0));
	    
	    backPaint.setStyle(Paint.Style.FILL);			// background style
    	backPaint.setColor(Color.rgb(255, 255, 255));	//            color white

	    gridPaint.setStyle(Paint.Style.STROKE);			// grid style
    	gridPaint.setColor(Color.rgb(190, 190, 255));	//      color light blue
		
	    sidePaint.setStyle(Paint.Style.FILL);			// side style
    	sidePaint.setColor(Color.rgb(190, 190, 190));	//      color light grey
		  	
	    rocPaint.setStyle(Paint.Style.FILL);			// rotation center style
    	rocPaint.setColor(Color.rgb(80, 80, 255));		//      		   color blue 	
    	rocPaint.setAlpha(128);							// 				   alpha
    	
    	textRepPaint.setTextSize(25f);					// text representation size
    	
		Activity activity = (Activity)context;			// calc display density in pixel per mm
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		pmX = pmY = (dm.xdpi + dm.ydpi) / 50.8f ;		// pixel per mm, 2 inch = 50.8 mm
		hs = Math.round(1.2f * pmX);					// handle size half
		
		grid.width = 210f;								// grid size DIN A4
		grid.height = 297f;
		grid.wx = 5f;									// grid width 5 mm
		grid.wy = 5f;
		
		initDrawGrid();									// init drawGrid
		
		sideBar = new SideBar(context, this);			// instance of SideBar
									
		SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mPlotThread = new PlotThread(holder, this);
        
		setFocusable(true);								// set focus to allow touch events	
	}
	
	private void initDrawGrid()
	{
		drawGrid.left = 0f;								// draw grid
		drawGrid.top = 0f;
		drawGrid.width = grid.width * pmX;				
		drawGrid.height = grid.height * pmY;
		drawGrid.wx = grid.wx * pmX;
		drawGrid.wy = grid.wy * pmY;
	}
	
	// called through plotThread
	//
	public void touchHandling()
	{
		if(!sideBar.touched()){
			switch(curTool){							// draw area touch
				case POINTER:
					pointer();
					break;
				case CHANGE_GRID:
					changeGrid();
					break;
				case DRAW_LINE:
					conLine();
					break;
				case DRAW_RECT:
					conRect();
					break;
				case DRAW_ARC:
					conArc();
					break;
				case DRAW_TEXT:
					conText();
					break;
				case DRAW_FREE:
					conFree();
					break;
				default:
					break;
			}
		}
	}		
	
	private void pointer()
	{
		if(sideBar.pointerRotate())
			rotateObject();
		else
			moveObject();
		
		if(doubleTap)
			doubleTap = false;	
	}	
	
	private void conLine()
	{
		if(touchMode==Touch.TAP && !touched){
			touched = true;
			findGridPoint(conStart, touchStart);
			unselectObjects(true);
		}
		else if(touchMode == Touch.DRAG){
			if(curObj == null){
				curObj = new DrawLine(this, conStart);
				curObj.flags |= Flags.SELECTED;
				drawObj.add(curObj);
			}
			
			findGridPoint(conEnd, touchPos);
			((DrawLine)curObj).setEnd(conEnd);
		}
		else if(touchMode==Touch.NONE && curObj!=null){
			curObj = null;
		}
		
		if(doubleTap)
			doubleTap = false;	
	}

	private void conRect()
	{
		if(touchMode==Touch.TAP && !touched){
			touched = true;
			findGridPoint(conStart, touchStart);
			unselectObjects(true);
		}
		else if(touchMode == Touch.DRAG){
			if(curObj == null){
				curObj = new DrawRect(this, conStart);
				curObj.flags |= Flags.SELECTED;
				drawObj.add(curObj);		
			}
			
			findGridPoint(conEnd, touchPos);
			((DrawRect)curObj).setEnd(conEnd);
		}
		else if(touchMode==Touch.NONE && curObj!=null){
			curObj = null;
		}
		
		if(doubleTap)
			doubleTap = false;	
	}
	
	private void conArc()
	{
		if(touchMode==Touch.TAP && !touched){
			touched = true;
			findGridPoint(conStart, touchStart);
			unselectObjects(true);
		}
		else if(touchMode == Touch.DRAG){
			if(curObj == null){
				curObj = new DrawArc(this, conStart);
				curObj.flags |= Flags.SELECTED;
				drawObj.add(curObj);
			}
			findGridPoint(conEnd, touchPos);
			((DrawArc)curObj).setEnd(conEnd);
		}
		else if(touchMode==Touch.NONE && curObj!=null){
			curObj = null;
		}

		if(doubleTap)
			doubleTap = false;
	}

	private void conFree()
	{
		if(touchMode==Touch.TAP && !touched){
			touched = true;
//			findGridPoint(conStart, touchStart);
			conStart.set(touchStart);
			unselectObjects(true);
		}
		else if(touchMode == Touch.DRAG){
			if(curObj == null){
				curObj = new DrawFree(this, conStart);
				curObj.flags |= Flags.SELECTED;
				drawObj.add(curObj);
			}
			
//			findGridPoint(conEnd, touchPos);
			conEnd.set(touchPos);
			((DrawFree)curObj).setNext(conEnd);
		}
		else if(touchMode==Touch.NONE && curObj!=null){
			curObj = null;
		}
		
		if(doubleTap)
			doubleTap = false;		
	}
	
	DrawObject textObj;
	
	private void conText()
	{
		if(touchMode==Touch.TAP && !touched){
			touched = true;
			tp = norm(touchStart);
			curObj = findObject(tp);
			
			Message msg = new Message();
			msg.what = MainActivity.OPENTEXTINPUT;
			
			if(curObj!=null && curObj.type==Tool.DRAW_TEXT){	// edit existing DrawText
				textObj = curObj;
				msg.obj = ((DrawText)curObj).text;
			}
			else{												// new DrawText
				unselectObjects(true);
				msg.obj = "";
			}
			
			((MainActivity)mainActivity).mHandler.sendMessage(msg);
		}
		else if(touchMode == Touch.DRAG){
		}
		else if(touchMode==Touch.NONE && curObj!=null){
			curObj = null;
		}

		if(doubleTap)
			doubleTap = false;
	}

	// called from MainActivity mHandler Alert
	//
	public void setDrawText(String text)
	{
		if(textObj == null){							// new DrawText
			findGridPoint(conStart, touchStart);
			textObj = new DrawText(this, conStart, text);
			textObj.flags |= Flags.SELECTED;
			drawObj.add(textObj);
			textObj = null;
		}
		else{											// update existing DrawText
			if(text.equals(""))
				drawObj.remove(textObj);
			else		
				((DrawText)textObj).updateText(text);
		}
		
		textObj = null;
	}
	
	// called from FeatFunc/PointerRotate/Execute
	//
	public void conRotCent(boolean sel)
	{
		if(sel){					// add rotation center
			PointF cp = new PointF();
			calcCenter(cp);
			rocObj = new DrawRotCent(this, cp);
			rocObj.flags |= Flags.SELECTED;
			drawObj.add(rocObj);
		}
		else{						// remove rotation center
			if(rocObj != null){
//				ArrayList<DrawObject> toRemove = new ArrayList<DrawObject>();	
//				toRemove.add(rocObj);
//				drawObj.removeAll(toRemove);
				drawObj.remove(rocObj);
				rocObj = null;
			}
		}
	}
	
	private void calcCenter(PointF c)
	{
		float x=0, y=0, n=0;
		
		for(DrawObject d : drawObj){
			if((d.flags & Flags.SELECTED) != 0){
				x += (d.pn1.x + d.pn3.x) / 2f;
				y += (d.pn1.y + d.pn3.y) / 2f;
				n++;
			}
		}
			
		if(n == 0){						// center of draw
			PointF sp = new PointF();
			sp.set(draw.left + draw.width / 2, draw.top + draw.height / 2);
			findGridPoint(c, sp);
		}
		else{							// center of selected Objects
			c.set(left + (x / n) * gfx, top + (y / n) * gfy);
		}
	}

	// called from FeatFunc/PointerDelete/Execute
	//
	public void deleteObject(boolean sel)
	{
		if(sel){
			ArrayList<DrawObject> toRemove = new ArrayList<DrawObject>();	
			
			for(DrawObject d : drawObj)
				if((d.flags & Flags.SELECTED)!=0 && d.type!=Tool.DRAW_ROTCENT)
					toRemove.add(d);
			
			drawObj.removeAll(toRemove);
		}
	}
	
	private void changeGrid()
	{
		if(touchMode==Touch.TAP && !touched){
			touched = true;
			tGrid.copy(drawGrid);
		}
		else if(touchMode == Touch.DRAG){
			drawGrid.left = tGrid.left - touchDrag.x;
			drawGrid.top =  tGrid.top - touchDrag.y;
			drawCheckBorder();				
		}
		else if(touchMode == Touch.TAP2){
			tGrid.copy(drawGrid);
		}
		else if(touchMode==Touch.ZOOM && touchDistS>1f){
			float touchScale = touchDist / touchDistS;
			drawGrid.width = tGrid.width * touchScale;
			drawGrid.height = tGrid.height * touchScale;
			
			drawGrid.wx = tGrid.wx * touchScale;
			drawGrid.wy = tGrid.wy * touchScale;
			
			float dx = (touchMid.x - tGrid.left) / tGrid.width;
			float dy = (touchMid.y - tGrid.top) / tGrid.height;
			
			drawGrid.left = tGrid.left + (tGrid.width - drawGrid.width) * dx;
			drawGrid.top = tGrid.top + (tGrid.height - drawGrid.height) * dy;	
			
			drawCheckBorder();						
		}
		
		if(doubleTap)
			doubleTap = false;	
	}
	
	private void drawCheckBorder()
	{		
		if(drawGrid.left > draw.width-dgLimit) 
			drawGrid.left = draw.width - dgLimit;
		else if(drawGrid.left+drawGrid.width < dgLimit) 
			drawGrid.left = dgLimit - drawGrid.width;

		if(drawGrid.top > draw.height-dgLimit) 
			drawGrid.top = draw.height - dgLimit;
		else if(drawGrid.top+drawGrid.height < dgLimit) 
			drawGrid.top = dgLimit - drawGrid.height;		
			
		gfx = drawGrid.wx / grid.wx;			// grid factor
		gfy = drawGrid.wy / grid.wy;
		
		left = draw.left + drawGrid.left;		// border
		top = draw.top + drawGrid.top;
		
		gbo = GBOMM * pmX / gfx;				// grid box oversize in pixel
	}
	
	private void findGridPoint(PointF gdp, PointF scp)
	{
		float nx = Math.round((scp.x - left) / drawGrid.wx);
		float ny = Math.round((scp.y - top) / drawGrid.wy);
		gdp.set(left + nx * drawGrid.wx, top + ny * drawGrid.wy);
	}

	public DrawObject findObject(PointF tp)
	{
		float min = Float.MAX_VALUE;
		DrawObject found = null;	

		for(DrawObject d : drawObj){
			if(d.flags != Flags.DELETED){
				float dist = d.touchDist(tp);
				
				if(dist<gbo && ((d.flags & Flags.SELECTED)!=0 || d==rocObj)){
					found = d;		// stay on selected object and prefere rotation center
					break;
				}
				
				if(dist < min){
					found = d;
					min = dist;
				}
			}
		}
		
		return found;
	}

	private PointF mDrag = new PointF();
	private PointF tp;
	private boolean init;
	
	private void moveObject()
	{
		if(touchMode==Touch.TAP && !touched){
			touched = true;
			tp = norm(touchStart);
			curObj = findObject(tp);
			
			if(sideBar.pointerMultisel()){		// multisel on
				if(curObj != null)
					curObj.flags ^= Flags.SELECTED;
				else
					unselectObjects(false);
			}
			else{								// multisel off
				unselectObjects(false);
				
				if(curObj != null){
					curObj.changeInitMove(tp);
					curObj.flags |= Flags.SELECTED;
				}
			}
		}
		else if(touchMode==Touch.DRAG && curObj!=null){
			if(sideBar.pointerMultisel()){
				if(!init){
					curObj.changeInitMove(tp);
					curObj.flags |= Flags.SELECTED;
				}
				
				curObj.changeObjectMove(touchDrag);
				mDrag.set(curObj.pn1o.x-curObj.pn1.x, curObj.pn1o.y-curObj.pn1.y);
				
				for(DrawObject d : drawObj)
					if((d.flags & Flags.SELECTED)!=0 && d!=curObj)
						d.changeObjectMoveMulti(mDrag, init);
				
				init = true;
			}
			else{
				curObj.changeObjectMove(touchDrag);
			}
		}
		else if(touchMode == Touch.NONE){
			curObj = null;
			init = false;
		}
	}
	
	private float angle0;
	
	private void rotateObject()
	{
		if(touchMode==Touch.TAP && !touched){
			touched = true;
			tp = norm(touchStart);
			DrawObject found = findObject(tp);
			
			if(found == rocObj){
				rocObj.changeInitMove(tp);
				rocObj.flags |= Flags.SELECTED;
			}
			else{
				rocObj.flags &= ~Flags.SELECTED;
				
				if(found != null)
					found.flags ^= Flags.SELECTED;
			}
		}
		else if(touchMode == Touch.DRAG){
			if((rocObj.flags & Flags.SELECTED) != 0){
				rocObj.changeObjectMove(touchDrag);
			}
			else{
				if(!init){
					angle0 = calcAngle(rocObj.pn1, tp);
					
					for(DrawObject d : drawObj)
						if((d.flags & Flags.SELECTED) != 0)
							d.changeInitRotate(rocObj.pn1);
					
					init = true;
				}
				
				float angle = snapAngle(calcAngle(rocObj.pn1, norm(touchPos)) - angle0);
				
				for(DrawObject d : drawObj)
					if((d.flags & Flags.SELECTED) != 0)
						d.changeObjectRotate(angle);
			}
		}
		else if(touchMode == Touch.NONE){
			init = false;
		}
	}

	private void unselectObjects(boolean cuob)
	{
		for(DrawObject d : drawObj)
			d.flags &= ~Flags.SELECTED;
		
		if(cuob)
			curObj = null;
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
	
//------------------------------------------------------------------------------
		
	/** Touch Event handler
	 * 
	 * 			+90			touch angle
	 * 		 +1		+179
	 * 		0		 +180
	 * 		 -1		-179
	 * 			-90
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch(event.getAction() & MotionEvent.ACTION_MASK){	// handle touch events
	        case MotionEvent.ACTION_DOWN:
	        	if(System.currentTimeMillis()-startTime > DOUBLETAPTIME){ 
					startTime = System.currentTimeMillis();
					clickCount = 0;
				}
				
            	clickCount++;
				touchPos.set(event.getX(), event.getY());
		        touchStart.set(event.getX(), event.getY());
		        touchDrag.set(0, 0);
		        touchMode = Touch.TAP;
		        break;      

            case MotionEvent.ACTION_POINTER_DOWN:
            	touchMode = Touch.TAP2;
                break;		        

            case MotionEvent.ACTION_MOVE:
                if(touchMode==Touch.DRAG || touchMode==Touch.TAP){
                	touchPos.set(event.getX(), event.getY());
                	touchDrag.set(touchStart.x-event.getX(), touchStart.y-event.getY());
                	touchMode = Touch.DRAG;
                }
                else if(touchMode == Touch.TAP2){
                	touchDistS = calcDist(event);
                    
                	if(touchDistS > 10f){
                		midPoint(touchMid, event);
                		touchDist = calcDist(event);
                		touchMode = Touch.ZOOM;
                	}       
                }
                else if(touchMode == Touch.ZOOM){
                	// touchAngle = calcAngle(event);
	                touchDist = calcDist(event);
                }

                break;

	        case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
	           	if(clickCount==2 && System.currentTimeMillis()-startTime<DOUBLETAPTIME){
	           		doubleTap = true;
					clickCount = 0;
					startTime = 0;
                }
	        
		        touchMode = Touch.NONE;
		        touched = false;
		        break;
		}

	    return true;
	}	
	
	private float calcDist(MotionEvent event) 
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x*x + y*y);
	}

	private void midPoint(PointF point, MotionEvent event) 
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x/2, y/2);
	}		
	
	@SuppressWarnings("unused")
	private final float PI = (float)(Math.PI);
    private final float PI180 = (float)(Math.PI / 180d);
    
	/**
	 * calculate angle between two points
	 * @param p0 point1 norm
	 * @param p1 point2 norm
	 * @return angle rad 0..PI -PI..0 (0..180 -179..-1)
	 */
	private float calcAngle(PointF p0, PointF p1) 
	{
		return (float)Math.atan2(p1.y-p0.y, p1.x-p0.x);
	}											

	private float snapAngle(float angle)
	{
		float a = 5 * PI180;
		return Math.round(angle / a) * a;
	}
	
    @SuppressWarnings("unused")
	private PointF pixel(PointF n)
    {
    	PointF p = new PointF();
    	p.set(left + n.x * gfx, top + n.y * gfy);
    	return p;
    }
  
	@SuppressWarnings("unused")
	private float pixelx(float nx)
	{
		return left + nx * gfx;
	}
	
	@SuppressWarnings("unused")
	private float pixely(float ny)
	{
		return top + ny * gfy;
	}
	
   private PointF norm(PointF p)
    {
    	PointF n = new PointF();
    	n.set((p.x - left) / gfx, (p.y - top) / gfy);
    	return n;
    }
	   
	@SuppressWarnings("unused")
	private float normx(float px)
	{
		return (px - left) / gfx;
	}
	
	@SuppressWarnings("unused")
	private float normy(float py)
	{
		return (py - top) / gfy;
	}
	
//------------------------------------------------------------------------------
	
	public void setTool(Tool a)
	{
		curTool = a;
		sideBar.setTool(a);
	}	
    
    public void cleanPanel()
    {
		drawObj.clear();
		resetRoatate();
		initDrawGrid();
		drawCheckBorder();
    }
    
    private void resetRoatate()
    {
		sideBar.pointerRotateUnsel();
		
		if(rocObj != null){
			drawObj.remove(rocObj);
			rocObj = null;
		}
    }
    
	public boolean writeFile(Context context, String pafi)
	{
		boolean error = false;
		resetRoatate();
		
		try{
			File file = new File(pafi);						
			FileOutputStream  fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(FILEIDENTIER);
			
			for(ArrayList<Feature> tool : sideBar.toolCol)	// write sidebar settings
				for(Feature f : tool)
					f.write(oos);		
			
			grid.writeObject(oos);							//		 grid
			drawGrid.writeObject(oos);						// 		 draw grid
			
			int num = 0;									// 		 number of draw objects
			
			for(DrawObject d : drawObj)						//		 draw objects
				if(d.flags != Flags.DELETED)
					num++;
			
			oos.writeInt(num);								
			
			for(DrawObject d : drawObj)						//		 draw objects
				if(d.flags != Flags.DELETED)
					d.writePars(oos);

			oos.close();	 
			fos.flush();
			fos.close();
		}
		catch(Exception e){
			e.printStackTrace();
			error = true;
		}
		
		return error;
	}

	public boolean readFile(Context context, String pafi)
	{
		drawCheckBorder();		// to update gfx and gfy for DrawObject/DrawText constructor
		
		File file = new File(pafi);	
		
		if(!file.exists())
			return true;
		
		boolean error = false;
		
		try{
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			
			if(!((String)ois.readObject()).equals(FILEIDENTIER)){
				ois.close();			
				fis.close();
				return true;
			}
			
			for(ArrayList<Feature> tool : sideBar.toolCol)	// read sidebar settings
				for(Feature f : tool)
					f.read(ois);
			
			grid.readObject(ois); 							// 		grid
			drawGrid.readObject(ois); 						//      draw grid
			
			int size = ois.readInt();						// 		number of draw objects
			DrawObject d;									
			
			for(int i=0; i<size; i++){						//		draw objects		
				Tool type = Tool.values()[ois.readInt()];
				d = null;
				
				switch(type){
					case DRAW_LINE:
						d = new DrawLine(this, ois);
						break;
					case DRAW_RECT:
						d = new DrawRect(this, ois);
						break;
					case DRAW_ARC:
						d = new DrawArc(this, ois);
						break;
					case DRAW_FREE:
						d = new DrawFree(this, ois);
						break;
					case DRAW_TEXT:
						d = new DrawText(this, ois);
						break;
					default:
						break;
				}
				
				if(d != null)
					drawObj.add(d);				
			}

			ois.close();			
			fis.close();
		}
		catch(Exception e){
			e.printStackTrace();
			error = true;
		}
		
		return error;	
	}
	
//------------------------------------------------------------------------------

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{		
 		mPlotThread.setRunning(true);
		mPlotThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int s_width, int s_height)
	{		
		screen.width = s_width;
		screen.height = s_height;

		draw.left = 0f;
		draw.top = 0f;
		draw.width = screen.width - draw.left;
		draw.height = screen.height - draw.top;
		
		dgLimit = (screen.width + screen.height) / 20;
		drawCheckBorder();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{				
		boolean retry = true;
		mPlotThread.setRunning(false);

		while(retry){
			try{
				mPlotThread.join();
				retry = false;
			}
			catch(InterruptedException e){
			}
		}
	}
}

//Log.w(null, "text ...");
//Toast.makeText(getContext(), "text ...", Toast.LENGTH_SHORT).show();

// grid drag bounds
//
//	if(gridLeft < -gridWidth+gridRim)
// 		gridLeft = -gridWidth+gridRim;
//					
//	if(gridLeft > drawLeft+drawWidth-gridRim)
//		gridLeft = drawLeft+drawWidth-gridRim;	
//					
//	if(gridTop < -gridHeight+gridRim)
//		gridTop = -gridHeight+gridRim;
//				
//	if(gridTop > drawTop+drawHeight-gridRim)
//		gridTop = drawTop+drawHeight-gridRim;
	
//	//float touchSc = (touchDistS - touchDist) / (Math.abs(touchDistS) + Math.abs(touchDist));
//	//float touchScale = touchDist / touchDistS;
//
//
//	drawGrid.wx = tGrid.wx - tGrid.wx * touchSc;
//	drawGrid.wy = tGrid.wy - tGrid.wy * touchSc;
//
//	drawGrid.width = tGrid.width - tGrid.width * touchSc;
//	drawGrid.height = tGrid.height - tGrid.height * touchSc;
//
//	float dx = drawGrid.left - touchMid.x;
//	float dy = drawGrid.top - touchMid.y;
//
////						drawGrid.left = tGrid.left + (tGrid.width - drawGrid.width) / 2;
////						drawGrid.top = tGrid.top + (tGrid.height - drawGrid.height) / 2;
//
//	drawGrid.left = tGrid.left - dx*touchSc;
//	drawGrid.top = tGrid.top - dy*touchSc;
//
//	drawCheckBorder();

