package com.chitu.bigdata.sdp.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DESede;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import com.google.common.base.Preconditions;

import java.nio.charset.StandardCharsets;

/**
 * 3DES 加解密
 *
 * @author zouchangzhen
 * @date 2022/4/28
 */
public class TripleDesCipher {

    private DESede desede = null;

    /**
     * @param keyStr 不能随意改动，如果数据库存在加密的数据，修改了秘钥，会导致已经存在加密的字段无法解密
     */
    public TripleDesCipher(String keyStr) {
        Preconditions.checkArgument(StrUtil.isNotBlank(keyStr), "请指定加解密秘钥");
        byte[] key = SecureUtil.generateDESKey(SymmetricAlgorithm.DESede.getValue(), keyStr.getBytes(StandardCharsets.UTF_8)).getEncoded();
        desede = SecureUtil.desede(key);
    }

    /**
     * 加密
     *
     * @param pt 明文
     * @return ct
     */
    public String encrypt(String pt) {
        if (StrUtil.isNotBlank(pt) && !isEncrypted(pt)) {
            return desede.encryptBase64(pt);
        }
        return pt;
    }

    /**
     * 解密
     *
     * @param ct 密文
     * @return pt
     */
    public String decrypt(String ct) {
        if (StrUtil.isNotBlank(ct) && isEncrypted(ct)) {
            return desede.decryptStr(ct);
        }
        return ct;
    }

    /**
     * 判断字符串是否是密文
     *
     * @param text
     * @return
     */
    public boolean isEncrypted(String text) {
        boolean isEncr = false;
        try {
            //能解密出來就是密文，解密不出來就是明文
            desede.decryptStr(text);
            isEncr = true;
        } catch (Exception e) {
            isEncr = false;
        }
        return isEncr;
    }

}
