
package osgi.common.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import osgi.common.vo.AuthorizationVO;
import osgi.common.vo.UserVO;

/**
 * The Class AuthorizationUtil.
 * 
 * @author zhangchangchun
 *
 */
public class AuthorizationUtil {
    private static final String SPLIT_STR = "@";

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationUtil.class);

    private static String appSecret;

    private static String appPublicKey;

    static {
        appSecret = PropertyUtil.getApplicationProp("application.appSecret");
        appPublicKey = PropertyUtil.getApplicationProp("application.appPublicKey");
    }

    public static String generateToken(Object param) {
        long createdTime = System.currentTimeMillis();
        String nonce = RandomUtil.getRandomAscIICode(32);
        String content = "";
        try {
            content = JacksonUtil.getDefaultObjectMapper().writeValueAsString(param);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String message = createdTime + SPLIT_STR + nonce + SPLIT_STR + content;
        try {
            return Base64.encodeBase64String(RSAUtil.encryptByPrivateKey(message.getBytes("UTF-8"), appSecret));
        } catch (Exception e) {
            LOGGER.error("enc token e,", e);
        }
        return null;
    }

    public static AuthorizationVO decryptToken(String token) {
        if (token == null || token.equals("")) {
            return null;
        }
        try {
            String decryptedStr = new String(RSAUtil.decryptByPublicKey(Base64.decodeBase64(token), appPublicKey),
                    "UTF-8");
            String[] decryptedStrArr = decryptedStr.split(SPLIT_STR);
            if (decryptedStrArr.length != 3) {
                return null;
            }
            String userInfo = decryptedStrArr[2];
            UserVO user = JacksonUtil.getDefaultObjectMapper().readValue(userInfo, UserVO.class);
            String createdTime = decryptedStrArr[0];
            String nonce = decryptedStrArr[1];
            AuthorizationVO authVO = new AuthorizationVO();
            authVO.setCreatedTime(Long.parseLong(createdTime));
            authVO.setNonce(nonce);
            authVO.setUser(user);
            return authVO;
        } catch (Exception e) {
            LOGGER.error("dec token e,", e);
        }
        return null;
    }

}
