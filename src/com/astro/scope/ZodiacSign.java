package com.astro.scope;

public class ZodiacSign {
	public String name;
	public int zodiacSignNum;
	public int zodiacSignDeg;
	public int raSignInt;
	public int raSignMin;
	
	public static final String[] names = {
	    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", 
	    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
	};

	public ZodiacSign(double raDeg)
   	{
   		zodiacSignNum = (int) Math.floor(raDeg / 30);
   		zodiacSignDeg = zodiacSignNum * 30;
   		raSignInt = (int) Math.floor(raDeg - zodiacSignDeg);
   		raSignMin = (int) (60 * (raDeg - zodiacSignDeg - raSignInt));
   		
   		this.name = names[zodiacSignNum];
   	}
	
	// Get the zodiac number in which the planet with the given RA is
	public int getZodiacNum() {
		return zodiacSignNum;
	}
 
}