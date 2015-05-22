package de.qland.android.gridraw;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import android.graphics.Bitmap;
import android.graphics.PointF;

interface FeatFunc 
{
	public void execute(GraficPanel p, boolean sel);
	public void read(ObjectInputStream ois) throws IOException;
	public void write(ObjectOutputStream oos) throws IOException;
}

class PointerMoveO extends Feature
{
	PointerMoveO(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class PointerMoveH extends Feature
{
	PointerMoveH(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class PointerRotate extends Feature
{
	PointerRotate(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
	
	@Override
	public void execute(GraficPanel p, boolean sel)
	{
		p.conRotCent(sel);
	}
}

class PointerProperty extends Feature
{
	PointerProperty(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class PointerDelete extends Feature
{
	PointerDelete(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
	
	@Override 
	public void execute(GraficPanel p, boolean sel)
	{
		p.deleteObject(sel);
		
		if(sel){
			new Timer().schedule(new TimerTask() {          
			    @Override
			    public void run() {
			    	sideBar.featPointer.get(4).selected = false;    
			    }
			}, 500);
		}
	}
}

class PointerMultisel extends Feature
{
	PointerMultisel(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class PointerCentCon extends Feature
{
	PointerCentCon(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class ChangeGridProperty extends Feature
{
	ChangeGridProperty(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class LineProperty extends Feature
{
	LineProperty(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class RectProperty extends Feature
{
	RectProperty(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class RectCentCon extends Feature
{
	RectCentCon(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class ArcProperty extends Feature
{
	ArcProperty(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

class ArcCentCon extends Feature
{
	ArcCentCon(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		super(sb, u, s, p, m);
	}
}

