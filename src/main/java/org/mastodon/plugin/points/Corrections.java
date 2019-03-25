package org.mastodon.plugin.points;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Corrections
{
	public Corrections(final int ID)
	{
		switch (ID)
		{
			case 1:
				init01();
				break;
			case 2:
				init02();
				break;
			default:
		}
	}

	//data holder: time -> [[ x,y,z ]]
	HashMap<Integer, Vector<int[]>> corr = new HashMap<>(5);

	HashSet<Integer> usedSTpos = new HashSet<>(1000);

	public
	int suggestZ(final int time, final int x, final int y)
	{
		if (corr.containsKey(time) == false) return -1;
		Vector<int[]> v = corr.get(time);

		for (int i=0; i < v.size(); ++i)
		if (v.get(i)[0] == x && v.get(i)[1] == y)
		{
			int code = time << 20 | x << 11 | y;
			if (usedSTpos.contains(code))
				System.out.println("Reusing time="+time+", x="+x+", y="+y);
			else
				usedSTpos.add(code);

			return v.get(i)[2];
		}

		return -1;
	}

	public
	boolean isHitXY(final int time, final int x, final int y)
	{
		if (corr.containsKey(time) == false) return false;
		Vector<int[]> v = corr.get(time);

		for (int i=0; i < v.size(); ++i)
		if (v.get(i)[0] == x && v.get(i)[1] == y)
		{
			int code = time << 20 | x << 11 | y;
			if (usedSTpos.contains(code))
				System.out.println("Reusing time="+time+", x="+x+", y="+y);
			else
				usedSTpos.add(code);

			return true;
		}

		return false;
	}

	public
	Set<Integer> listTimePoints()
	{
		return corr.keySet();
	}

	public
	void reportUnusedCorrections()
	{
		for (Integer time : corr.keySet())
		{
			Vector<int[]> v = corr.get(time);
			for (int i=0; i < v.size(); ++i)
			{
				int x = v.get(i)[0];
				int y = v.get(i)[1];
				int z = v.get(i)[2];

				int code = time << 20 | x << 11 | y;
				if (!usedSTpos.contains(code))
					System.out.println("Not used time="+time+", x="+x+", y="+y+", newz="+z);
			}
		}
	}

	public static void main(final String... args)
	{
		final Corrections c = new Corrections(1);
		System.out.println(c.suggestZ(4, 40, 20));
		System.out.println(c.suggestZ(31, 550, 1168));
	}

	//--------------- 01 ---------------
	private
	void init01()
	{
		/*
		Vector<int[]> v = new Vector<>(80);
		v.add(new int[] {370,376,12});
		v.add(new int[] {368,419,12});
		v.add(new int[] {304,451,12});
		v.add(new int[] {353,459,12});
		v.add(new int[] {325,500,12});
		v.add(new int[] {354,522,12});
		v.add(new int[] {269,530,12});
		v.add(new int[] {336,551,12});
		v.add(new int[] {312,565,12});
		v.add(new int[] {333,588,12});
		v.add(new int[] {286,595,12});
		v.add(new int[] {312,604,12});
		v.add(new int[] {266,620,12});
		v.add(new int[] {302,638,12});
		v.add(new int[] {290,663,12});
		v.add(new int[] {334,669,12});
		v.add(new int[] {311,694,12});
		v.add(new int[] {343,702,12});
		v.add(new int[] {367,722,12});
		v.add(new int[] {304,739,12});
		v.add(new int[] {349,751,12});
		v.add(new int[] {327,773,12});
		v.add(new int[] {357,779,12});
		v.add(new int[] {391,785,12});
		v.add(new int[] {382,814,12});
		v.add(new int[] {407,837,12});
		v.add(new int[] {432,844,12});
		v.add(new int[] {387,853,12});
		v.add(new int[] {461,857,12});
		v.add(new int[] {494,859,12});
		v.add(new int[] {404,876,12});
		v.add(new int[] {479,884,12});
		v.add(new int[] {437,889,12});
		v.add(new int[] {552,901,12});
		v.add(new int[] {511,905,12});
		v.add(new int[] {490,911,12});
		v.add(new int[] {579,918,12});
		v.add(new int[] {638,921,12});
		v.add(new int[] {667,934,12});
		v.add(new int[] {548,938,12});
		v.add(new int[] {521,946,12});
		v.add(new int[] {621,946,12});
		v.add(new int[] {710,946,12});
		v.add(new int[] {588,947,12});
		v.add(new int[] {651,953,12});
		v.add(new int[] {688,960,12});
		v.add(new int[] {569,965,12});
		v.add(new int[] {741,966,12});
		v.add(new int[] {711,974,12});
		v.add(new int[] {805,980,12});
		v.add(new int[] {600,982,12});
		v.add(new int[] {891,989,12});
		v.add(new int[] {656,990,12});
		v.add(new int[] {779,992,12});
		v.add(new int[] {685,994,12});
		v.add(new int[] {744,996,12});
		v.add(new int[] {852,997,12});
		v.add(new int[] {992,1007,12});
		v.add(new int[] {825,1011,12});
		v.add(new int[] {924,1015,12});
		v.add(new int[] {718,1017,12});
		v.add(new int[] {757,1017,12});
		v.add(new int[] {790,1019,12});
		v.add(new int[] {1030,1026,12});
		v.add(new int[] {1079,1026,12});
		v.add(new int[] {951,1031,12});
		v.add(new int[] {1122,1031,12});
		v.add(new int[] {849,1032,12});
		v.add(new int[] {883,1032,12});
		v.add(new int[] {990,1042,12});
		v.add(new int[] {919,1048,12});
		v.add(new int[] {789,1051,12});
		v.add(new int[] {823,1052,12});
		v.add(new int[] {1062,1061,12});
		v.add(new int[] {1100,1062,12});
		v.add(new int[] {1131,1066,12});
		v.add(new int[] {1227,1068,12});
		v.add(new int[] {998,1080,12});
		v.add(new int[] {1164,1083,12});
		v.add(new int[] {1037,1087,12});
		v.add(new int[] {1199,1099,12});
		v.add(new int[] {1258,1100,12});
		v.add(new int[] {1074,1101,12});
		v.add(new int[] {1108,1103,12});
		v.add(new int[] {1228,1112,12});
		v.add(new int[] {1142,1114,12});
		v.add(new int[] {1198,1133,12});
		v.add(new int[] {1168,1134,12});
		v.add(new int[] {1236,1148,12});
		v.add(new int[] {1267,1150,12});
		v.add(new int[] {1299,1160,12});
		v.add(new int[] {1334,1193,12});
		v.add(new int[] {1296,1196,12});
		v.add(new int[] {1364,1224,12});
		v.add(new int[] {1334,1238,12});
		v.add(new int[] {1297,1242,12});
		v.add(new int[] {1331,1276,12});
		v.add(new int[] {1298,1287,12});
		v.add(new int[] {1356,1316,12});
		v.add(new int[] {1321,1321,12});
		v.add(new int[] {1290,1329,12});
		v.add(new int[] {1331,1366,12});
		v.add(new int[] {1303,1369,12});
		corr.put(5, v);

		v = new Vector<>(80);
		v.add(new int[] {368,375,12});
		v.add(new int[] {341,394,12});
		v.add(new int[] {303,399,12});
		v.add(new int[] {368,419,12});
		v.add(new int[] {330,429,12});
		v.add(new int[] {305,450,12});
		v.add(new int[] {352,458,12});
		v.add(new int[] {282,492,12});
		v.add(new int[] {328,498,12});
		v.add(new int[] {354,523,12});
		v.add(new int[] {271,529,12});
		v.add(new int[] {308,534,12});
		v.add(new int[] {338,552,12});
		v.add(new int[] {316,564,12});
		v.add(new int[] {333,587,12});
		v.add(new int[] {286,595,12});
		v.add(new int[] {312,604,12});
		v.add(new int[] {342,620,12});
		v.add(new int[] {266,624,12});
		v.add(new int[] {304,635,12});
		v.add(new int[] {291,663,12});
		v.add(new int[] {334,666,12});
		v.add(new int[] {311,696,12});
		v.add(new int[] {346,702,12});
		v.add(new int[] {368,722,12});
		v.add(new int[] {303,740,12});
		v.add(new int[] {351,749,12});
		v.add(new int[] {385,752,12});
		v.add(new int[] {326,770,12});
		v.add(new int[] {416,772,12});
		v.add(new int[] {358,778,12});
		v.add(new int[] {394,784,12});
		v.add(new int[] {425,809,12});
		v.add(new int[] {383,814,12});
		v.add(new int[] {476,830,12});
		v.add(new int[] {410,831,12});
		v.add(new int[] {433,844,12});
		v.add(new int[] {390,850,12});
		v.add(new int[] {464,855,12});
		v.add(new int[] {493,856,12});
		v.add(new int[] {406,872,12});
		v.add(new int[] {481,880,12});
		v.add(new int[] {439,888,12});
		v.add(new int[] {550,899,12});
		v.add(new int[] {512,902,12});
		v.add(new int[] {489,909,12});
		v.add(new int[] {579,911,12});
		v.add(new int[] {611,915,12});
		v.add(new int[] {638,917,12});
		v.add(new int[] {536,918,12});
		v.add(new int[] {669,928,12});
		v.add(new int[] {551,934,12});
		v.add(new int[] {622,941,12});
		v.add(new int[] {520,943,12});
		v.add(new int[] {589,943,12});
		v.add(new int[] {714,943,12});
		v.add(new int[] {653,948,12});
		v.add(new int[] {687,958,12});
		v.add(new int[] {569,962,12});
		v.add(new int[] {746,965,12});
		v.add(new int[] {631,968,12});
		v.add(new int[] {715,973,12});
		v.add(new int[] {599,978,12});
		v.add(new int[] {804,978,12});
		v.add(new int[] {894,982,12});
		v.add(new int[] {654,985,12});
		v.add(new int[] {687,989,12});
		v.add(new int[] {773,989,12});
		v.add(new int[] {958,992,12});
		v.add(new int[] {745,995,12});
		v.add(new int[] {853,995,12});
		v.add(new int[] {992,1000,12});
		v.add(new int[] {825,1008,12});
		v.add(new int[] {925,1013,12});
		v.add(new int[] {718,1017,12});
		v.add(new int[] {792,1018,12});
		v.add(new int[] {755,1019,12});
		v.add(new int[] {1077,1024,12});
		v.add(new int[] {1026,1026,12});
		v.add(new int[] {1112,1027,12});
		v.add(new int[] {951,1028,12});
		v.add(new int[] {854,1029,12});
		v.add(new int[] {885,1032,12});
		v.add(new int[] {990,1040,12});
		v.add(new int[] {919,1047,12});
		v.add(new int[] {827,1048,12});
		v.add(new int[] {790,1049,12});
		v.add(new int[] {1062,1059,12});
		v.add(new int[] {1101,1062,12});
		v.add(new int[] {1129,1064,12});
		v.add(new int[] {1226,1067,12});
		v.add(new int[] {994,1078,12});
		v.add(new int[] {1165,1081,12});
		v.add(new int[] {1038,1085,12});
		v.add(new int[] {1198,1094,12});
		v.add(new int[] {1078,1095,12});
		v.add(new int[] {1261,1097,12});
		v.add(new int[] {1108,1103,12});
		v.add(new int[] {1228,1108,12});
		v.add(new int[] {1141,1114,12});
		v.add(new int[] {1198,1132,12});
		v.add(new int[] {1168,1136,12});
		v.add(new int[] {1267,1146,12});
		v.add(new int[] {1232,1150,12});
		v.add(new int[] {1298,1156,12});
		v.add(new int[] {1335,1193,12});
		v.add(new int[] {1296,1195,12});
		v.add(new int[] {1366,1224,12});
		v.add(new int[] {1334,1238,12});
		v.add(new int[] {1301,1241,12});
		v.add(new int[] {1330,1276,12});
		v.add(new int[] {1302,1288,12});
		v.add(new int[] {1360,1315,12});
		v.add(new int[] {1323,1319,12});
		v.add(new int[] {1289,1330,12});
		v.add(new int[] {1305,1369,12});
		corr.put(15, v);

		v = new Vector<>(80);
		v.add(new int[] {326,359,12});
		v.add(new int[] {344,395,12});
		v.add(new int[] {306,399,12});
		v.add(new int[] {368,421,12});
		v.add(new int[] {309,453,12});
		v.add(new int[] {355,456,12});
		v.add(new int[] {354,492,12});
		v.add(new int[] {287,494,12});
		v.add(new int[] {333,498,12});
		v.add(new int[] {362,519,12});
		v.add(new int[] {272,530,12});
		v.add(new int[] {309,532,12});
		v.add(new int[] {345,546,12});
		v.add(new int[] {317,568,12});
		v.add(new int[] {338,589,12});
		v.add(new int[] {295,594,12});
		v.add(new int[] {316,607,12});
		v.add(new int[] {276,624,12});
		v.add(new int[] {312,635,12});
		v.add(new int[] {333,664,12});
		v.add(new int[] {300,666,12});
		v.add(new int[] {319,696,12});
		v.add(new int[] {351,702,12});
		v.add(new int[] {374,714,12});
		v.add(new int[] {312,737,12});
		v.add(new int[] {355,745,12});
		v.add(new int[] {390,748,12});
		v.add(new int[] {336,768,12});
		v.add(new int[] {362,771,12});
		v.add(new int[] {421,775,12});
		v.add(new int[] {397,781,12});
		v.add(new int[] {428,808,12});
		v.add(new int[] {390,810,12});
		v.add(new int[] {415,828,12});
		v.add(new int[] {441,846,12});
		v.add(new int[] {394,848,12});
		v.add(new int[] {466,853,12});
		v.add(new int[] {495,854,12});
		v.add(new int[] {415,874,12});
		v.add(new int[] {486,877,12});
		v.add(new int[] {446,887,12});
		v.add(new int[] {554,894,12});
		v.add(new int[] {491,905,12});
		v.add(new int[] {583,908,12});
		v.add(new int[] {614,910,12});
		v.add(new int[] {639,911,12});
		v.add(new int[] {537,915,12});
		v.add(new int[] {674,926,12});
		v.add(new int[] {557,932,12});
		v.add(new int[] {526,935,12});
		v.add(new int[] {623,937,12});
		v.add(new int[] {591,938,12});
		v.add(new int[] {715,940,12});
		v.add(new int[] {654,947,12});
		v.add(new int[] {689,952,12});
		v.add(new int[] {570,959,12});
		v.add(new int[] {631,961,12});
		v.add(new int[] {748,962,12});
		v.add(new int[] {715,966,12});
		v.add(new int[] {805,970,12});
		v.add(new int[] {602,975,12});
		v.add(new int[] {895,976,12});
		v.add(new int[] {780,982,12});
		v.add(new int[] {656,983,12});
		v.add(new int[] {960,984,12});
		v.add(new int[] {687,987,12});
		v.add(new int[] {855,989,12});
		v.add(new int[] {996,989,12});
		v.add(new int[] {745,992,12});
		v.add(new int[] {823,1003,12});
		v.add(new int[] {925,1006,12});
		v.add(new int[] {795,1011,12});
		v.add(new int[] {722,1012,12});
		v.add(new int[] {760,1015,12});
		v.add(new int[] {1081,1015,12});
		v.add(new int[] {1028,1019,12});
		v.add(new int[] {1120,1019,12});
		v.add(new int[] {857,1024,12});
		v.add(new int[] {955,1024,12});
		v.add(new int[] {891,1027,12});
		v.add(new int[] {996,1033,12});
		v.add(new int[] {790,1044,12});
		v.add(new int[] {921,1044,12});
		v.add(new int[] {825,1045,12});
		v.add(new int[] {1099,1054,12});
		v.add(new int[] {1132,1060,12});
		v.add(new int[] {1232,1063,12});
		v.add(new int[] {1164,1075,12});
		v.add(new int[] {1003,1076,12});
		v.add(new int[] {1041,1076,12});
		v.add(new int[] {1210,1077,12});
		v.add(new int[] {1263,1093,12});
		v.add(new int[] {1188,1094,12});
		v.add(new int[] {1071,1097,12});
		v.add(new int[] {1110,1097,12});
		v.add(new int[] {1143,1108,12});
		v.add(new int[] {1231,1110,12});
		v.add(new int[] {1171,1117,12});
		v.add(new int[] {1202,1133,12});
		v.add(new int[] {1269,1141,12});
		v.add(new int[] {1167,1150,12});
		v.add(new int[] {1233,1151,12});
		v.add(new int[] {1297,1166,12});
		v.add(new int[] {1301,1192,12});
		v.add(new int[] {1336,1192,12});
		v.add(new int[] {1305,1237,12});
		v.add(new int[] {1334,1239,12});
		v.add(new int[] {1331,1276,12});
		v.add(new int[] {1303,1287,12});
		v.add(new int[] {1359,1316,12});
		v.add(new int[] {1293,1329,12});
		v.add(new int[] {1306,1369,12});
		corr.put(33, v);

		v = new Vector<>(80);
		v.add(new int[] {336,363,12});
		v.add(new int[] {379,383,12});
		v.add(new int[] {316,404,12});
		v.add(new int[] {364,419,12});
		v.add(new int[] {397,432,12});
		v.add(new int[] {345,436,12});
		v.add(new int[] {325,460,12});
		v.add(new int[] {304,491,12});
		v.add(new int[] {340,499,12});
		v.add(new int[] {366,499,12});
		v.add(new int[] {365,521,12});
		v.add(new int[] {320,524,12});
		v.add(new int[] {366,563,12});
		v.add(new int[] {324,570,12});
		v.add(new int[] {347,585,12});
		v.add(new int[] {301,594,12});
		v.add(new int[] {328,600,12});
		v.add(new int[] {290,621,12});
		v.add(new int[] {314,628,12});
		v.add(new int[] {350,644,12});
		v.add(new int[] {311,664,12});
		v.add(new int[] {364,666,12});
		v.add(new int[] {376,691,12});
		v.add(new int[] {322,696,12});
		v.add(new int[] {400,696,12});
		v.add(new int[] {349,697,12});
		v.add(new int[] {406,722,12});
		v.add(new int[] {381,723,12});
		v.add(new int[] {330,735,12});
		v.add(new int[] {360,739,12});
		v.add(new int[] {385,749,12});
		v.add(new int[] {450,751,12});
		v.add(new int[] {416,753,12});
		v.add(new int[] {344,763,12});
		v.add(new int[] {383,768,12});
		v.add(new int[] {367,770,12});
		v.add(new int[] {422,770,12});
		v.add(new int[] {483,791,12});
		v.add(new int[] {427,800,12});
		v.add(new int[] {397,801,12});
		v.add(new int[] {460,811,12});
		v.add(new int[] {417,818,12});
		v.add(new int[] {480,829,12});
		v.add(new int[] {447,833,12});
		v.add(new int[] {405,844,12});
		v.add(new int[] {471,849,12});
		v.add(new int[] {536,856,12});
		v.add(new int[] {514,865,12});
		v.add(new int[] {424,867,12});
		v.add(new int[] {484,873,12});
		v.add(new int[] {625,881,12});
		v.add(new int[] {452,882,12});
		v.add(new int[] {587,883,12});
		v.add(new int[] {659,883,12});
		v.add(new int[] {551,887,12});
		v.add(new int[] {522,888,12});
		v.add(new int[] {494,899,12});
		v.add(new int[] {607,901,12});
		v.add(new int[] {650,901,12});
		v.add(new int[] {727,903,12});
		v.add(new int[] {564,906,12});
		v.add(new int[] {539,910,12});
		v.add(new int[] {680,913,12});
		v.add(new int[] {697,919,12});
		v.add(new int[] {624,920,12});
		v.add(new int[] {656,924,12});
		v.add(new int[] {559,926,12});
		v.add(new int[] {676,929,12});
		v.add(new int[] {596,932,12});
		v.add(new int[] {529,933,12});
		v.add(new int[] {742,935,12});
		v.add(new int[] {738,944,12});
		v.add(new int[] {573,950,12});
		v.add(new int[] {712,950,12});
		v.add(new int[] {637,952,12});
		v.add(new int[] {691,952,12});
		v.add(new int[] {805,956,12});
		v.add(new int[] {940,958,12});
		v.add(new int[] {606,961,12});
		v.add(new int[] {665,965,12});
		v.add(new int[] {923,967,12});
		v.add(new int[] {898,975,12});
		v.add(new int[] {685,978,12});
		v.add(new int[] {743,979,12});
		v.add(new int[] {970,979,12});
		v.add(new int[] {879,980,12});
		v.add(new int[] {847,985,12});
		v.add(new int[] {1007,992,12});
		v.add(new int[] {981,993,12});
		v.add(new int[] {819,996,12});
		v.add(new int[] {715,1000,12});
		v.add(new int[] {787,1000,12});
		v.add(new int[] {943,1000,12});
		v.add(new int[] {891,1003,12});
		v.add(new int[] {912,1004,12});
		v.add(new int[] {1075,1004,12});
		v.add(new int[] {754,1009,12});
		v.add(new int[] {966,1015,12});
		v.add(new int[] {1048,1015,12});
		v.add(new int[] {1121,1018,12});
		v.add(new int[] {1016,1020,12});
		v.add(new int[] {1098,1020,12});
		v.add(new int[] {947,1030,12});
		v.add(new int[] {998,1030,12});
		v.add(new int[] {917,1034,12});
		v.add(new int[] {819,1036,12});
		v.add(new int[] {785,1039,12});
		v.add(new int[] {884,1040,12});
		v.add(new int[] {1172,1044,12});
		v.add(new int[] {1052,1049,12});
		v.add(new int[] {1136,1051,12});
		v.add(new int[] {1100,1055,12});
		v.add(new int[] {1025,1059,12});
		v.add(new int[] {1073,1066,12});
		v.add(new int[] {1240,1066,12});
		v.add(new int[] {1008,1069,12});
		v.add(new int[] {986,1073,12});
		v.add(new int[] {1154,1078,12});
		v.add(new int[] {1120,1079,12});
		v.add(new int[] {1184,1086,12});
		v.add(new int[] {1084,1088,12});
		v.add(new int[] {1054,1096,12});
		v.add(new int[] {1134,1098,12});
		v.add(new int[] {1144,1131,12});
		v.add(new int[] {1169,1140,12});
		v.add(new int[] {1269,1142,12});
		v.add(new int[] {1314,1164,12});
		v.add(new int[] {1305,1196,12});
		v.add(new int[] {1336,1278,12});
		v.add(new int[] {1304,1286,12});
		v.add(new int[] {1305,1369,12});
		corr.put(68, v);

		v = new Vector<>(80);
		v.add(new int[] {1215,1081,8});
		v.add(new int[] {1166,1106,8});
		v.add(new int[] {1250,1108,8});
		v.add(new int[] {1218,1114,8});
		v.add(new int[] {1197,1115,8});
		v.add(new int[] {1189,1144,8});
		v.add(new int[] {1239,1156,8});
		v.add(new int[] {1313,1249,8});
		corr.put(80, v);
		*/
	}

	//--------------- 02 ---------------
	private
	void init02()
	{
		Vector<int[]> v = new Vector<>(80);
		v.add(new int[] { 418,1861,4 });
		v.add(new int[] { 411,1821,4 });
		v.add(new int[] { 477,1805,4 });
		v.add(new int[] { 494,1772,4 });
		v.add(new int[] { 474,1695,4 });
		v.add(new int[] { 454,1700,4 });
		v.add(new int[] { 434,1698,4 });
		v.add(new int[] { 431,1677,4 });
		v.add(new int[] { 382,1602,4 });
		v.add(new int[] { 344,1527,4 });
		v.add(new int[] { 387,1454,4 });
		v.add(new int[] { 341,1422,4 });
		v.add(new int[] { 364,1400,4 });
		v.add(new int[] { 411,1409,4 });
		v.add(new int[] { 435,1407,4 });
		v.add(new int[] { 419,1366,4 });
		v.add(new int[] { 458,1357,4 });
		v.add(new int[] { 518,1356,4 });
		v.add(new int[] { 510,1251,4 });
		v.add(new int[] { 547,1250,4 });
		v.add(new int[] { 562,1231,4 });
		v.add(new int[] { 947,1094,4 });
		v.add(new int[] { 963,1108,4 });
		v.add(new int[] { 1103,641,4 });
		v.add(new int[] { 1030,613,4 });
		v.add(new int[] { 636,966,5 });
		corr.put(44+70, v);

		v = new Vector<>(80);
		v.add(new int[] { 517,1355,6 });
		v.add(new int[] { 632,1322,6 });
		corr.put(55+70, v);

		v = new Vector<>(80);
		v.add(new int[] { 560,1405,6 });
		v.add(new int[] { 670,1388,6 });
		v.add(new int[] { 716,1399,6 });
		corr.put(83+70, v);

		v = new Vector<>(80);
		v.add(new int[] { 412,1612,6 });
		v.add(new int[] { 536,1439,6 });
		v.add(new int[] { 638,1419,6 });
		v.add(new int[] { 680,1413,6 });
		v.add(new int[] { 725,1403,6 });
		v.add(new int[] { 724,1424,6 });
		v.add(new int[] { 740,1407,6 });
		v.add(new int[] { 636,966,5 });
		corr.put(93+70, v);

		v = new Vector<>(80);
		v.add(new int[] { 439,1825,2 });
		v.add(new int[] { 427,1804,2 });
		v.add(new int[] { 450,1761,2 });
		v.add(new int[] { 389,1663,2 });
		v.add(new int[] { 376,1645,2 });
		v.add(new int[] { 352,1614,2 });
		v.add(new int[] { 419,1465,2 });
		v.add(new int[] { 450,1470,2 });
		v.add(new int[] { 551,1457,2 });
		v.add(new int[] { 755,1414,2 });
		v.add(new int[] { 777,1413,2 });
		v.add(new int[] { 798,1411,2 });
		v.add(new int[] { 833,1434,2 });
		v.add(new int[] { 848,1417,2 });
		v.add(new int[] { 895,1445,2 });
		v.add(new int[] { 900,1416,2 });
		v.add(new int[] { 923,1390,2 });
		v.add(new int[] { 938,1397,2 });
		v.add(new int[] { 990,1374,2 });
		v.add(new int[] { 1000,1358,2 });
		v.add(new int[] { 1010,1395,2 });
		v.add(new int[] { 1369,328,2 });
		v.add(new int[] { 1432,453,2 });
		v.add(new int[] { 1403,972,2 });
		v.add(new int[] { 1377,1034,2 });
		v.add(new int[] { 1348,1027,2 });
		v.add(new int[] { 1243,1155,2 });
		v.add(new int[] { 1224,1135,2 });
		v.add(new int[] { 1173,1186,2 });
		v.add(new int[] { 1071,1313,2 });
		v.add(new int[] { 636,966,5 });
		corr.put(110+70, v);

		v = new Vector<>(80);
		v.add(new int[] { 592,1413,4 });
		v.add(new int[] { 458,1496,4 });
		v.add(new int[] { 467,1719,4 });
		v.add(new int[] { 481,1803,4 });
		v.add(new int[] { 422,1860,4 });
		corr.put(116+70, v);

		v = new Vector<>(80);
		v.add(new int[] { 1322,1371,2 });
		v.add(new int[] { 1157,1419,2 });
		v.add(new int[] { 1130,1411,2 });
		v.add(new int[] { 437,1829,2 });
		v.add(new int[] { 1109,1454,2 });
		v.add(new int[] { 887,1469,2 });
		v.add(new int[] { 856,1492,2 });
		v.add(new int[] { 412,1470,2 });
		v.add(new int[] { 471,1515,2 });
		v.add(new int[] { 349,1617,2 });
		v.add(new int[] { 429,1811,2 });
		corr.put(131+70, v);

		v = new Vector<>(80);
		v.add(new int[] { 435,1558,6 });
		corr.put(138+70, v);

		v = new Vector<>(80);
		v.add(new int[] { 449,1873,6 });
		v.add(new int[] { 413,1743,6 });
		v.add(new int[] { 451,1548,6 });
		v.add(new int[] { 566,1551,6 });
		corr.put(148+70, v);
	}
}
