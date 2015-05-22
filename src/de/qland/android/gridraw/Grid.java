package de.qland.android.gridraw;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Grid
{
	public float left, top, width, height, wx, wy;
	
	public void copy(Grid source)
	{
		left = source.left;
		top = source.top;
		width = source.width;
		height = source.height;
		wx = source.wx;
		wy = source.wy;
	}
	
	public float[] getData()
	{
		return new float[] {left, top, width, height, wx, wy};
	}
	
	public void setData(float[] data)
	{
		left= data[0];
		top = data[1];
		width = data[2];
		height = data[3];
		wx = data[4];
		wy = data[5];
	}
	
    public void writeObject(ObjectOutputStream o) throws IOException 
    {  
        o.writeFloat(left);  
        o.writeFloat(top); 
        o.writeFloat(width); 
        o.writeFloat(height); 
        o.writeFloat(wx); 
        o.writeFloat(wy); 
    }
    
    public void readObject(ObjectInputStream o) throws IOException 
    {  
        left = o.readFloat();  
        top = o.readFloat(); 
        width = o.readFloat(); 
        height = o.readFloat(); 
        wx = o.readFloat(); 
        wy = o.readFloat(); 
    }
}


