package com.example.tsbtoolsupreme;

public class HackItem {
	
	private String mPath="";
	private String mContent="";
	private String mDisplayString="";
	
	
	public HackItem(String path, String content){
		mPath = path;
		mContent = content;
		
		int pos = mPath.lastIndexOf("/");
		if( pos > -1)
		{
			mDisplayString = mPath.substring(pos+1);
		}
	}
	
	public String getPath(){ return mPath;}
	
	public String getContent(){ return mContent;}
	
	@Override
	public String toString(){
		return mDisplayString;
	}

}
