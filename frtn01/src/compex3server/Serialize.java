package compex3server;

import java.util.StringTokenizer;

import se.lth.control.DoublePoint;

public class Serialize {
	// PIParameters
	public static String serialize(PIParameters p) {
		return p.K + " " + p.Ti + " " + p.Tr + " " + p.Beta + " " + p.H + " " + p.integratorOn;
	}
	
	public static PIParameters unserializePIParameters(String s) {
		PIParameters p = null;
		StringTokenizer tok = new StringTokenizer(s, " ");
		if (tok.countTokens() == 6) {
			try {
				p = new PIParameters();
				p.K = Double.parseDouble(tok.nextToken());
				p.Ti = Double.parseDouble(tok.nextToken());
				p.Tr = Double.parseDouble(tok.nextToken());
				p.Beta = Double.parseDouble(tok.nextToken());
				p.H = Double.parseDouble(tok.nextToken());
				p.integratorOn = Boolean.parseBoolean(tok.nextToken());
			} catch (Exception e) {
				System.out.println("Error: Exception in unserializePIParameters(): " + e.getMessage());
				p = null;
			}
		} else {
			System.out.println("Error: Invalid number of tokens in PIParameters string:");
			System.out.println("\"" + s + "\"");
		}
		return p;
	}
	
	// PIDParameters
	public static String serialize(PIDParameters p) {
		return p.K + " " + p.Ti + " " + p.Tr + " " + p.Td + " " + p.N + " " + p.Beta + " " + p.H + " " + p.integratorOn;
	}
	
	public static PIDParameters unserializePIDParameters(String s) {
		PIDParameters p = null;
		StringTokenizer tok = new StringTokenizer(s, " ");
		if (tok.countTokens() == 8) {
			try {
				p = new PIDParameters();
				p.K = Double.parseDouble(tok.nextToken());
				p.Ti = Double.parseDouble(tok.nextToken());
				p.Tr = Double.parseDouble(tok.nextToken());
				p.Td = Double.parseDouble(tok.nextToken());
				p.N = Double.parseDouble(tok.nextToken());
				p.Beta = Double.parseDouble(tok.nextToken());
				p.H = Double.parseDouble(tok.nextToken());
				p.integratorOn = Boolean.parseBoolean(tok.nextToken());
			} catch (Exception e) {
				System.out.println("Error: Exception in unserializePIDParameters(): " + e.getMessage());
				p = null;
			}
		} else {
			System.out.println("Error: Invalid number of tokens in PIDParameters string:");
			System.out.println("\"" + s + "\"");
		}
		return p;
	}
	
	// DoublePoint
	public static String serialize(DoublePoint dp) {
		return dp.x + " " + dp.y;
	}
	
	public static DoublePoint unserializeDoublePoint(String s) {
		DoublePoint dp = null;
		StringTokenizer tok = new StringTokenizer(s, " ");
		if (tok.countTokens() == 2) {
			try {
				dp = new DoublePoint();
				dp.x = Double.parseDouble(tok.nextToken());
				dp.y = Double.parseDouble(tok.nextToken());
			} catch (Exception e) {
				System.out.println("Error: Exception in unserializeDoublePoint(): " + e.getMessage());
				dp = null;
			}
		} else {
			System.out.println("Error: Invalid number of tokens in DoublePoint string:");
			System.out.println("\"" + s + "\"");
		}
		return dp;
	}
	
	// PlotData
	public static String serialize(PlotData pd) {
		return pd.x + " " + pd.y + " " + pd.yref;
	}
	
	public static PlotData unserializePlotData(String s) {
		PlotData pd = null;
		StringTokenizer tok = new StringTokenizer(s, " ");
		if (tok.countTokens() == 3) {
			try {
				double x = Double.parseDouble(tok.nextToken());
				double y = Double.parseDouble(tok.nextToken());
				double ref = Double.parseDouble(tok.nextToken());
				pd = new PlotData(x, ref, y);
			} catch (Exception e) {
				System.out.println("Error: Exception in unserializePlotData(): " + e.getMessage());
				pd = null;
			}
		} else {
			System.out.println("Error: Invalid number of tokens in PlotData string:");
			System.out.println("\"" + s + "\"");
		}
		return pd;
	}
}
