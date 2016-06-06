package com.astro.scope;

public class SwissEphConst {

    public static int planetIndexToSwephIndex(int planetIndex) {
    	int swephIndex = 0;
    	
    	switch (planetIndex) {
    	case 0:
    		swephIndex = swisseph.SweConst.SE_SUN;
    		break;
    	case 1:
    		swephIndex = swisseph.SweConst.SE_MOON;
    		break;
    	case 2:
    		swephIndex = swisseph.SweConst.SE_MERCURY;
    		break;
    	case 3:
    		swephIndex = swisseph.SweConst.SE_VENUS;
    		break;
    	case 4:
    		swephIndex = swisseph.SweConst.SE_MARS;
    		break;
    	case 5:
    		swephIndex = swisseph.SweConst.SE_JUPITER;
    		break;
    	case 6:
    		swephIndex = swisseph.SweConst.SE_SATURN;
    		break;
    	case 7:
    		swephIndex = swisseph.SweConst.SE_URANUS;
    		break;
    	case 8:
    		swephIndex = swisseph.SweConst.SE_NEPTUNE;
    		break;
    	case 9:
    		swephIndex = swisseph.SweConst.SE_PLUTO;
    		break;
    	case 10:
    		swephIndex = swisseph.SweConst.SE_MEAN_NODE;
    		break;
    	case 11:
    		swephIndex = swisseph.SweConst.SE_TRUE_NODE;
    		break;
    	case 12:
    		swephIndex = swisseph.SweConst.SE_MEAN_APOG;
    		break;
    	}
    	return swephIndex;
    }
    
}
