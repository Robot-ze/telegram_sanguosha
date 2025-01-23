package components;

/**
 * 简单的加解密，免得暴露人家的chatId
 */
public class SimpleEnDecrypt {
    public static final String KEY = "+6426457345237966452"; // 密钥
    public static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; // 目标字符集合
    public static final int CHARSETLEN = CHARSET.length();

    // 加密方法
    public static String encrypt(String password) {
        //System.out.println("password="+password);
        StringBuilder encrypted = new StringBuilder();

        for (int i = 0; i < password.length(); i++) {
            int xorResult = password.charAt(i) ^ KEY.charAt(i); // 异或操作
            int mappedIndex = Math.abs(xorResult % CHARSETLEN); // 映射到字符集合
            encrypted.append(CHARSET.charAt(mappedIndex));
        }
        return encrypted.toString();
    }

    // 解密方法
    public static String decrypt(String encrypted) {
        StringBuilder decrypted = new StringBuilder();
        try {
            for (int i = 0; i < encrypted.length(); i++) {
                int mappedIndex = CHARSET.indexOf(encrypted.charAt(i));
                if (mappedIndex == -1) {
                    throw new IllegalArgumentException("加密字符不在可映射范围内！");
                }
                int originalChar = mappedIndex ^ KEY.charAt(i); // 逆向异或操作
                decrypted.append((char) originalChar);
            }
            return decrypted.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
