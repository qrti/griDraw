// todo
// - DrawArc, resize with handles and fixed opposite edges on rotated Arcs
// - update text while keep scale and adapted boxwidth
//   similar handling like changeHandle (careful with roated text)

package de.qland.android.gridraw;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public abstract class DrawObject
{
	static final float PI = (float)Math.PI;
	static final float PI180 = (float)(Math.PI / 180d);
	
	Tool type;
	int flags;
	GraficPanel panel;
	
	PointF pn1 = new PointF();
	PointF pn2 = new PointF();
	PointF pn3 = new PointF();
	PointF pn4 = new PointF();
	
	PointF pn1o = new PointF();
	PointF pn2o = new PointF();
	PointF pn3o = new PointF();
	PointF pn4o = new PointF();
	
	DrawObject(GraficPanel p, Tool t, PointF sp)
	{
		panel = p;
		type = t;
		normPoint(pn1, sp);
		pn3.set(pn1);
	}
	
	DrawObject(GraficPanel p, Tool t, ObjectInputStream ois) throws IOException
	{
		panel = p;
		type = t;
		readParsC(ois);
	}
	
	public abstract void draw(Canvas canvas);
	public abstract float touchDist(PointF p);
	public abstract void changeInitMove(PointF touch);
	public abstract void changeObjectMove(PointF drag);
	public abstract void changeObjectMoveMulti(PointF drag, boolean init);
	public abstract void changeInitRotate(PointF center);
	public abstract void changeObjectRotate(float angle);
	public abstract void writePars(ObjectOutputStream oos) throws IOException;

	/**
	 * normalize source in pixel to target in mm 
	 * @param tp target
	 * @param sp source
	 */
	protected void normPoint(PointF tp, PointF sp)			
	{
		float x = (sp.x - panel.left) / panel.gfx;
		float y = (sp.y - panel.top) / panel.gfy;
		tp.set(x, y);
	}
	
	/**
	 * distance point to point
	 * @param p1 point1
	 * @param p2 point2
	 * @return distance
	 */
	protected float distPoint(PointF p1, PointF p2)
	{
		float a = p2.x - p1.x;
		float b = p2.y - p1.y;
		return (float)Math.sqrt(a*a + b*b); 
	}

	/**
	 * distance point to point
	 * @param p1x point1 x
	 * @param p1y point1 y
	 * @param p2 point2
	 * @return distance
	 */
	protected float distPoint(float p1x, float p1y, PointF p2)
	{
		float a = p2.x - p1x;
		float b = p2.y - p1y;
		return (float)Math.sqrt(a*a + b*b); 
	}
	
	/**
	 * distance point to point
	 * @param p1x point1 x
	 * @param p1y point1 y
	 * @param p2x point2 x
	 * @param p2y point2 y
	 * @return distance
	 */
	protected float distPoint(float p1x, float p1y, float p2x, float p2y)
	{
		float a = p2x - p1x;
		float b = p2y - p1y;
		return (float)Math.sqrt(a*a + b*b); 
	}
	
	/**
	 * distance point to line
	 * @param s lineStart
	 * @param e lineEnd
	 * @param p testPoint
	 */
	protected float distLine(PointF s, PointF e, PointF p)
	{
		float d = Float.MAX_VALUE;
		float a = s.y - e.y;
		float b = s.x - e.x;

		if(a!=0f || b!=0f){
			float c = s.x*e.y - s.y*e.x;
			d = Math.abs(a*p.x - b*p.y + c) / (float)Math.sqrt(a*a + b*b);
		}
		else{
			d = distPoint(s.x, s.y, p);  	// line is point, s = e
		}
		
		return d;
	}
	
	/**
	 * distance point to line
	 * @param sx lineStart x
	 * @param sy lineStart y
	 * @param ex lineEnd x 
	 * @param ey lineEnd y 
	 * @param p testPoint 
	 * @return distance 
	 */
	protected float distLine(float sx, float sy, float ex, float ey, PointF p)
	{
		float d = Float.MAX_VALUE;
		float a = sy - ey;
		float b = sx - ex;

		if(a!=0f || b!=0){
			float c = sx*ey - sy*ex;
			d = Math.abs(a*p.x - b*p.y + c) / (float)Math.sqrt(a*a + b*b);
		}
		else{
			d = distPoint(sx, sy, p);		// line is point, s = e
		}
		
		return d;
	}
	
	/**
	 * pixelize point
	 * @param x norm
	 * @return pixel
	 */
	protected float pixel_x(float x)
	{
		return panel.left + x * panel.gfx;
	}
	
	/**
	 * pixelize point
	 * @param y norm
	 * @return pixel
	 */
	protected float pixel_y(float y)
	{
		return panel.top + y * panel.gfy;
	}	
    
	/**
	 * calculate angle between two points
	 * @param p0 point1
	 * @param p1 point2
	 * @return angle rad 0..PI -PI..0 (0..180 -179..-1)
	 */
	public float calcAngle(PointF p0, PointF p1) 
	{
		return (float)Math.atan2(p1.y-p0.y, p1.x-p0.x);
	}		
	
	public float calcAngle(float p0x, float p0y, PointF p1) 
	{
		return (float)Math.atan2(p1.y-p0y, p1.x-p0x);
	}	
	
	public float calcAngle(float p0x, float p0y, float p1x, float p1y) 
	{
		return (float)Math.atan2(p1y-p0y, p1x-p0x);
	}	
	
	// serialize common parameters
	//
	protected void writeParsC(ObjectOutputStream oos) throws IOException
	{
		oos.writeInt(type.ordinal());
		oos.writeFloat(pn1.x);
		oos.writeFloat(pn1.y);
		oos.writeFloat(pn3.x);
		oos.writeFloat(pn3.y);
	}
	
	protected void readParsC(ObjectInputStream ois) throws IOException
	{
		pn1.x = ois.readFloat();
		pn1.y = ois.readFloat();
		pn3.x = ois.readFloat();
		pn3.y = ois.readFloat();
	}
}

class DrawLine extends DrawObject
{		
	PointF rotcen = new PointF();
	float angle_pn1;
	float angle_pn3;
	float radius_pn1;
	float radius_pn3;	
	
	DrawLine(GraficPanel p, PointF sp)
	{
		super(p, Tool.DRAW_LINE, sp);
	}	

	DrawLine(GraficPanel p, ObjectInputStream ois) throws IOException
	{
		super(p, Tool.DRAW_LINE, ois);
	}
	
	@Override
	public void draw(Canvas canvas)
	{
		float hs = panel.hs;
		float p1x = pixel_x(pn1.x);
		float p1y = pixel_y(pn1.y);
		float p3x = pixel_x(pn3.x);
		float p3y = pixel_y(pn3.y);
		
		if(Math.abs(p1x-p3x)>hs || Math.abs(p1y-p3y)>hs){					// object
			canvas.drawLine(p1x, p1y, p3x, p3y, panel.conPaint);
		}																	
		else{																// representation
			float x = (p1x + p3x) / 2;
			float y = (p1y + p3y) / 2;
			float qs = hs / 2;
			canvas.drawLine(x-hs, y-hs, x+hs, y+hs, panel.conPaint);		// cross
			canvas.drawLine(x-hs, y+hs, x+hs, y-hs, panel.conPaint);
			canvas.drawLine(x-qs, y, x+qs, y, panel.conPaint);				// line
		}
	
		if((flags & Flags.SELECTED) != 0){									// handles
			canvas.drawRect(p1x-hs, p1y-hs, p1x+hs, p1y+hs, panel.conPaint);
			canvas.drawRect(p3x-hs, p3y-hs, p3x+hs, p3y+hs, panel.conPaint);			
		}
	}	
	
	@Override
	public float touchDist(PointF p)
	{	
		float dist = Float.MAX_VALUE;

		if(p.x<Math.max(pn1.x, pn3.x)+panel.gbo && p.x>Math.min(pn1.x, pn3.x)-panel.gbo &&
		   p.y<Math.max(pn1.y, pn3.y)+panel.gbo && p.y>Math.min(pn1.y, pn3.y)-panel.gbo){

			float d = distLine(pn1, pn3, p);
			
			if(d < panel.gbo)
				dist = d;
		}
		
		return dist;	
	}
	
	@Override
	public void changeInitMove(PointF p)
	{
		pn1o.set(pn1);								// save original po1 and po3 points
		pn3o.set(pn3);

		float p1 = distPoint(pn1, p);				// calc distance to handles
		float p3 = distPoint(pn3, p);
		
		boolean mo = panel.sideBar.pointerMoveO();	// get move/rotate/multisel flags
		boolean mh = panel.sideBar.pointerMoveH();
		boolean ms = panel.sideBar.pointerMultisel();
		
		flags &= ~Flags.CHANGEMASK;					// reset and set change flags
		float g = panel.gbo;
		
		if(mo || ms || (!mh && p1>g && p3>g) || (!mh && p1<g && p3<g))
			flags |= Flags.MOVEOBJECT;
		else if(p1<g || p3<g)
			flags |= Flags.MOVEHANDLE;
		
		flags |= p1<p3 ? Flags.HA_P1 : Flags.HA_P3;				
	}
	
	@Override
	public void changeObjectMove(PointF drag)
	{			
		if((flags & Flags.MOVEOBJECT) != 0){
			if((flags & Flags.HA_P1) !=0)
				moveObject(pn1, pn3, pn1o, drag);
			else if((flags & Flags.HA_P3) != 0)
				moveObject(pn3, pn1, pn3o, drag);
		}
		else if((flags & Flags.MOVEHANDLE) != 0){
			if((flags & Flags.HA_P1) != 0)
				moveHandle(pn1, pn1o, drag);
			else if((flags & Flags.HA_P3) != 0)
				moveHandle(pn3, pn3o, drag);
		}
	}	
	
	@Override
	public void changeObjectMoveMulti(PointF drag, boolean init)
	{
		if(!init){
			pn1o.set(pn1);						// save original po1 and po3 points
			pn3o.set(pn3);
		}
		
		pn1.set(pn1o.x - drag.x, pn1o.y - drag.y);
		pn3.set(pn3o.x - drag.x, pn3o.y - drag.y);
	}
	
	@Override
	public void changeInitRotate(PointF c)
	{
		rotcen.set(c);							// save center
		
		angle_pn1 = calcAngle(rotcen, pn1);		// calc angle
		angle_pn3 = calcAngle(rotcen, pn3);
		radius_pn1 = distPoint(rotcen, pn1);	// calc radius
		radius_pn3 = distPoint(rotcen, pn3);
		
		flags &= ~Flags.CHANGEMASK;				// reset and change flags
		flags |= Flags.ROTATE;					// set rotate flag
	}
	
	@Override
	public void changeObjectRotate(float angle)
	{
		pn1.x = rotcen.x + (float)(radius_pn1 * Math.cos(angle_pn1 + angle));
		pn1.y = rotcen.y + (float)(radius_pn1 * Math.sin(angle_pn1 + angle));
		pn3.x = rotcen.x + (float)(radius_pn3 * Math.cos(angle_pn3 + angle));
		pn3.y = rotcen.y + (float)(radius_pn3 * Math.sin(angle_pn3 + angle));
	}
	
	public void setEnd(PointF ep)
	{
		normPoint(pn3, ep);
	}

	/**
	 * move handle line, circle
	 * @param tp target
	 * @param sp source
	 * @param op offset screen
	 */
	private void moveHandle(PointF tp, PointF sp, PointF op)	
	{
		float nx = Math.round((sp.x - op.x / panel.gfx) / panel.grid.wx);
		float ny = Math.round((sp.y - op.y / panel.gfy) / panel.grid.wy);			
		tp.set(nx * panel.grid.wx, ny * panel.grid.wy);
	}
	
	/**
	 * move object line, circle
	 * @param tpg target grid
	 * @param tpn target nongrid
	 * @param sp source
	 * @param op offset screen
	 */
	private void moveObject(PointF tpg, PointF tpn, PointF sp, PointF op)	
	{
		float	x = sp.x - op.x / panel.gfx;
		float	y = sp.y - op.y / panel.gfy;
		x = Math.round(x / panel.grid.wx) * panel.grid.wx;
		y = Math.round(y / panel.grid.wy) * panel.grid.wy;	
		tpn.set(tpn.x-tpg.x+x, tpn.y-tpg.y+y);
		tpg.set(x, y);
	}
	
	@Override
	public void writePars(ObjectOutputStream oos) throws IOException
	{		
		writeParsC(oos);
	}
}

class DrawRect extends DrawObject
{
	PointF center = new PointF();	// center
	float radius;					// radius of surrounding circle
	float alpha;					// angle of point 1/3 of unrotated rectangle
	float beta;						// angle of rotation
	
	PointF centero = new PointF();	// save values
	float betao;
	
	PointF rotcen = new PointF();	// rotation center
	float distro;					// rotation center to object center distance
	float anglro;					// 									angle 
	
	DrawRect(GraficPanel p, PointF sp)
	{
		super(p, Tool.DRAW_RECT, sp);
		updateRect();
		centero.set(center);		// for center orientated construction
	}	

	DrawRect(GraficPanel p, ObjectInputStream ois) throws IOException
	{
		super(p, Tool.DRAW_RECT, ois);
		beta = ois.readFloat();
		updateRect();
	}
	
	@Override
	public void draw(Canvas canvas)
	{
		float hs = panel.hs;
		
		float p1x = pixel_x(pn1.x);
		float p1y = pixel_y(pn1.y);
		float p2x = pixel_x(pn2.x);
		float p2y = pixel_y(pn2.y);
		float p3x = pixel_x(pn3.x);		
		float p3y = pixel_y(pn3.y);		
		float p4x = pixel_x(pn4.x);		
		float p4y = pixel_y(pn4.y);		
		
		if(Math.abs(p1x-p3x)>hs || Math.abs(p1y-p3y)>hs){						// object
			canvas.drawLine(p1x, p1y, p2x, p2y, panel.conPaint);				// because of issues in drawRect
			canvas.drawLine(p2x, p2y, p3x, p3y, panel.conPaint);				// drawing is done with drawLine
			canvas.drawLine(p3x, p3y, p4x, p4y, panel.conPaint);				// issue 1, 2^15-1 clipping
			canvas.drawLine(p4x, p4y, p1x, p1y, panel.conPaint);				// issue 2, point order
		}
		else{																	// representation
			float x = (p1x + p3x) / 2;
			float y = (p1y + p3y) / 2;
			float qs = hs / 2;
			canvas.drawLine(x-hs, y-hs, x+hs, y+hs, panel.conPaint);			// cross
			canvas.drawLine(x-hs, y+hs, x+hs, y-hs, panel.conPaint);
			canvas.drawRect(x-qs, y-qs, x+qs, y+qs, panel.conPaint);			// rect
		}
		
		if((flags & Flags.SELECTED) != 0){										// handles
			canvas.drawRect(p1x-hs, p1y-hs, p1x+hs, p1y+hs, panel.conPaint);	// edges
			canvas.drawRect(p2x-hs, p2y-hs, p2x+hs, p2y+hs, panel.conPaint);
			canvas.drawRect(p3x-hs, p3y-hs, p3x+hs, p3y+hs, panel.conPaint);
			canvas.drawRect(p4x-hs, p4y-hs, p4x+hs, p4y+hs, panel.conPaint);
	
			float cx = (p1x + p3x) / 2f;										// center
			float cy = (p1y + p3y) / 2f;
			canvas.drawLine(cx, cy-panel.CESI, cx, cy+panel.CESI, panel.conPaint);
			canvas.drawLine(cx-panel.CESI, cy, cx+panel.CESI, cy, panel.conPaint);
		}
	}
	
	@Override
	public float touchDist(PointF p)
	{
		float gbo = panel.gbo;
		float dist = Float.MAX_VALUE;
		
		float xmin = Math.min(pn1.x, pn2.x);
		xmin = Math.min(xmin, pn3.x);
		xmin = Math.min(xmin, pn4.x);
		
		float ymin = Math.min(pn1.y, pn2.y);
		ymin = Math.min(ymin, pn3.y);
		ymin = Math.min(ymin, pn4.y);
		
		float xmax = Math.max(pn1.x, pn2.x);
		xmax = Math.max(xmax, pn3.x);
		xmax = Math.max(xmax, pn4.x);
		
		float ymax = Math.max(pn1.y, pn2.y);
		ymax = Math.max(ymax, pn3.y);
		ymax = Math.max(ymax, pn4.y);		
		
		if(p.x<xmax+gbo && p.x>xmin-gbo && p.y<ymax+gbo && p.y>ymin-gbo){
			float dc = Float.MAX_VALUE;
			
			float d0 = distLine(pn1, pn2, p);		
			float d1 = distLine(pn2, pn3, p);
			float d2 = distLine(pn3, pn4, p);
			float d3 = distLine(pn4, pn1, p);
			
			if((flags & Flags.SELECTED) != 0)
				dc = distPoint(center, p);
			
			if(d0<gbo || d1<gbo || d2<gbo || d3<gbo || dc<gbo){
				dist = Math.min(d0, dc);
				dist = Math.min(dist, d1);
				dist = Math.min(dist, d2);
				dist = Math.min(dist, d3);
			}
		}
		
		return dist;	
	}
	
	@Override
	public void changeInitMove(PointF p)
	{
		centero.set(center);						// save current parameters
		pn1o.set(pn1);
		pn2o.set(pn2);
		pn3o.set(pn3);
		pn4o.set(pn4);
		
		float d1 = distPoint(pn1, p);				// calc distance to handles
		float d2 = distPoint(pn2, p);
		float d3 = distPoint(pn3, p);
		float d4 = distPoint(pn4, p);
		float dc = distPoint(center, p);			// calc distance to center
					
		boolean mo = panel.sideBar.pointerMoveO();	// get move flags
		boolean mh = panel.sideBar.pointerMoveH();
		boolean ms = panel.sideBar.pointerMultisel();
		
		flags &= ~Flags.CHANGEMASK;					// reset and set change flags
		float g = panel.gbo;
		
		if(mo || ms || (!mh && d1>g && d3>g && d2>g && d4>g) || (!mh && d1<g && d3<g && d2<g && d4<g))
			flags |= Flags.MOVEOBJECT;
		else if(d1<g || d3<g || d2<g || d4<g)
			flags |= Flags.MOVEHANDLE;
			
		if(d2<d1 && d2<d3 && d2<d4 && d2<dc)
			flags |= Flags.HA_P2;
		else if(d3<d1 && d3<d2 && d3<d4 && d3<dc)
			flags |= Flags.HA_P3;
		else if(d4<d1 && d4<d2 && d4<d3 && d4<dc)
			flags |= Flags.HA_P4;
		else if(dc<d1 && dc<d2 && dc<d3 && dc<d4)
			flags |= Flags.HA_CENTER;
		else
			flags |= Flags.HA_P1;
	}

	@Override
	public void changeObjectMove(PointF drag)
	{			
		if((flags & Flags.MOVEOBJECT) != 0){
			if((flags & Flags.HA_CENTER) != 0)
				moveObject(centero, drag);
			else if((flags & Flags.HA_P1) != 0)
				moveObject(pn1o, drag);
			else if((flags & Flags.HA_P2) != 0)
				moveObject(pn2o, drag);	
			else if((flags & Flags.HA_P3) != 0)
				moveObject(pn3o, drag);
			else
				moveObject(pn4o, drag);
		}
		else if((flags & Flags.MOVEHANDLE) != 0){
			if((flags & Flags.HA_P1) != 0)
				moveHandle(pn1o, pn3o, drag, 0);
			else if((flags & Flags.HA_P2) != 0)
				moveHandle(pn2o, pn4o, drag, -PI-1);
			else if((flags & Flags.HA_P3) != 0)
				moveHandle(pn3o, pn1o, drag, PI);
			else
				moveHandle(pn4o, pn2o, drag, -1);
		}
	}
	
	@Override
	public void changeObjectMoveMulti(PointF drag, boolean init)
	{
		if(!init){
			pn1o.set(pn1);					// save current parameters
			pn3o.set(pn3);
		}
		
		pn1.set(pn1o.x - drag.x, pn1o.y - drag.y);
		pn3.set(pn3o.x - drag.x, pn3o.y - drag.y);
		updateRect();
	}
	
	@Override
	public void changeInitRotate(PointF rc)
	{
		betao = beta;						// save current rotation angle				
		rotcen.set(rc);						// set rotation center
		
		distro = distPoint(rc, center);		// rotation center to rect center distance
		anglro = calcAngle(rc, center);		// 								  angle 
		
		flags &= ~Flags.CHANGEMASK;			// reset and change flags
		flags |= Flags.ROTATE;				// set rotate flag
	}
	
	@Override
	public void changeObjectRotate(float a)
	{
		center.x = rotcen.x + (float)(distro * Math.cos(anglro + a));
		center.y = rotcen.y + (float)(distro * Math.sin(anglro + a));
		beta = betao + a;
		updatePoints();
	}
	
	public void setEnd(PointF ep)
	{
		normPoint(pn3, ep);
		
		if(panel.sideBar.rectCentCon())
			pn1.set(2*centero.x-pn3.x, 2*centero.y-pn3.y);
		
		updateRect();
	}
	
	/**
	 * move object rect
	 * @param sp source handle
	 * @param drag drag distance
	 */
	private void moveObject(PointF sp, PointF drag)
	{
		float x = sp.x - drag.x / panel.gfx;
		float y = sp.y - drag.y / panel.gfy;
		x = Math.round(x / panel.grid.wx) * panel.grid.wx;
		y = Math.round(y / panel.grid.wy) * panel.grid.wy;
		x -= sp.x;
		y -= sp.y;
		
		center.set(centero.x + x, centero.y + y);
		updatePoints();
	}
	
	/**
	 * move handle rect
	 * @param sp source handle
	 * @param op opposite handle
	 * @param drag drag distance
	 * @param corang correction angle
	 */
	private void moveHandle(PointF sp, PointF op, PointF drag, float corang)	
	{
		float npx = Math.round((sp.x - drag.x / panel.gfx) / panel.grid.wx) * panel.grid.wx;
		float npy = Math.round((sp.y - drag.y / panel.gfy) / panel.grid.wy) * panel.grid.wy;
		
		if(!panel.sideBar.pointerCentCon())
			center.set((npx + op.x) / 2, (npy + op.y) / 2);

		radius = distPoint(npx, npy, op) / 2;
		
		if(corang < 0)
			alpha = corang + 1 - calcAngle(npx, npy, op) + beta;
		else
			alpha = calcAngle(npx, npy, op) + corang - beta;	
		
		updatePoints();
	}
	
	private void updateRect()
	{
		center.set((pn1.x + pn3.x) / 2f, (pn1.y + pn3.y) / 2f);
		radius = distPoint(pn1, pn3) / 2f;	
		alpha = calcAngle(pn1, pn3) - beta;	
		updatePoints();
	}
	
	private void updatePoints()
	{
		pn1.x = center.x + radius * (float)Math.cos(alpha-PI + beta);
		pn1.y = center.y + radius * (float)Math.sin(alpha-PI + beta);
		pn2.x = center.x + radius * (float)Math.cos(-alpha + beta);
		pn2.y = center.y + radius * (float)Math.sin(-alpha + beta);
		pn3.x = center.x + radius * (float)Math.cos(alpha + beta);
		pn3.y = center.y + radius * (float)Math.sin(alpha + beta);
		pn4.x = center.x + radius * (float)Math.cos(PI-alpha + beta);
		pn4.y = center.y + radius * (float)Math.sin(PI-alpha + beta);
	}
	
	@Override
	public void writePars(ObjectOutputStream oos) throws IOException
	{
		writeParsC(oos);
		oos.writeFloat(beta);
	}
}

class DrawArc extends DrawObject
{
	PointF center = new PointF();	// center
	float beta;						// rotation angle				(deg)
	PointF png = new PointF();		// handle gamma							 
	PointF pnd = new PointF();		//		  delta	
	float gamma;					// 		  angle start			(deg)
	float delta;					// 				end				(deg)
	float deltad;					//					addition	(deg)
	
	PointF centero = new PointF();	// save values
	PointF pngo = new PointF();
	PointF pndo = new PointF();	
	float betao;
	
	PointF rotcen = new PointF();	// rotation center
	float rotdiso; 					// 		    distance
	float rotango;					//			angle
	
	RectF oval = new RectF();		// oval rect to avoid allocation in draw routine
	
	DrawArc(GraficPanel p, PointF sp)
	{
		super(p, Tool.DRAW_ARC, sp);
		gamma = 0;
		delta = 360;
		updateArc();
		centero.set(center);		// for center orientated construction
	}	

	DrawArc(GraficPanel p, ObjectInputStream ois) throws IOException
	{
		super(p, Tool.DRAW_ARC, ois);
		
		beta = ois.readFloat();
		gamma = ois.readFloat();
		delta = ois.readFloat();
		deltad = ois.readFloat();
		updateArc();
	}
	
	@Override
	public void draw(Canvas canvas)
	{
		float hs = panel.hs;
		float p1x = pixel_x(pn1.x);
		float p1y = pixel_y(pn1.y);
		float p3x = pixel_x(pn3.x);		
		float p3y = pixel_y(pn3.y);	
		float pcx = pixel_x(center.x);
		float pcy = pixel_y(center.y);
		
		if((Math.abs(p1x-p3x)>hs || Math.abs(p1y-p3y)>hs)){						// object
			float x1 = p1x;
			float y1 = p1y;
			float x3 = p3x;
			float y3 = p3y;
			if(x1 > x3) { x1 = p3x; x3 = p1x; }
			if(y1 > y3) { y1 = p3y; y3 = p1y; }
			oval.set(x1, y1, x3, y3);
			
			canvas.save();
			canvas.rotate(beta, pcx, pcy);
			canvas.drawArc(oval, gamma, delta+deltad-gamma, false, panel.conPaint);
			canvas.restore();
		}																	
		else{																	// representation
			canvas.drawLine(pcx-hs, pcy-hs, pcx+hs, pcy+hs, panel.conPaint);	// cross
			canvas.drawLine(pcx-hs, pcy+hs, pcx+hs, pcy-hs, panel.conPaint);
			canvas.drawCircle(pcx, pcy, hs/2, panel.conPaint);					// circle
		}

		if((flags & Flags.SELECTED) != 0){										// handles
			float p2x = pixel_x(pn2.x);
			float p2y = pixel_y(pn2.y);
			float p4x = pixel_x(pn4.x);		
			float p4y = pixel_y(pn4.y);
			float pax = pixel_x(png.x);		
			float pay = pixel_y(png.y);
			float pbx = pixel_x(pnd.x);		
			float pby = pixel_y(pnd.y);
			
			canvas.save();
			canvas.rotate(beta, pcx, pcy);
			
			canvas.drawLine(p1x, p1y, p2x, p2y, panel.dotsPaint);						// box
			canvas.drawLine(p2x, p2y, p3x, p3y, panel.dotsPaint);				
			canvas.drawLine(p3x, p3y, p4x, p4y, panel.dotsPaint);				
			canvas.drawLine(p4x, p4y, p1x, p1y, panel.dotsPaint);		
			
			canvas.drawRect(p1x-hs, p1y-hs, p1x+hs, p1y+hs, panel.conPaint);			// handles
			canvas.drawRect(p2x-hs, p2y-hs, p2x+hs, p2y+hs, panel.conPaint);	
			canvas.drawRect(p3x-hs, p3y-hs, p3x+hs, p3y+hs, panel.conPaint);	
			canvas.drawRect(p4x-hs, p4y-hs, p4x+hs, p4y+hs, panel.conPaint);	
			
			canvas.drawLine(pcx, pcy-panel.CESI, pcx, pcy+panel.CESI, panel.conPaint);	// center
			canvas.drawLine(pcx-panel.CESI, pcy, pcx+panel.CESI, pcy, panel.conPaint);
			
			canvas.drawRect(pax-hs, pay-hs, pax+hs, pay+hs, panel.conPaint);			// start
			canvas.drawText(String.valueOf(gamma), pax+2*hs, pay-2*hs, panel.conPaint);
			
			canvas.drawRect(pbx-hs/2, pby-hs/2, pbx+hs/2, pby+hs/2, panel.conPaint);	// sweep
			canvas.drawText(String.valueOf(delta), pbx+2*hs, pby+2*hs, panel.conPaint);
			
			canvas.restore();
		}
	}
	
	@Override
	public float touchDist(PointF rop)
	{
		float dist = Float.MAX_VALUE;
		float gbo = panel.gbo;
		PointF p = unrotPoint(rop);
		
		if(p.x<Math.max(pn1.x, pn3.x)+gbo && p.x>Math.min(pn1.x, pn3.x)-gbo &&
		   p.y<Math.max(pn1.y, pn3.y)+gbo && p.y>Math.min(pn1.y, pn3.y)-gbo){
			
			float dhan = Float.MAX_VALUE;
			float dang = Float.MAX_VALUE;
			
			float a = Math.abs(pn3.x - pn1.x) / 2;
			float b = Math.abs(pn3.y - pn1.y) / 2;
			
			if((flags & Flags.SELECTED) != 0){		// if selected
				float d0 = distLine(pn1, pn2, p);		
				float d1 = distLine(pn2, pn3, p);
				float d2 = distLine(pn3, pn4, p);
				float d3 = distLine(pn4, pn1, p);
				float dc = distPoint(center, p);
				
				dhan = Math.min(d0, dc);
				dhan = Math.min(dhan, d1);
				dhan = Math.min(dhan, d2);
				dhan = Math.min(dhan, d3);
			}
			else if(a<gbo/2 || b<gbo/2){			// if not selected and one rectangle side is very small
				dhan = gbo / 2;						// set small handle distance
			}
			
			if(a>0f && b>0f){
				float cx = p.x - center.x;
				float cy = p.y - center.y;
				float bcx = b * cx;
				float acy = a * cy;
				float ro = (float)Math.sqrt(bcx*bcx + acy*acy);
						
				if(ro != 0f){
					float sx = a * bcx / ro;		// sx = a*b*cx / sqrt((b*cx)^2 + (a*cy)^2)
					float sy = b * acy / ro;		// sy = a*b*cy / sqrt((b*cx)^2 + (a*cy)^2)
					
					float w = (float)Math.acos(sx / a) / PI180;
					if(sy < 0) w = 360 - w;
					
					w += 360;						// prepare w>a-5 w>b+5
					
					if(deltad == 0f){
						if(w>gamma+355 && w<delta+365)
							dang = distPoint(sx, sy, cx, cy);
					}
					else{
						if(w>gamma+355 || w<delta+365)
							dang = distPoint(sx, sy, cx, cy);
					}
				}
			}
			
			dang = Math.min(dang, dhan);
			
			if(dang < gbo)
				dist = dang;
		}
		
		return dist;				
	}
	
	@Override
	public void changeInitMove(PointF rop)
	{
		PointF p = unrotPoint(rop);
		
		pn1o.set(pn1);								// save current parameters
		pn2o.set(pn2);
		pn3o.set(pn3);
		pn4o.set(pn4);
		pngo.set(png);
		pndo.set(pnd);
		centero.set(center);
		
		float d1 = distPoint(pn1, p);				// calc distance to handles
		float d2 = distPoint(pn2, p);
		float d3 = distPoint(pn3, p);
		float d4 = distPoint(pn4, p);
		float da = distPoint(png, p);				
		float db = distPoint(pnd, p);
		float dc = distPoint(center, p);	
		
		boolean mo = panel.sideBar.pointerMoveO();	// get move flags
		boolean mh = panel.sideBar.pointerMoveH();
		boolean ms = panel.sideBar.pointerMultisel();
		
		flags &= ~Flags.CHANGEMASK;					// reset and set change flags
		float g = panel.gbo;
		
		if(mo || ms || (!mh && d1>g && d2>g && d3>g && d4>g && da>g && db>g) || (!mh && d1<g && d2<g && d3<g && d4<g && da<g && db<g))
			flags |= Flags.MOVEOBJECT;
		else if(d1<g || d2<g || d3<g || d4<g || da<g || db<g)
			flags |= Flags.MOVEHANDLE;

		if(d2<d1 && d2<d3 && d2<d4 && d2<da && d2<db)
			flags |= Flags.HA_P2;
		else if(d3<d1 && d3<d2 && d3<d4 && d3<da && d3<db)
			flags |= Flags.HA_P3;
		else if(d4<d1 && d4<d2 && d4<d3 && d4<da && d4<db)
			flags |= Flags.HA_P4;
		else if(dc<d1 && dc<d2 && dc<d3 && dc<d4 && dc<da && dc<db)
			flags |= Flags.HA_CENTER;
		else if(da<d1 && da<d2 && da<d3 && da<d4 && da<=db)		// <= for da=db
			flags |= Flags.HA_ALPHA;
		else if(db<d1 && db<d2 && db<d3 && db<d4 && db<da)
			flags |= Flags.HA_BETA;
		else
			flags |= Flags.HA_P1;
	}

	@Override
	public void changeObjectMove(PointF drag)
	{			
		if((flags & Flags.MOVEOBJECT) != 0){
			if((flags & Flags.HA_CENTER) != 0)
				moveObject(centero, drag);
			else if((flags & Flags.HA_P1) != 0)
				moveObject(pn1o, drag);
			else if((flags & Flags.HA_P2) != 0)
				moveObject(pn2o, drag);
			else if((flags & Flags.HA_P3) != 0)
				moveObject(pn3o, drag);
			else if((flags & Flags.HA_P4) != 0)
				moveObject(pn4o, drag);
			else if((flags & Flags.HA_ALPHA) != 0)
				moveObject(pngo, drag);
			else
				moveObject(pndo, drag);
		}
		else if((flags & Flags.MOVEHANDLE) != 0){
			if((flags & Flags.HA_P1) != 0)
				moveHandlePoint(pn1o, drag);
			else if((flags & Flags.HA_P2) != 0)
				moveHandlePoint(pn2o, drag);
			else if((flags & Flags.HA_P3) != 0)
				moveHandlePoint(pn3o, drag);
			else if((flags & Flags.HA_P4) != 0)
				moveHandlePoint(pn4o, drag);
			else if((flags & Flags.HA_ALPHA) != 0)
				moveHandleAngle(pngo, drag);
			else
				moveHandleAngle(pndo, drag);
		}
	}
	
	@Override
	public void changeObjectMoveMulti(PointF drag, boolean init)
	{
		if(!init){
			pn1o.set(pn1);					// save current parameters
			pn3o.set(pn3);
		}
		
		pn1.set(pn1o.x - drag.x, pn1o.y - drag.y);
		pn3.set(pn3o.x - drag.x, pn3o.y - drag.y);
		updateArc();
	}
	
	@Override
	public void changeInitRotate(PointF rc)
	{			
		betao = beta;								// save rotation angle				
		pn1o.set(pn1);								// 		points
		pn3o.set(pn3);
		rotcen.set(rc.x-center.x, rc.y-center.y);	// 		center difference
		
		rotdiso = distPoint(rc, center);			// distance to center
		rotango = calcAngle(rc, center);			// angle to center
		
		flags &= ~Flags.CHANGEMASK;					// reset and change flags
		flags |= Flags.ROTATE;						// set rotate flag
	}
	
	@Override
	public void changeObjectRotate(float a)
	{
		float x = rotcen.x + (float)(rotdiso * Math.cos(rotango + a));
		float y = rotcen.y + (float)(rotdiso * Math.sin(rotango + a));
		pn1.set(pn1o.x + x, pn1o.y + y);
		pn3.set(pn3o.x + x, pn3o.y + y);
		beta = betao + a / PI180;
		updateArc();
	}
	
	public void setEnd(PointF ep)
	{
		normPoint(pn3, ep);
		
		if(panel.sideBar.arcCentCon())
			pn1.set(2*centero.x-pn3.x, 2*centero.y-pn3.y);
		
		updateArc();
	}
	
	private void moveObject(PointF sp, PointF drag)
	{
		float x = sp.x - drag.x / panel.gfx;
		float y = sp.y - drag.y / panel.gfy;
		x = Math.round(x / panel.grid.wx) * panel.grid.wx;
		y = Math.round(y / panel.grid.wy) * panel.grid.wy;
		x -= sp.x;
		y -= sp.y;
		pn1.set(pn1o.x + x, pn1o.y + y);
		pn3.set(pn3o.x + x, pn3o.y + y);
		updateArc();
	}
	
	private void moveHandlePoint(PointF sp, PointF rdrag)	
	{
		PointF drag = unrotDrag(rdrag);
		
		float x = Math.round((sp.x - drag.x / panel.gfx) / panel.grid.wx) * panel.grid.wx - sp.x;
		float y = Math.round((sp.y - drag.y / panel.gfy) / panel.grid.wy) * panel.grid.wy - sp.y;
		
		if(panel.sideBar.pointerCentCon()){
			if(sp == pn1o){
				pn1.set(pn1o.x + x, pn1o.y + y);
				pn3.set(pn3o.x - x, pn3o.y - y);
			}
			else if(sp == pn3o){
				pn1.set(pn1o.x - x, pn1o.y - y);
				pn3.set(pn3o.x + x, pn3o.y + y);
			}
			else if(sp == pn2o){
				pn1.set(pn1o.x - x, pn1o.y + y);
				pn3.set(pn3o.x + x, pn3o.y - y);
			}
			else if(sp == pn4o){
				pn1.set(pn1o.x + x, pn1o.y - y);
				pn3.set(pn3o.x - x, pn3o.y + y);
			}
		}
		else{
			if(sp == pn1o){
				pn1.set(pn1o.x + x, pn1o.y + y);
			}
			else if(sp == pn3o){
				pn3.set(pn3o.x + x, pn3o.y + y);
			}
			else if(sp == pn2o){
				pn3.x = pn3o.x + x;
				pn1.y = pn1o.y + y;
			}
			else if(sp == pn4o){
				pn1.x = pn1o.x + x;
				pn3.y = pn3o.y + y;
			}
		}
		
		updateArc();
	}
	
	private void moveHandleAngle(PointF sp, PointF rdrag)	
	{
		float a = Math.abs(pn3.x - pn1.x) / 2f;
		float b = Math.abs(pn3.y - pn1.y) / 2f;
		
		if(a>0f && b>0f){
			PointF drag = unrotDrag(rdrag);
			float cx = sp.x - center.x - drag.x / panel.gfx;
			float cy = sp.y - center.y - drag.y / panel.gfy;
			float bcx = b * cx;
			float acy = a * cy;
			float ro = (float)Math.sqrt(bcx*bcx + acy*acy);
					
			if(ro != 0f){
				float sx = a * bcx / ro;	// sx = a*b*cx / sqrt((b*cx)^2 + (a*cy)^2)
				float sy = b * acy / ro;	// sy = a*b*cy / sqrt((b*cx)^2 + (a*cy)^2)
				
				float w = (float)Math.acos(sx / a) / PI180;
				if(sy < 0) w = 360 - w;		// 0..360
				w = Math.round(w / 5f) * 5f;
				
				if(sp == pngo)
					gamma = w;
				else
					delta = w;
				
				deltad = delta>gamma ? 0f : 360f;
				updateArc();
			}
		}
	}
	
	public void updateArc()
	{
		center.set((pn1.x + pn3.x) / 2f, (pn1.y + pn3.y) / 2f);
		
		pn2.set(pn3.x, pn1.y);
		pn4.set(pn1.x, pn3.y);
		
		float a = Math.abs(pn3.x - pn1.x) / 2f;
		float b = Math.abs(pn3.y - pn1.y) / 2f;
		
		png.x = center.x + a * (float)Math.cos(gamma * PI180);
		png.y = center.y + b * (float)Math.sin(gamma * PI180);
		pnd.x = center.x + a * (float)Math.cos(delta * PI180);
		pnd.y = center.y + b * (float)Math.sin(delta * PI180);
	}
	
	PointF urp = new PointF();
	
	private PointF unrotPoint(PointF rop)
	{
	    float c = (float)Math.cos(-beta * PI180);								
	    float s = (float)Math.sin(-beta * PI180);
	    urp.x = center.x + c * (rop.x - center.x) - s * (rop.y - center.y);		
	    urp.y = center.y + s * (rop.x - center.x) + c * (rop.y - center.y);
	    return urp;
	}
	
	private PointF unrotDrag(PointF drag)
	{
	    float c = (float)Math.cos(-beta * PI180);								
	    float s = (float)Math.sin(-beta * PI180);
	    urp.x = c * drag.x - s * drag.y;		
	    urp.y = s * drag.x + c * drag.y;
	    return urp;
	}
	
	@Override
	public void writePars(ObjectOutputStream oos) throws IOException
	{
		writeParsC(oos);
		oos.writeFloat(beta);
		oos.writeFloat(gamma);
		oos.writeFloat(delta);
		oos.writeFloat(deltad);
	}
}

class DrawFree extends DrawObject
{		
	ArrayList<PointF> point = new ArrayList<PointF>();
	ArrayList<PointF> pointo;
	
	PointF trans = new PointF();
	PointF transo = new PointF();

	PointF center = new PointF();
	PointF centero = new PointF();
	PointF rotcen = new PointF();
	float beta;						// angle of rotation			(bow)
	float scaleX;					// text scale x
	float scaleY;					//			  y	
	
	DrawFree(GraficPanel p, PointF sp)
	{
		super(p, Tool.DRAW_FREE, sp);
		PointF np = new PointF();
		np.set(pn1);
		point.add(np);
		center.set((pn1.x + pn3.x) / 2f, (pn1.y + pn3.y) / 2f);
	}	

	DrawFree(GraficPanel p, ObjectInputStream ois) throws IOException
	{
		super(p, Tool.DRAW_FREE, ois);
		pn2.set(pn1.x, pn3.y);
		pn4.set(pn3.x, pn1.y);
		center.set((pn1.x + pn3.x) / 2f, (pn1.y + pn3.y) / 2f);
		
		int size = ois.readInt();
		
		for(int i=0; i<size; i++){
			PointF np = new PointF();
			np.x = ois.readFloat();
			np.y = ois.readFloat();
			point.add(np);
		}
	}
	
	@Override
	public void draw(Canvas canvas)
	{
		float hs = panel.hs;
		float p1x = pixel_x(pn1.x);
		float p1y = pixel_y(pn1.y);
		float p3x = pixel_x(pn3.x);
		float p3y = pixel_y(pn3.y);
		float cx = pixel_x(center.x);
		float cy = pixel_y(center.y);
		
		if(Math.abs(p1x-p3x)>hs || Math.abs(p1y-p3y)>hs){						// object
			if(point.size() > 1){
				canvas.save();
				
				canvas.scale(panel.gfx, panel.gfy);
				canvas.translate(panel.left/panel.gfx + trans.x, panel.top/panel.gfy + trans.y);
//				canvas.rotate(beta/PI180, p1x, p1y);
				
				float px0 = point.get(0).x;
				float py0 = point.get(0).y;
				float px1;
				float py1;
				
				for(int i=1; i<point.size(); i++){
					px1 = point.get(i).x;
					py1 = point.get(i).y;
					canvas.drawLine(px0, py0, px1, py1, panel.conPaint);
					px0 = px1;
					py0 = py1;
				}
				
				canvas.restore();
			}
		}																	
		else{																	// representation
			float qs = hs / 2;
			canvas.drawLine(cx-hs, cy-hs, cx+hs, cy+hs, panel.conPaint);			// cross
			canvas.drawLine(cx-hs, cy+hs, cx+hs, cy-hs, panel.conPaint);
			canvas.drawLine(cx-qs, cy, cx+qs, cy, panel.conPaint);					// line
		}
	
		if((flags & Flags.SELECTED) != 0){										// handles
			float p2x = pixel_x(pn2.x);
			float p2y = pixel_y(pn2.y);
			float p4x = pixel_x(pn4.x);
			float p4y = pixel_y(pn4.y);
			
			canvas.drawLine(p1x, p1y, p2x, p2y, panel.dotsPaint);				// box
			canvas.drawLine(p2x, p2y, p3x, p3y, panel.dotsPaint);				
			canvas.drawLine(p3x, p3y, p4x, p4y, panel.dotsPaint);				
			canvas.drawLine(p4x, p4y, p1x, p1y, panel.dotsPaint);				
			
			canvas.drawRect(p1x-hs, p1y-hs, p1x+hs, p1y+hs, panel.conPaint);	// edges
			canvas.drawRect(p2x-hs, p2y-hs, p2x+hs, p2y+hs, panel.conPaint);
			canvas.drawRect(p3x-hs, p3y-hs, p3x+hs, p3y+hs, panel.conPaint);
			canvas.drawRect(p4x-hs, p4y-hs, p4x+hs, p4y+hs, panel.conPaint);
	
			canvas.drawLine(cx, cy-panel.CESI, cx, cy+panel.CESI, panel.conPaint);
			canvas.drawLine(cx-panel.CESI, cy, cx+panel.CESI, cy, panel.conPaint);			
		}
	}	
	
	@Override
	public float touchDist(PointF p)
	{	
		float dist = Float.MAX_VALUE;
		
		if(p.x<Math.max(pn1.x, pn3.x)+panel.gbo && p.x>Math.min(pn1.x, pn3.x)-panel.gbo &&
		   p.y<Math.max(pn1.y, pn3.y)+panel.gbo && p.y>Math.min(pn1.y, pn3.y)-panel.gbo){

			float dbox = Float.MAX_VALUE;
			float dobj = Float.MAX_VALUE;
			
			if((flags & Flags.SELECTED) != 0){		// if selected
				float d0 = distLine(pn1, pn2, p);		
				float d1 = distLine(pn2, pn3, p);
				float d2 = distLine(pn3, pn4, p);
				float d3 = distLine(pn4, pn1, p);
				float dc = distPoint(center, p);
				
				dbox = Math.min(d0, dc);
				dbox = Math.min(dbox, d1);
				dbox = Math.min(dbox, d2);
				dbox = Math.min(dbox, d3);
			}
			
			for(PointF pp : point){
				float d = distPoint(pp, p);
				if(d < dobj) dobj = d;
			}
		
			float dmin = Math.min(dobj, dbox);
			if(dmin < panel.gbo) dist = dmin;
		}
		
		return dist;	
	}
	
	@Override
	public void changeInitMove(PointF p)
	{
		transo.set(trans);
		pn1o.set(pn1);
		pn2o.set(pn2);
		pn3o.set(pn3);
		pn4o.set(pn4);
		centero.set(center);
		
		float p1 = distPoint(pn1, p);				// calc distance to handles
		float p2 = distPoint(pn2, p);
		float p3 = distPoint(pn3, p);
		float p4 = distPoint(pn4, p);
		float dc = distPoint(center, p);
		
		boolean mo = panel.sideBar.pointerMoveO();	// get move/rotate/multisel flags
		boolean mh = panel.sideBar.pointerMoveH();
		boolean ms = panel.sideBar.pointerMultisel();
		
		flags &= ~Flags.CHANGEMASK;					// reset and set change flags
		float g = panel.gbo;
		
		if(mo || ms || (!mh && p1>g && p2>g && p3>g && p4>g) || (!mh && p1<g && p2<g && p3<g && p4<g))
			flags |= Flags.MOVEOBJECT;
		else if(p1<g || p3<g || p2<g || p4<g)
			flags |= Flags.MOVEHANDLE;
		
		if(p1<p2 && p1<p3 && p1<p4 && p1<dc)
			flags |= Flags.HA_P1;
		else if(p2<p1 && p2<p3 && p2<p4 && p2<dc)
			flags |= Flags.HA_P2;
		else if(p3<p1 && p3<p2 && p3<p4 && p3<dc)
			flags |= Flags.HA_P3;
		else if(p4<p1 && p4<p2 && p4<p3 && p4<dc)
			flags |= Flags.HA_P4;
		else if(dc<p1 && dc<p2 && dc<p3 && dc<p4)
			flags |= Flags.HA_CENTER;
	}
	
	@Override
	public void changeObjectMove(PointF drag)
	{			
		if((flags & Flags.MOVEOBJECT) != 0){
			if((flags & Flags.HA_P1) !=0)
				moveObject(pn1o, drag);
			else if((flags & Flags.HA_P2) != 0)
				moveObject(pn2o, drag);
			else if((flags & Flags.HA_P3) != 0)
				moveObject(pn3o, drag);
			else if((flags & Flags.HA_P4) != 0)
				moveObject(pn4o, drag);
			else if((flags & Flags.HA_CENTER) != 0)
				moveObject(centero, drag);

		}
		else if((flags & Flags.MOVEHANDLE) != 0){
			if((flags & Flags.HA_P1) != 0)
				moveHandle(pn1, pn1o, drag);
			else if((flags & Flags.HA_P2) != 0)
				moveHandle(pn2, pn2o, drag);
			else if((flags & Flags.HA_P3) != 0)
				moveHandle(pn3, pn3o, drag);
			else if((flags & Flags.HA_P4) != 0)
				moveHandle(pn4, pn4o, drag);
		}
	}	
	
	@Override
	public void changeObjectMoveMulti(PointF drag, boolean init)
	{
		if(!init){
			pn1o.set(pn1);						// save original po1 and po3 points
			pn3o.set(pn3);
		}
		
		pn1.set(pn1o.x - drag.x, pn1o.y - drag.y);
		pn3.set(pn3o.x - drag.x, pn3o.y - drag.y);
	}
	
	@Override
	public void changeInitRotate(PointF c)
	{
		flags &= ~Flags.CHANGEMASK;				// reset and change flags
		flags |= Flags.ROTATE;					// set rotate flag
	}
	
	@Override
	public void changeObjectRotate(float angle)
	{
	}
	
	PointF op = new PointF();
	
	public void setNext(PointF ep)
	{
		PointF np = new PointF();
		normPoint(np, ep);
		
		if(Math.abs(op.x-np.x)>0.01f || Math.abs(op.y-np.y)>0.01f){			
			point.add(np);
			op.set(np);
			
			if(np.x < pn1.x) pn1.x = np.x;
			if(np.y < pn1.y) pn1.y = np.y;
			if(np.x > pn3.x) pn3.x = np.x;
			if(np.y > pn3.y) pn3.y = np.y;
			
			pn2.x = pn1.x;
			pn2.y = pn3.y;
			pn4.x = pn3.x;
			pn4.y = pn1.y;
			
			center.set((pn1.x + pn3.x) / 2f, (pn1.y + pn3.y) / 2f);
		}
	}

	/**
	 * move object
	 * @param sp source
	 * @param op offset screen
	 */
	private void moveObject(PointF sp, PointF op)	
	{
		float dx = Math.round((sp.x - op.x / panel.gfx) / panel.grid.wx) * panel.grid.wx - sp.x;
		float dy = Math.round((sp.y - op.y / panel.gfy) / panel.grid.wy) * panel.grid.wy - sp.y;	
		
		trans.set(transo.x + dx, transo.y + dy);
		
		pn1.set(pn1o.x + dx, pn1o.y + dy);
		pn2.set(pn2o.x + dx, pn2o.y + dy);
		pn3.set(pn3o.x + dx, pn3o.y + dy);
		pn4.set(pn4o.x + dx, pn4o.y + dy);
		center.set(centero.x + dx, centero.y + dy);
	}
	
	/**
	 * move handle line, circle
	 * @param tp target
	 * @param sp source
	 * @param op offset screen
	 */
	private void moveHandle(PointF tp, PointF sp, PointF op)	
	{
		float nx = Math.round((sp.x - op.x / panel.gfx) / panel.grid.wx);
		float ny = Math.round((sp.y - op.y / panel.gfy) / panel.grid.wy);			
		tp.set(nx * panel.grid.wx, ny * panel.grid.wy);
		
		if(tp == pn1){
			pn2.x = pn1.x;
			pn4.y = pn1.y;
		}
		else if(tp == pn2){
			pn1.x = pn2.x;
			pn3.y = pn2.y;
		}
		else if(tp == pn3){
			pn4.x = pn3.x;
			pn2.y = pn3.y;
		}
		else{
			pn3.x = pn4.x;
			pn1.y = pn4.y;
		}
		
		center.set((pn1.x + pn3.x) / 2f, (pn1.y + pn3.y) / 2f);
	}
	
	@Override
	public void writePars(ObjectOutputStream oos) throws IOException
	{		
		writeParsC(oos);
		oos.writeInt(point.size());
		
		for(PointF p : point){
			oos.writeFloat(p.x);
			oos.writeFloat(p.y);
		}
	}
}

class DrawText extends DrawObject
{
	String text = "";				// text			save values
	float beta;						// angle of rotation			(bow)
	Paint paint = new Paint();		// paint 		not saved yet

	float defaultSize = 45f;		// default size
	PointF center = new PointF();	// center
	float radius;					// radius of surrounding circle
	float alpha;					// angle of point 1 (3) of unrotated rectangle
	float scaleX;					// text scale x
	float scaleY;					//			  y	
	
	PointF centero = new PointF();	// save values
	float betao;
	
	PointF rotcen = new PointF();	// rotation center
	float distro;					// rotation center to object center distance
	float anglro;					// 									angle 
	
	DrawText(GraficPanel p, PointF sp, String text)
	{
		super(p, Tool.DRAW_TEXT, sp);
		
		this.text = text;
		
		paint.setTextSize(defaultSize);
	    Rect bounds = new Rect();
	    paint.getTextBounds(text, 0, text.length(), bounds);	
		pn3.set(pn1.x + bounds.width()/p.gfx, pn1.y - bounds.height()/p.gfy);
		scaleX = 1f / p.gfx;
		scaleY = 1f / p.gfy;
		updateRect();
	}	
	
	DrawText(GraficPanel p, ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		super(p, Tool.DRAW_TEXT, ois);
		
		beta = ois.readFloat();
		text = (String)ois.readObject();
		
		paint.setTextSize(defaultSize);
		updateRect();
		scaleText();
	}

	public void updateText(String text)
	{
		this.text = text;
		scaleText();
	}
	
	@Override
	public void draw(Canvas canvas)
	{
		float hs = panel.hs;
		float p1x = pixel_x(pn1.x);
		float p1y = pixel_y(pn1.y);
		float p3x = pixel_x(pn3.x);		
		float p3y = pixel_y(pn3.y);			
		
		if(Math.abs(p1x-p3x)>hs || Math.abs(p1y-p3y)>hs){						// object
			canvas.save();
			canvas.rotate(beta/PI180, p1x, p1y);
			canvas.scale(scaleX*panel.gfx, scaleY*panel.gfy, p1x, p1y);
			canvas.drawText(text, p1x, p1y, paint);
			canvas.restore();
		}
		else{																	// representation
			float x = (p1x + p3x) / 2;
			float y = (p1y + p3y) / 2;
			float qs = hs / 2;
			canvas.drawLine(x-hs, y-hs, x+hs, y+hs, panel.conPaint);			// cross
			canvas.drawLine(x-hs, y+hs, x+hs, y-hs, panel.conPaint);
			canvas.drawText("A", x+qs, y+qs, panel.textRepPaint);
		}
		
		if((flags & Flags.SELECTED) != 0){										// handles
			float p2x = pixel_x(pn2.x);
			float p2y = pixel_y(pn2.y);
			float p4x = pixel_x(pn4.x);		
			float p4y = pixel_y(pn4.y);	
			
			canvas.drawLine(p1x, p1y, p2x, p2y, panel.dotsPaint);				// box
			canvas.drawLine(p2x, p2y, p3x, p3y, panel.dotsPaint);				
			canvas.drawLine(p3x, p3y, p4x, p4y, panel.dotsPaint);				
			canvas.drawLine(p4x, p4y, p1x, p1y, panel.dotsPaint);				
			
			canvas.drawRect(p1x-hs, p1y-hs, p1x+hs, p1y+hs, panel.conPaint);	// edges
			canvas.drawRect(p2x-hs, p2y-hs, p2x+hs, p2y+hs, panel.conPaint);
			canvas.drawRect(p3x-hs, p3y-hs, p3x+hs, p3y+hs, panel.conPaint);
			canvas.drawRect(p4x-hs, p4y-hs, p4x+hs, p4y+hs, panel.conPaint);
	
			float cx = (p1x + p3x) / 2f;										// center
			float cy = (p1y + p3y) / 2f;
			canvas.drawLine(cx, cy-panel.CESI, cx, cy+panel.CESI, panel.conPaint);
			canvas.drawLine(cx-panel.CESI, cy, cx+panel.CESI, cy, panel.conPaint);
		}
	}
	
	@Override
	public float touchDist(PointF p)
	{
		float gbo = panel.gbo;
		float dist = Float.MAX_VALUE;
		
		float xmin = Math.min(pn1.x, pn2.x);
		xmin = Math.min(xmin, pn3.x);
		xmin = Math.min(xmin, pn4.x);
		
		float ymin = Math.min(pn1.y, pn2.y);
		ymin = Math.min(ymin, pn3.y);
		ymin = Math.min(ymin, pn4.y);
		
		float xmax = Math.max(pn1.x, pn2.x);
		xmax = Math.max(xmax, pn3.x);
		xmax = Math.max(xmax, pn4.x);
		
		float ymax = Math.max(pn1.y, pn2.y);
		ymax = Math.max(ymax, pn3.y);
		ymax = Math.max(ymax, pn4.y);		
		
		if(p.x<xmax+gbo && p.x>xmin-gbo && p.y<ymax+gbo && p.y>ymin-gbo){
			float pu1x = center.x + radius * (float)Math.cos(alpha-PI);		// unrotate box
			float pu1y = center.y + radius * (float)Math.sin(alpha-PI);
			float pu3x = center.x + radius * (float)Math.cos(alpha);
			float pu3y = center.y + radius * (float)Math.sin(alpha);
			
		    float c = (float)Math.cos(-beta);								// unrotate point
		    float s = (float)Math.sin(-beta);
		    float px = center.x + c * (p.x - center.x) - s * (p.y - center.y);		
		    float py = center.y + s * (p.x - center.x) + c * (p.y - center.y);
			
			xmin = Math.min(pu1x, pu3x);
			xmax = Math.max(pu1x, pu3x);
			ymin = Math.min(pu1y, pu3y);
			ymax = Math.max(pu1y, pu3y);
			
			if(px<xmax+gbo && px>xmin-gbo && py<ymax+gbo && py>ymin-gbo)
				dist = 0;
		}
		
		return dist;	
	}
	
	@Override
	public void changeInitMove(PointF p)
	{
		centero.set(center);						// save current parameters
		pn1o.set(pn1);
		pn2o.set(pn2);
		pn3o.set(pn3);
		pn4o.set(pn4);
		
		float d1 = distPoint(pn1, p);				// calc distance to handles
		float d2 = distPoint(pn2, p);
		float d3 = distPoint(pn3, p);
		float d4 = distPoint(pn4, p);
		float dc = distPoint(center, p);			// calc distance to center
					
		boolean mo = panel.sideBar.pointerMoveO();	// get move flags
		boolean mh = panel.sideBar.pointerMoveH();
		boolean ms = panel.sideBar.pointerMultisel();
		
		flags &= ~Flags.CHANGEMASK;					// reset and set change flags
		float g = panel.gbo;
		
		if(mo || ms || (!mh && d1>g && d3>g && d2>g && d4>g) || (!mh && d1<g && d3<g && d2<g && d4<g))
			flags |= Flags.MOVEOBJECT;
		else if(d1<g || d3<g || d2<g || d4<g)
			flags |= Flags.MOVEHANDLE;
			
		if(d2<d1 && d2<d3 && d2<d4 && d2<dc)
			flags |= Flags.HA_P2;
		else if(d3<d1 && d3<d2 && d3<d4 && d3<dc)
			flags |= Flags.HA_P3;
		else if(d4<d1 && d4<d2 && d4<d3 && d4<dc)
			flags |= Flags.HA_P4;
		else if(dc<d1 && dc<d2 && dc<d3 && dc<d4)
			flags |= Flags.HA_CENTER;
		else
			flags |= Flags.HA_P1;
	}

	@Override
	public void changeObjectMove(PointF drag)
	{			
		if((flags & Flags.MOVEOBJECT) != 0){
			if((flags & Flags.HA_CENTER) != 0)
				moveObject(centero, drag);
			else if((flags & Flags.HA_P1) != 0)
				moveObject(pn1o, drag);
			else if((flags & Flags.HA_P2) != 0)
				moveObject(pn2o, drag);	
			else if((flags & Flags.HA_P3) != 0)
				moveObject(pn3o, drag);
			else
				moveObject(pn4o, drag);
		}
		else if((flags & Flags.MOVEHANDLE) != 0){
			if((flags & Flags.HA_P1) != 0)
				moveHandle(pn1o, pn3o, drag, 0);
			else if((flags & Flags.HA_P2) != 0)
				moveHandle(pn2o, pn4o, drag, -PI-1);
			else if((flags & Flags.HA_P3) != 0)
				moveHandle(pn3o, pn1o, drag, PI);
			else
				moveHandle(pn4o, pn2o, drag, -1);
		}
	}
	
	@Override
	public void changeObjectMoveMulti(PointF drag, boolean init)
	{
		if(!init){
			pn1o.set(pn1);					// save current parameters
			pn3o.set(pn3);
		}
		
		pn1.set(pn1o.x - drag.x, pn1o.y - drag.y);
		pn3.set(pn3o.x - drag.x, pn3o.y - drag.y);
		updateRect();
	}
	
	@Override
	public void changeInitRotate(PointF rc)
	{
		betao = beta;						// save current rotation angle				
		rotcen.set(rc);						// set rotation center
		
		distro = distPoint(rc, center);		// rotation center to rect center distance
		anglro = calcAngle(rc, center);		// 								  angle 
		
		flags &= ~Flags.CHANGEMASK;			// reset and change flags
		flags |= Flags.ROTATE;				// set rotate flag
	}
	
	@Override
	public void changeObjectRotate(float a)
	{
		center.x = rotcen.x + (float)(distro * Math.cos(anglro + a));
		center.y = rotcen.y + (float)(distro * Math.sin(anglro + a));
		beta = betao + a;
		updatePoints();
	}
	
	/**
	 * move object rect
	 * @param sp source handle
	 * @param drag drag distance
	 */
	private void moveObject(PointF sp, PointF drag)
	{
		float x = sp.x - drag.x / panel.gfx;
		float y = sp.y - drag.y / panel.gfy;
		x = Math.round(x / panel.grid.wx) * panel.grid.wx;
		y = Math.round(y / panel.grid.wy) * panel.grid.wy;
		x -= sp.x;
		y -= sp.y;
		
		center.set(centero.x + x, centero.y + y);
		updatePoints();
	}
	
	/**
	 * move handle rect
	 * @param sp source handle
	 * @param op opposite handle
	 * @param drag drag distance
	 * @param corang correction angle
	 */
	private void moveHandle(PointF sp, PointF op, PointF drag, float corang)	
	{
		float npx = Math.round((sp.x - drag.x / panel.gfx) / panel.grid.wx) * panel.grid.wx;
		float npy = Math.round((sp.y - drag.y / panel.gfy) / panel.grid.wy) * panel.grid.wy;
		
		if(!panel.sideBar.pointerCentCon())
			center.set((npx + op.x) / 2f, (npy + op.y) / 2f);

		radius = distPoint(npx, npy, op) / 2f;
		
		if(corang < 0)
			alpha = corang + 1 - calcAngle(npx, npy, op) + beta;
		else
			alpha = calcAngle(npx, npy, op) + corang - beta;	
		
		scaleText();	
		updatePoints();
	}
	
	private void scaleText()
	{
		float pu1x = center.x + radius * (float)Math.cos(alpha-PI);		// unrotate box
		float pu1y = center.y + radius * (float)Math.sin(alpha-PI);
		float pu3x = center.x + radius * (float)Math.cos(alpha);
		float pu3y = center.y + radius * (float)Math.sin(alpha);
		float dWidth = (pu3x - pu1x) * panel.gfx;						// desired width
	    float dHeight = (pu1y - pu3y) * panel.gfy;						//		   height
	    
	    Rect bounds = new Rect();
	    paint.getTextBounds(text, 0, text.length(), bounds);	
	    float dSize = paint.getTextSize() * dWidth / bounds.width();	// 		   size
	    dSize = (float)Math.floor(dSize / 5) * 5f;						// in steps of 5
	    if(dSize < 10f) dSize = 10f;
	    paint.setTextSize(dSize);
	    
	    paint.getTextBounds(text, 0, text.length(), bounds);
	    scaleX = (dWidth / bounds.width()) / panel.gfx;	
	    scaleY = (dHeight / bounds.height()) / panel.gfy;		
	    if(Math.abs(scaleX) < 0.005f) scaleX = 0.005f;
	    if(Math.abs(scaleY) < 0.005f) scaleY = 0.005f;
	}
	
	private void updateRect()
	{
		center.set((pn1.x + pn3.x) / 2f, (pn1.y + pn3.y) / 2f);
		radius = distPoint(pn1, pn3) / 2f;	
		alpha = calcAngle(pn1, pn3) - beta;	
		updatePoints();
	}
	
	private void updatePoints()
	{
		pn1.x = center.x + radius * (float)Math.cos(alpha-PI + beta);
		pn1.y = center.y + radius * (float)Math.sin(alpha-PI + beta);
		pn2.x = center.x + radius * (float)Math.cos(-alpha + beta);
		pn2.y = center.y + radius * (float)Math.sin(-alpha + beta);
		pn3.x = center.x + radius * (float)Math.cos(alpha + beta);
		pn3.y = center.y + radius * (float)Math.sin(alpha + beta);
		pn4.x = center.x + radius * (float)Math.cos(PI-alpha + beta);
		pn4.y = center.y + radius * (float)Math.sin(PI-alpha + beta);
	}
	
	@Override
	public void writePars(ObjectOutputStream oos) throws IOException
	{
		writeParsC(oos);
		oos.writeFloat(beta);
		oos.writeObject(text);
	}
}

class DrawRotCent extends DrawObject
{
	float r;
	
	DrawRotCent(GraficPanel p, PointF sp)
	{
		super(p, Tool.DRAW_ROTCENT, sp);
		r = panel.hs / panel.gfx;
	}	
	
	@Override
	public void draw(Canvas canvas)
	{
		float p1x = pixel_x(pn1.x);
		float p1y = pixel_y(pn1.y);
		float sr = r * panel.gfx;
		
		canvas.drawCircle(p1x, p1y, sr, panel.rocPaint);					// object
		
		if((flags & Flags.SELECTED) != 0){									// handle
			float hs = panel.hs;
			canvas.drawRect(p1x-hs, p1y-hs, p1x+hs, p1y+hs, panel.conPaint);
		}
	}
	
	@Override
	public float touchDist(PointF p)
	{
		float dist = Float.MAX_VALUE;

		if(p.x<Math.max(pn1.x-r, pn1.x+r)+panel.gbo && p.x>Math.min(pn1.x-r, pn1.x+r)-panel.gbo &&
		   p.y<Math.max(pn1.y-r, pn1.y+r)+panel.gbo && p.y>Math.min(pn1.y-r, pn1.y+r)-panel.gbo){

			float dc = Float.MAX_VALUE;
			
			if((flags & Flags.SELECTED) != 0)
				dc = distPoint(pn1, p);

			float d = Math.abs(distPoint(pn1, p) - r);
			d = Math.min(d, dc);
			
			if(d < panel.gbo)
				dist = d;
		}
		
		return dist;			
	}
	
	@Override
	public void changeInitMove(PointF p)
	{
		pn1o.set(pn1);									// save orignal po1 point	
		flags &= ~Flags.CHANGEMASK;						// reset and set move flags
		flags |= Flags.MOVEOBJECT | Flags.HA_CENTER;
	}
	
	@Override
	public void changeObjectMove(PointF drag)
	{
		if((flags & Flags.MOVEOBJECT) != 0)
			moveObject(pn1, pn3, pn1o, drag, true);
	}	
	
	@Override
	public void changeObjectMoveMulti(PointF p, boolean init)
	{
	}
	
	@Override
	public void changeInitRotate(PointF center)
	{
	}
	
	@Override
	public void changeObjectRotate(float angle)
	{
	}
	
	/**
	 * move object line, circle
	 * @param tpg target grid
	 * @param tpn target nongrid
	 * @param sp source
	 * @param op offset screen
	 * @param snap
	 */
	private void moveObject(PointF tpg, PointF tpn, PointF sp, PointF op, boolean snap)	
	{
		float x = Math.round((sp.x - op.x / panel.gfx) / panel.grid.wx) * panel.grid.wx;
		float y = Math.round((sp.y - op.y / panel.gfy) / panel.grid.wy) * panel.grid.wy;	
		tpn.set(tpn.x-tpg.x+x, tpn.y-tpg.y+y);
		tpg.set(x, y);
	}
	
	@Override
	public void writePars(ObjectOutputStream oos) throws IOException
	{
	}
}
