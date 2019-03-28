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
		Vector<int[]> v;
		v = new Vector<>(80);
		v.add(new int[] {386,1360,4});
		corr.put(21, v);

		v = new Vector<>(80);
		v.add(new int[] {447,1697,4});
		v.add(new int[] {354,1544,4});
		v.add(new int[] {327,1501,4});
		corr.put(31, v);

		v = new Vector<>(80);
		v.add(new int[] {391,1689,6});
		v.add(new int[] {338,1467,6});
		v.add(new int[] {327,1333,6});
		v.add(new int[] {341,1292,6});
		v.add(new int[] {424,1220,6});
		v.add(new int[] {454,1230,6});
		v.add(new int[] {394,1204,6});
		v.add(new int[] {442,1202,6});
		v.add(new int[] {419,1185,6});
		v.add(new int[] {495,1172,6});
		v.add(new int[] {469,1152,6});
		v.add(new int[] {548,1168,6});
		v.add(new int[] {646,1134,6});
		v.add(new int[] {924,865,6});
		corr.put(33, v);

		v = new Vector<>(80);
		v.add(new int[] {324,1503,4});
		v.add(new int[] {352,1550,4});
		corr.put(34, v);

		v = new Vector<>(80);
		v.add(new int[] {392,1682,4});
		v.add(new int[] {356,1600,4});
		v.add(new int[] {447,1277,4});
		corr.put(39, v);
	}

	//--------------- 02 ---------------
	private
	void init02()
	{
		Vector<int[]> v = new Vector<>(80);
		v = new Vector<>(80);
		v.add(new int[] {1283,1164,10});
		v.add(new int[] {1130,1130,10});
		v.add(new int[] {1098,1107,10});
		v.add(new int[] {1071,1059,10});
		v.add(new int[] {961,993,10});
		v.add(new int[] {911,975,10});
		v.add(new int[] {856,964,10});
		v.add(new int[] {833,967,10});
		v.add(new int[] {817,986,10});
		v.add(new int[] {778,972,10});
		v.add(new int[] {757,996,10});
		v.add(new int[] {725,992,10});
		v.add(new int[] {701,973,10});
		v.add(new int[] {671,960,10});
		v.add(new int[] {662,925,10});
		v.add(new int[] {701,901,10});
		v.add(new int[] {672,872,10});
		v.add(new int[] {645,909,10});
		v.add(new int[] {604,889,10});
		v.add(new int[] {588,879,10});
		v.add(new int[] {573,892,10});
		v.add(new int[] {562,874,10});
		v.add(new int[] {538,891,10});
		v.add(new int[] {537,921,10});
		v.add(new int[] {518,861,10});
		v.add(new int[] {487,835,10});
		v.add(new int[] {456,836,10});
		v.add(new int[] {456,868,10});
		v.add(new int[] {431,853,10});
		v.add(new int[] {478,798,10});
		v.add(new int[] {452,803,10});
		v.add(new int[] {375,767,10});
		v.add(new int[] {363,726,10});
		v.add(new int[] {323,728,10});
		v.add(new int[] {350,694,10});
		v.add(new int[] {323,692,10});
		v.add(new int[] {310,654,10});
		v.add(new int[] {368,665,10});
		v.add(new int[] {355,637,10});
		v.add(new int[] {324,559,10});
		v.add(new int[] {391,527,10});
		v.add(new int[] {356,515,10});
		v.add(new int[] {328,519,10});
		v.add(new int[] {285,518,10});
		v.add(new int[] {298,480,10});
		v.add(new int[] {352,484,10});
		v.add(new int[] {385,469,10});
		v.add(new int[] {356,443,10});
		v.add(new int[] {377,400,10});
		corr.put(104+1, v);

		v = new Vector<>(80);
		v.add(new int[] {384,397,10});
		v.add(new int[] {299,471,10});
		v.add(new int[] {339,504,10});
		v.add(new int[] {388,522,10});
		v.add(new int[] {367,528,10});
		v.add(new int[] {349,541,10});
		v.add(new int[] {320,540,10});
		v.add(new int[] {337,569,10});
		v.add(new int[] {311,576,10});
		v.add(new int[] {284,599,10});
		v.add(new int[] {363,624,10});
		v.add(new int[] {405,678,10});
		v.add(new int[] {386,707,10});
		v.add(new int[] {326,709,10});
		v.add(new int[] {410,749,10});
		v.add(new int[] {446,790,10});
		v.add(new int[] {421,798,10});
		v.add(new int[] {488,806,10});
		v.add(new int[] {551,806,10});
		v.add(new int[] {575,830,10});
		v.add(new int[] {551,841,10});
		v.add(new int[] {534,841,10});
		v.add(new int[] {503,852,10});
		v.add(new int[] {551,860,10});
		v.add(new int[] {550,897,10});
		v.add(new int[] {602,887,10});
		v.add(new int[] {626,846,10});
		v.add(new int[] {647,911,10});
		v.add(new int[] {661,862,10});
		v.add(new int[] {664,832,10});
		v.add(new int[] {672,877,10});
		v.add(new int[] {679,904,10});
		v.add(new int[] {684,828,10});
		v.add(new int[] {699,846,10});
		v.add(new int[] {703,916,10});
		v.add(new int[] {733,851,10});
		v.add(new int[] {727,922,10});
		v.add(new int[] {838,889,10});
		corr.put(104+50, v);

		v = new Vector<>(80);
		v.add(new int[] {311,406,10});
		v.add(new int[] {341,436,10});
		v.add(new int[] {402,409,10});
		v.add(new int[] {373,541,10});
		v.add(new int[] {362,616,10});
		v.add(new int[] {388,648,10});
		v.add(new int[] {397,684,10});
		v.add(new int[] {364,689,10});
		v.add(new int[] {473,740,10});
		v.add(new int[] {482,717,10});
		v.add(new int[] {507,714,10});
		v.add(new int[] {545,717,10});
		v.add(new int[] {534,752,10});
		v.add(new int[] {553,736,10});
		v.add(new int[] {565,761,10});
		v.add(new int[] {578,741,10});
		v.add(new int[] {640,728,10});
		v.add(new int[] {658,724,10});
		v.add(new int[] {732,768,10});
		v.add(new int[] {815,712,10});
		v.add(new int[] {1525,880,10});
		v.add(new int[] {1565,823,10});
		v.add(new int[] {1642,841,10});
		v.add(new int[] {620,966,4});
		v.add(new int[] {1702,843,10});
		v.add(new int[] {1700,920,10});
		corr.put(104+104, v);

		v = new Vector<>(80);
		v.add(new int[] {326,372,10});
		v.add(new int[] {317,412,10});
		v.add(new int[] {394,401,10});
		v.add(new int[] {414,408,10});
		v.add(new int[] {374,424,10});
		v.add(new int[] {343,474,10});
		v.add(new int[] {403,488,10});
		v.add(new int[] {384,498,10});
		v.add(new int[] {373,536,10});
		v.add(new int[] {395,539,10});
		v.add(new int[] {411,554,10});
		v.add(new int[] {353,574,10});
		v.add(new int[] {412,581,10});
		v.add(new int[] {419,623,10});
		v.add(new int[] {427,659,10});
		v.add(new int[] {390,669,10});
		v.add(new int[] {496,673,10});
		v.add(new int[] {503,704,10});
		v.add(new int[] {523,729,10});
		v.add(new int[] {539,704,10});
		v.add(new int[] {562,709,10});
		v.add(new int[] {565,698,10});
		v.add(new int[] {583,673,10});
		v.add(new int[] {672,679,10});
		v.add(new int[] {686,701,10});
		v.add(new int[] {817,653,10});
		v.add(new int[] {1098,643,10});
		v.add(new int[] {1242,703,10});
		v.add(new int[] {1407,704,10});
		v.add(new int[] {1479,783,10});
		v.add(new int[] {1543,735,10});
		v.add(new int[] {1536,775,10});
		corr.put(104+130, v);
	}
}
