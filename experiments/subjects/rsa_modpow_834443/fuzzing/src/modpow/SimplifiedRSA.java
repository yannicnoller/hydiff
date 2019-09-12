package modpow;

public class SimplifiedRSA {

	public static int modPowFastKocherReduction(int num, int e, int m, int max_high) { 
		// computes num^e mod m

		int s = 1;
		int y = num;
		int res = 0;

		int bound = (int) (Math.log(max_high + 1) / Math.log(2));
		
        int j=0;
		while (e > 0) {
			if (e % 2 == 1) {
				// res = (s * y) % m;
				// reduction:
				int tmp = s * y;
				if (tmp > m) {
					tmp = tmp - m;
				}
				res = tmp % m;
			} else {
				res = s;
			}
			s = (res * res) % m; // squaring the base
			e /= 2;
			j++;
			
			if(j==bound) break;
		}
		return res;
	}

}
