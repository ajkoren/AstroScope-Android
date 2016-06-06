package com.astro.scope;

public class TransitData
{
	public String startDate;
	public String endDate;
	public int aspect;
	public int innerPlanet;
	public int outerPlanet;
	public double raInnerPlanet;
	public double raOuterPlanet;	
	
	public TransitData(String startDate, String endDate, int aspect, 
		int innerPlanet, int outerPlanet, double raInnerPlanet, double raOuterPlanet)
	{
		this.startDate = startDate;
		this.endDate = endDate;
		this.aspect = aspect;
		this.innerPlanet = innerPlanet;
		this.outerPlanet = outerPlanet;
		this.raInnerPlanet = raInnerPlanet;
		this.raOuterPlanet = raOuterPlanet;
	}

	public TransitData(TransitData transitData) {
		this.startDate = transitData.startDate;
		this.endDate = transitData.endDate;
		this.aspect = transitData.aspect;
		this.innerPlanet = transitData.innerPlanet;
		this.outerPlanet = transitData.outerPlanet;
		this.raInnerPlanet = transitData.raInnerPlanet;
		this.raOuterPlanet = transitData.raOuterPlanet;
	}
}

