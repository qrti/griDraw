class DrawCircle extends DrawObject
{
	float r;
	
	DrawCircle(GraficPanel p, PointF sp)
	{
		super(p, Tool.DRAW_CIRCLE, sp);
	}	

	DrawCircle(GraficPanel p, ObjectInputStream ois) throws IOException
	{
		super(p, Tool.DRAW_CIRCLE, ois);
		r = distPoint(pn1, pn3);
	}
	
	public void setEnd(PointF e)
	{
		normPoint(pn3, e);
		r = distPoint(pn1, pn3);
	}
	
	@Override
	public void draw(Canvas canvas)
	{
		float hs = panel.hs;
		float p1x = pixel_x(pn1.x);
		float p1y = pixel_y(pn1.y);
		float p3x = pixel_x(pn3.x);
		float p3y = pixel_y(pn3.y);
		float sr = r * panel.gfx;
		
		if(Math.abs(sr) > hs){													// object
			canvas.drawCircle(p1x, p1y, sr, panel.conPaint);
		}																	
		else{																	// representation
			canvas.drawLine(p1x-hs, p1y-hs, p1x+hs, p1y+hs, panel.conPaint);	// cross
			canvas.drawLine(p1x-hs, p1y+hs, p1x+hs, p1y-hs, panel.conPaint);
			canvas.drawCircle(p1x, p1y, hs/2, panel.conPaint);					// circle
		}

		if((flags & Flags.SELECTED) != 0){												// handles
			canvas.drawLine(p1x, p1y-panel.CESI, p1x, p1y+panel.CESI, panel.conPaint);	// center
			canvas.drawLine(p1x-panel.CESI, p1y, p1x+panel.CESI, p1y, panel.conPaint);
			canvas.drawRect(p3x-hs, p3y-hs, p3x+hs, p3y+hs, panel.conPaint);			// radius
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
		pn1o.set(pn1);								// save orignal po1 and po3 point	
		pn3o.set(pn3);
		
		float dc = distPoint(pn1, p);					// calc distance to center handle
		float dr = distPoint(pn3, p);					//					radius 
		
		boolean mo = panel.sideBar.pointerMoveO();		// get move flags
		boolean mh = panel.sideBar.pointerMoveH();
		
		flags &= ~Flags.CHANGEMASK;						// reset and set move flags
		float g = panel.gbo;
		
		if(mo || (!mh && dc<g) || (!mh && dc>g && dr>g) || (!mh && dc<g && dr<g))
			flags |= dc<g ? Flags.MOVEOBJECT | Flags.HA_CENTER : Flags.MOVEOBJECT;
		else if(dr < g)
			flags |= Flags.MOVEHANDLE | Flags.HA_RADIUS;
	}
	
	@Override
	public void changeObjectMove(PointF drag)
	{
		if((flags & Flags.MOVEOBJECT) != 0){
			moveObject(pn1, pn3, pn1o, drag);
		}
		else if((flags & Flags.MOVEHANDLE) != 0){
			moveHandle(pn3, pn3o, drag);
			r = distPoint(pn1, pn3);
		}
	}	
	
	@Override
	public void changeInitRotate(PointF center)
	{
		
	}
	
	@Override
	public void changeObjectRotate(float angle)
	{
		
	}
	
	@Override
	public void writePars(ObjectOutputStream oos) throws IOException
	{
		writeParsC(oos);
	}
}