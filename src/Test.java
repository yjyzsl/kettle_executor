import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Test {

	static Map<Integer, Integer> sizeRelPrice = new LinkedHashMap<>();
	static {
//		sizeRelPrice.put(2, 15);//7.5
//		sizeRelPrice.put(5, 20);//4.0
//		sizeRelPrice.put(10, 38);//3.8
//		sizeRelPrice.put(30, 110);//3.68
//		sizeRelPrice.put(50, 170);//3.4
//		sizeRelPrice.put(100, 330);//3.3
		sizeRelPrice.put(2, 15);
		sizeRelPrice.put(5, 20);
		sizeRelPrice.put(10, 40);
		sizeRelPrice.put(30, 120);
		sizeRelPrice.put(50, 200);
		sizeRelPrice.put(100, 400);
//		sizeRelPrice.put(200, 0);
//		sizeRelPrice.put(500, 0);
	}
	public static void main(String[] args) {
		calcAvgPriceRate();
		priceCalc(2623);
	}
	
	private static void calcAvgPriceRate() {
		for (Integer num : sizeRelPrice.keySet()) {
			System.out.printf("num->%d, price->%d, rate->%s\r\n", num, sizeRelPrice.get(num), String.valueOf(((float)sizeRelPrice.get(num))/num));
		}
	}
	
	
	private static Integer priceCalc(int num) {
		List<Integer> sizes = new ArrayList<>(sizeRelPrice.keySet());
		List<Object[]> results = new ArrayList<>();
		for (int x=sizes.size(); x >= 0; x--) {
			for (int y=sizes.size(); y >= 0; y--) {
				Object[] priceMeyPart = priceCalcByPart(sizeRelPrice, sizes, num, x, y);
				if (null != priceMeyPart && priceMeyPart.length == 2) {
					results.add(priceMeyPart);
				}
			}
		}
		if (results.size() > 0) {
			Object minResult[] = null;
			int minPrice = -1;
			for (Object[] result : results) {
				int xPrice = (Integer)result[0];
				if (minPrice < 0) {
					minPrice = xPrice;
					minResult = result;
					continue;
				}
				if (minPrice > xPrice) {
					minPrice = xPrice;
					minResult = result;
				}
			}
			System.out.printf("minPrice-->%d, complex-->%s\r\n", minPrice, minResult[1]);
		}
		return 0;
	}
	
	/**
	 * 
	 * @param num 需要计算价格的数量
	 * @param sizes
	 * @param x
	 * @param y
	 */
	private static Object[] priceCalcByPart(Map<Integer, Integer> priceMatrix, List<Integer> sizes, int num, int x, int y) {
//		System.out.printf("x:%d, y:%d\r\n",x ,y);
//		int x=0, y=sizeRelPrice.size();
		int remain = num;
		Map<Integer, Integer> detailSizeCalcator = new HashMap<>();
		for (int i=y-1; i>=x && remain > 0; i--) {
			int stdNum = sizes.get(i);
			if (remain >= stdNum) {
				if (remain % stdNum >= 0) {
					int mayStdNum = remain/stdNum;
					remain = remain % stdNum;
					detailSizeCalcator.put(stdNum, mayStdNum);
				}
			}
			if (i == x && remain > 0) {//到最后一个了任然分不尽，如果所填数据251，则表示1无法匹配（因为最小的数量是2），所以按照2计算
				Integer tmp = detailSizeCalcator.get(stdNum);
				if (null == tmp || tmp <= 0) {
					tmp = 1;
				} else {
					tmp++;
				}
				detailSizeCalcator.put(stdNum, tmp);
			}
		}
		//计算总价：
		int totalPrice = 0;
		for (Integer stdNum : detailSizeCalcator.keySet()) {
			totalPrice += detailSizeCalcator.get(stdNum) * priceMatrix.get(stdNum);
		}
		if (totalPrice > 0) {
//			System.out.println("totalPrice-->"+totalPrice+", --->"+detailSizeCalcator.toString());
			return new Object[]{totalPrice, detailSizeCalcator};
		}
		return null;
	}
}
