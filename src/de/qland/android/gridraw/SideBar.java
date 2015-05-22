package de.qland.android.gridraw;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

final class IcPos
{
	public static int POINTER_MOVEO;
	public static int POINTER_MOVEH;
	public static int POINTER_ROTATE;
	public static int POINTER_PROPERTY;
	public static int POINTER_DELETE;
	public static int POINTER_MULTISEL;
	public static int POINTER_CENTCON;
	public static int CHANGEGRID_PROPERTY;
	public static int LINE_PROPERTY;
	public static int RECT_PROPERTY;
	public static int RECT_CENTCON;
	public static int ARC_PROPERTY;
	public static int ARC_CENTCON;
}

abstract class Feature implements FeatFunc
{
	public SideBar sideBar;
	public Bitmap bmUnsel;
	public Bitmap bmSel;
	public boolean selected;
	public PointF pos;
	public int mutex[];
	
	Feature(SideBar sb, Bitmap u, Bitmap s, PointF p, int[] m)
	{
		sideBar = sb;
		bmUnsel = u;
		bmSel = s;
		pos = p;
		mutex = m;
	}
	
	public void execute(GraficPanel p, boolean sel)
	{
	}
	
	public void read(ObjectInputStream ois) throws IOException
	{
		selected = ois.readBoolean();
	}
	
	public void write(ObjectOutputStream oos) throws IOException
	{
		oos.writeBoolean(selected);
	}
}

public class SideBar 
{
	private final float SPEED = 2f;	
	private int touch = Touch.NONE;
	private float drawLeftStart;
	private GraficPanel panel;
	private float sbWidth;
	private float icLeft, icTop, icDy, icTmy, icTpy;
		
	public ArrayList<Feature> featPointer    = new ArrayList<Feature>();	// feature lists for tools
	public ArrayList<Feature> featChangeGrid = new ArrayList<Feature>();
	public ArrayList<Feature> featDrawLine   = new ArrayList<Feature>();
	public ArrayList<Feature> featDrawRect   = new ArrayList<Feature>();
	public ArrayList<Feature> featDrawArc    = new ArrayList<Feature>();
	public ArrayList<Feature> featDrawFree   = new ArrayList<Feature>();
	public ArrayList<Feature> featDrawText   = new ArrayList<Feature>();
	
	public ArrayList<ArrayList<Feature>> toolCol = new ArrayList<ArrayList<Feature>>();	// tool collection
	public ArrayList<Feature> curFeats;			// current tool features
	
	SideBar(Context context, GraficPanel p)
	{	
		panel = p;
		
		Bitmap bm_moveo 	   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_moveo);
		Bitmap bm_moveo_sel    = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_moveo_sel);
		Bitmap bm_moveh 	   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_moveh);
		Bitmap bm_moveh_sel    = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_moveh_sel);
		Bitmap bm_rotate 	   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_rotate);
		Bitmap bm_rotate_sel   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_rotate_sel);
		Bitmap bm_property 	   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_property);
		Bitmap bm_property_sel = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_property_sel);
		Bitmap bm_delete 	   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_delete);
		Bitmap bm_delete_sel   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_delete_sel);
		Bitmap bm_multisel 	   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_multisel);
		Bitmap bm_multisel_sel = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_multisel_sel);
		Bitmap bm_centcon 	   = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_centcon);
		Bitmap bm_centcon_sel  = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_centcon_sel);

		float bmSize = bm_moveo.getWidth();
		sbWidth = bmSize * 1.5f;
		icLeft =  bmSize * 1.25f;
		icTop = bmSize * 0.25f;
		icDy = bmSize * 1.75f;
		icTmy = (icDy - bmSize) / 2;
		icTpy = icTmy + bmSize;
		
		// add features to tool pointer
		IcPos.POINTER_MOVEO    = featPointer.size(); featPointer.add(new PointerMoveO(this,    bm_moveo,    bm_moveo_sel,    new PointF(icLeft, icTop),        new int[]{1, 2, 3, 4}));
		IcPos.POINTER_MOVEH    = featPointer.size(); featPointer.add(new PointerMoveH(this,    bm_moveh,    bm_moveh_sel,    new PointF(icLeft, icTop+icDy),   new int[]{0, 2, 3, 4}));
		IcPos.POINTER_ROTATE   = featPointer.size(); featPointer.add(new PointerRotate(this,   bm_rotate,   bm_rotate_sel,   new PointF(icLeft, icTop+2*icDy), new int[]{0, 1, 3, 4}));
		IcPos.POINTER_PROPERTY = featPointer.size(); featPointer.add(new PointerProperty(this, bm_property, bm_property_sel, new PointF(icLeft, icTop+3*icDy), new int[]{0, 1, 2, 4}));
		IcPos.POINTER_DELETE   = featPointer.size(); featPointer.add(new PointerDelete(this,   bm_delete,   bm_delete_sel,   new PointF(icLeft, icTop+4*icDy), null));
		IcPos.POINTER_MULTISEL = featPointer.size(); featPointer.add(new PointerMultisel(this, bm_multisel, bm_multisel_sel, new PointF(icLeft, icTop+5*icDy), null));
		IcPos.POINTER_CENTCON  = featPointer.size(); featPointer.add(new PointerCentCon(this,  bm_centcon,  bm_centcon_sel,  new PointF(icLeft, icTop+6*icDy), null));

		// add features to tool change grid
		IcPos.CHANGEGRID_PROPERTY = featChangeGrid.size(); featChangeGrid.add(new ChangeGridProperty(this, bm_property, bm_property_sel, new PointF(icLeft, icTop), null));
		
		// add features to tool draw line
		IcPos.LINE_PROPERTY = featDrawLine.size(); featDrawLine.add(new LineProperty(this, bm_property, bm_property_sel, new PointF(icLeft, icTop), null));

		// add features to tool draw rect
		IcPos.RECT_PROPERTY = featDrawRect.size(); featDrawRect.add(new RectProperty(this, bm_property, bm_property_sel, new PointF(icLeft, icTop),      null));
		IcPos.RECT_CENTCON  = featDrawRect.size(); featDrawRect.add(new RectCentCon(this,  bm_centcon,  bm_centcon_sel,  new PointF(icLeft, icTop+icDy), null));
		
		// add features to tool draw arc
		IcPos.ARC_PROPERTY = featDrawArc.size(); featDrawArc.add(new ArcProperty(this, bm_property, bm_property_sel, new PointF(icLeft, icTop),      null));
		IcPos.ARC_CENTCON  = featDrawArc.size(); featDrawArc.add(new ArcCentCon(this,  bm_centcon,  bm_centcon_sel,  new PointF(icLeft, icTop+icDy), null));
		
		toolCol.add(featPointer);		// add feature lists to tool collection
		toolCol.add(featChangeGrid);
		toolCol.add(featDrawLine);
		toolCol.add(featDrawRect);
		toolCol.add(featDrawArc);
		toolCol.add(featDrawFree);
		toolCol.add(featDrawText);
	}

	public boolean pointerMoveO()
	{
		return featPointer.get(IcPos.POINTER_MOVEO).selected;
	}
	
	public boolean pointerMoveH()
	{
		return featPointer.get(IcPos.POINTER_MOVEH).selected;
	}
	
	public boolean pointerRotate()
	{
		return featPointer.get(IcPos.POINTER_ROTATE).selected;
	}
	
	public void pointerRotateUnsel()
	{
		featPointer.get(IcPos.POINTER_ROTATE).selected = false;
	}
	
	public boolean pointerMultisel()
	{
		return featPointer.get(IcPos.POINTER_MULTISEL).selected;
	}
	
	public boolean pointerCentCon()
	{
		return featPointer.get(IcPos.POINTER_CENTCON).selected;
	}
	
	public boolean rectCentCon()
	{
		return featDrawRect.get(IcPos.RECT_CENTCON).selected;
	}
	
	public boolean arcCentCon()
	{
		return featDrawArc.get(IcPos.ARC_CENTCON).selected;
	}
	
	public void setTool(Tool a)
	{
		switch(a){
			case POINTER:
				curFeats = featPointer;
				break;
				
			case CHANGE_GRID:
				curFeats = featChangeGrid;
				break;
			
			case DRAW_LINE:
				curFeats = featDrawLine;
				break;	

			case DRAW_RECT:
				curFeats = featDrawRect;
				break;	
				
			case DRAW_ARC:
				curFeats = featDrawArc;
				break;	
				
			case DRAW_FREE:
				curFeats = featDrawFree;
				break;	
				
			case DRAW_TEXT:
				curFeats = featDrawText;
				break;	
				
			default:
				break;
		}
	}
	
	public Boolean touched()
	{
		Boolean rv = false;
		
		if(panel.touchStart.x>panel.draw.left-panel.pmX*5 && panel.touchStart.x<panel.draw.left+panel.pmX*5){
			rv = true;
			
			if(touch==Touch.NONE && panel.touchMode==Touch.DRAG){
				touch = Touch.DRAG;
				drawLeftStart = panel.draw.left;
			}
		}
		
		if(touch==Touch.DRAG || touch==Touch.AUTO){
			rv = true;
			
			if(panel.touchMode==Touch.DRAG && touch==Touch.DRAG){
				panel.draw.left = drawLeftStart - panel.touchDrag.x;
				if(panel.draw.left < 0) panel.draw.left = 0;
				if(panel.draw.left > sbWidth) panel.draw.left = sbWidth;
				panel.left = panel.draw.left + panel.drawGrid.left;
			}
			else{
				if(panel.draw.left==0 || panel.draw.left==sbWidth){
					touch = Touch.NONE;
				}
				else{
					touch = Touch.AUTO;
					panel.draw.left += panel.draw.left>sbWidth/2 ? SPEED : -SPEED;
					if(panel.draw.left < 0) panel.draw.left = 0;
					if(panel.draw.left > sbWidth) panel.draw.left = sbWidth;		
					panel.left = panel.draw.left + panel.drawGrid.left;
				}
			}
		}
		else if(touch==Touch.NONE && panel.touchMode==Touch.TAP && panel.draw.left==sbWidth && panel.touchStart.x<panel.draw.left){			// sidebar touch
			rv = true;
			Feature newsel = null;
			
			for(Feature a : curFeats)
				if(panel.touchStart.y>a.pos.y-icTmy && panel.touchStart.y<a.pos.y+icTpy)
					newsel = a;
			
			if(newsel != null){
				newsel.selected = !newsel.selected;
			
				if(newsel.selected && newsel.mutex!=null){
					for(int i : newsel.mutex){
						Feature f = curFeats.get(i);
						
						if(f.selected){
							f.selected = false;
							f.execute(panel, false);
						}
					}
				}
				
				newsel.execute(panel, newsel.selected);
			}
			
			touch = Touch.TAP;
		}
		else if(touch==Touch.TAP && panel.touchMode==Touch.NONE){
			touch = Touch.NONE;
		}
		
		return rv;
	}
}

















