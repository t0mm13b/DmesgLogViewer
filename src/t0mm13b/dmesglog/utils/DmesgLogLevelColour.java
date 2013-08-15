package t0mm13b.dmesglog.utils;

public class DmesgLogLevelColour{
	private DmesgLogLevelsEnum mEnumLogLevel;
	private int mIntColour;
	private String mStrColour;
	private String mStrKey;
	public DmesgLogLevelColour(DmesgLogLevelsEnum enumLogLevel, int iColour, String strColour){
		this.mEnumLogLevel = enumLogLevel;
		this.mIntColour = iColour;
		this.mStrColour = strColour;
		this.mStrKey = String.format("KLogColour: [%s]; %d; %s", this.mEnumLogLevel.name(), this.mIntColour, this.mStrColour);
	}
	public int getColour(){
		return this.mIntColour;
	}
	public String getHexColour(){
		return this.mStrColour;
	}
	public int hashCode(){
		return this.mStrKey.hashCode();
	}
	public boolean equals(Object obj){
		if (obj == null) return false;
		if (!(obj instanceof DmesgLine)) return false;
		if (obj.getClass() != this.getClass()) return false;
		DmesgLogLevelColour rhs = (DmesgLogLevelColour)obj;
		if (rhs != null){
			if (this.hashCode() == rhs.hashCode()) return true;
		}
		return false;
	}
	public String toString(){
		return this.mStrKey;
	}
}