package com.zh.programmer.util;
/**
 * 字符串共用操作
 * @author llq
 *
 */
public class StringUtil {
	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str){
		if(str == null)return true;
		//"".equals(str) 防止空指针异常
		if("".equals(str))return true;
		return false;
	}

	/**
	 * 生成编号
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	public static String generateSn(String prefix,String suffix){
		String sn = prefix + System.currentTimeMillis() + suffix;
		return sn;
	}

	/**
	 *
	 * @param str
	 * @return
	 */
	public static String convertToUnderLine(String str){
		String newStr = "";
		if(isEmpty(str))return "";
		for(int i=0;i<str.length(); i++){
			char c = str.charAt(i);
			if(Character.isUpperCase(c)){
				if(i == 0){
					newStr += Character.toLowerCase(c);
					continue;
				}
				newStr += "_" + Character.toLowerCase(c);
				continue;
			}
			newStr += c;
		}
		return newStr;
	}
}
